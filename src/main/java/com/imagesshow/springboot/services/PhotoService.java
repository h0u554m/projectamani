package com.imagesshow.springboot.services;

import com.azure.core.exception.HttpResponseException;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

@Service
public class PhotoService {

  @Value("${azure.storage.container-name}")
  private String containerName;

  @Value("${azure.storage.connection-string}")
  private String connectionString;

  public static String generateUniqueBlobName(String originalFileName) {
    // Get the current timestamp
    String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS")
      .format(new Date());

    // Generate a random UUID (Universally Unique Identifier)
    String randomUUID = UUID.randomUUID().toString();

    // Extract the file extension from the original filename
    String fileExtension = getFileExtension(originalFileName);

    // Combine all parts to form a unique blob name
    return timestamp + "_" + randomUUID + "." + fileExtension;
  }

  private static String getFileExtension(String fileName) {
    int lastDotIndex = fileName.lastIndexOf(".");
    if (lastDotIndex != -1) {
      return fileName.substring(lastDotIndex + 1);
    }
    return ""; // If no file extension found
  }

  private BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
    .endpoint("https://photosgalary.blob.core.windows.net/")
    .sasToken(
      "?sv=2022-11-02&ss=bfqt&srt=sco&sp=rwdlacupiytfx&se=2024-01-13T08:42:34Z&st=2024-01-13T00:42:34Z&spr=https,http&sig=06TY7z%2FNneyPrRihMAXqMsF48TRYewUzFsjmBvLWUtQ%3D"
    )
    .buildClient();

  public ResponseEntity<String> uploadPhoto(MultipartFile file) {
    // Check if the uploaded file is an image
    if (!isImage(file)) {
      return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body("Invalid file format. Please upload only images.");
    }

    try {
      BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(
        "$web"
      );

      BlobClient blobClient = containerClient.getBlobClient(
        generateUniqueBlobName(file.getOriginalFilename())
      );

      InputStream photo = file.getInputStream();
      System.err.println(blobClient.getBlobName());
      System.err.println(blobClient.getBlobUrl());
      try {
        blobClient.upload(photo, photo.available());
      } catch (HttpResponseException e) {
        // TODO: handle exception
        System.err.println(e);
      }

      return ResponseEntity.ok("File uploaded successfully!");
    } catch (Exception e) {
      // TODO: handle exception
      System.err.println(e);
      return ResponseEntity.ok("error " + e);
    }
  }

  // Utility method to check if the file is an image
  private boolean isImage(MultipartFile file) {
    // Implement your logic to check the file's content type, for example:
    String contentType = file.getContentType();
    return contentType != null && contentType.startsWith("image");
  }

  public String listPhotos(Model model) {
    try {
      BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(
        "$web"
      );
      List<String> photoNames = containerClient
        .listBlobs()
        .stream()
        .map(blobItem -> blobItem.getName())
        .collect(Collectors.toList());

      model.addAttribute("photoNames", photoNames);
      return "photo-list.html"; // Thymeleaf template name
    } catch (Exception e) {
      model.addAttribute("error", "Failed to list photos: " + e.getMessage());
      return "error"; // Thymeleaf template for displaying error
    }
  }
  /* 
  public ResponseEntity<InputStream> downloadPhoto(String fileName) {
    try {
      BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(
        containerName
      );
      BlobClient blobClient = containerClient.getBlobClient(fileName);
      InutStream blobStream = blobClient
        .openQueryCursor()
        .stream()
        .collect(Collectors.toList())
        .get(0);
      return ResponseEntity.ok(blobStream);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  } */
  /* 
  public ResponseEntity<String> deletePhoto(String fileName) {
    try {
      BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(
        containerName
      );
      containerClient.getBlobClient(fileName).delete();
      return ResponseEntity.ok("File deleted successfully!");
    } catch (Exception e) {
      return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body("Failed to delete file: " + e.getMessage());
    }
  } */
}
