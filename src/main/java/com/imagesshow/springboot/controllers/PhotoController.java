package com.imagesshow.springboot.controllers;

import com.imagesshow.springboot.services.*;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/photos")
public class PhotoController {

  @Autowired
  private PhotoService photoService;

  @PostMapping("/upload")
  public void uploadPhoto(@RequestParam("file") MultipartFile file) {
    photoService.uploadPhoto(file);
  }

  @GetMapping("/list")
  public String listPhotos(Model model) {
    try {
      String photoNames = photoService.listPhotos(model);
      model.addAttribute("photoNames", photoNames);
      return "photo-list"; // Thymeleaf template name
    } catch (Exception e) {
      model.addAttribute("error", "Failed to list photos: " + e.getMessage());
      return "error"; // Thymeleaf template for displaying error
    }
  }
}
