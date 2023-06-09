package com.setqt.Hiring.Service.Firebase;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import lombok.Data;
import org.apache.commons.io.FilenameUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Service
public class FirebaseDocumentFileService implements IStorageService{


    private boolean isDocumentFile(MultipartFile file) {
        //Let install FileNameUtils
        String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
        return Arrays.asList(new String[] {"doc","docx","pdf"})
                .contains(fileExtension.trim().toLowerCase());
    }

    @Override
    public String getFileUrl(String name) {
        Bucket bucket = StorageClient.getInstance().bucket();
        // Get the file URL
        BlobId blobId = BlobId.of(bucket.getName(),"cvs_candidates/" + name);
        Blob blob = bucket.getStorage().get(blobId);
        return blob.signUrl(100000, TimeUnit.DAYS).toString();
    }

    @Override
    public String save(MultipartFile file, String pathName) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file.");
        }
        //check file is image ?
        if(!isDocumentFile(file)) {
            throw new RuntimeException("You can only upload document file (pdf, doc, docx)");
        }
        //file must be <= 5Mb
        float fileSizeInMegabytes = file.getSize() / 1_000_000.0f;
        if(fileSizeInMegabytes > 5.0f) {
            throw new RuntimeException("File must be <= 5Mb");
        }

        Bucket bucket = StorageClient.getInstance().bucket();

        //String name = generateFileName(file.getOriginalFilename());

        bucket.create( "cvs_candidates/" + pathName, file.getBytes(), file.getContentType());

        return pathName;
    }

    @Override
    public String save(BufferedImage bufferedImage, String originalFileName) throws IOException {
        byte[] bytes = getByteArrays(bufferedImage, getExtension(originalFileName));

        Bucket bucket = StorageClient.getInstance().bucket();

        String name = generateFileName(originalFileName);

        bucket.create("cvs_candidates/" + name, bytes);

        return name;
    }

    @Override
    public void delete(String name) throws IOException {
        Bucket bucket = StorageClient.getInstance().bucket();

        if (StringUtils.isEmpty(name)) {
            throw new RuntimeException("invalid file name");
        }

        Blob blob = bucket.get("cvs_candidates/" + name);

        if (blob == null) {
            throw new RuntimeException("file not found");
        }

        blob.delete();
    }

    @Override
    public String update(MultipartFile file, String pathName) throws IOException {
        delete(pathName);
        return getFileUrl(save(file, pathName));
    }

    @Data
    @Configuration
    @ConfigurationProperties(prefix = "firebase.document")
    public class Properties {
        private String bucketName;

    }
}
