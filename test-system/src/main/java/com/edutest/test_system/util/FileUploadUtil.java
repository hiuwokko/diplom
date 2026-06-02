package com.edutest.test_system.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class FileUploadUtil {

    public static String saveFile(String uploadDir, String fileName, InputStream inputStream) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

       
        String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
        Path filePath = uploadPath.resolve(uniqueFileName);
        
        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        
        return uniqueFileName;
    }
}
