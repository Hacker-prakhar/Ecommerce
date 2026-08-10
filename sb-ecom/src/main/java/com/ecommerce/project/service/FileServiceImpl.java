package com.ecommerce.project.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {
        String originalFileName = file.getOriginalFilename(); // get original filename
        String randomId = UUID.randomUUID().toString(); // generate randomId from each file so that we do not ewrite file with same name
        String fileName = randomId.concat(originalFileName.substring(originalFileName.lastIndexOf('.'))); // extract the eextension name
        String filePath = path + File.separator + fileName; // folder + File.separator is a platform-dependent character used to separate directories in a file path.
        // On Windows, it’s \ (backslash).
        //    On Linux/macOS, it’s / (forward slash).
        // file name is incoded + extension

        File folder = new File(path);
        if (!folder.exists())
            folder.mkdirs();

        Files.copy(file.getInputStream(), Paths.get(filePath)); // copy file here
        return fileName; //return string
    }
}

