package com.ecommerce.project.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class ImageService {
  @Autowired
  private Cloudinary cloudinary;
    public String uploadImage(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        String url = uploadResult.get("url").toString();
        return url;
    }
}
//
//"public_id" → unique identifier for the image
//
//"version" → version number
//
//"format" → file extension (jpg, png, etc.)
//
//"resource_type" → usually "image"
//
//        "url" → the non-secure HTTP URL
//
//"secure_url" → the HTTPS URL (recommended)