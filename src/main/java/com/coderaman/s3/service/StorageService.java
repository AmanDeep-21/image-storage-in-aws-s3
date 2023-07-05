package com.coderaman.s3.service;

import com.amazonaws.util.IOUtils;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
@Slf4j
public class StorageService {

    @Value("${application.bucket.name}")
    private String bucketName;

    @Autowired
    private AmazonS3 amazonS3Client;

    public String uploadFile(MultipartFile file) {
        File fileObject = convertMultipartFileToFile(file);
        String fileName = System.currentTimeMillis()+"_"+file.getOriginalFilename();
        amazonS3Client.putObject(new PutObjectRequest(bucketName, fileName, fileObject));
        fileObject.delete();
        return "File uploaded : " + fileName;
    }

    public byte[] downloadFile(String fileName) {
        S3Object s3object = amazonS3Client.getObject(this.bucketName, fileName);
        S3ObjectInputStream inputStream = s3object.getObjectContent();
        try {
            byte[] content = IOUtils.toByteArray(inputStream);
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String deleteFile(String fileName) {
        amazonS3Client.deleteObject(this.bucketName, fileName);
        return fileName + " removed...";
    }

    private File convertMultipartFileToFile(MultipartFile file) {
        File convertedFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            log.error("Error converting multipartFile to file " + e);
        }
        return convertedFile;
    }
}
