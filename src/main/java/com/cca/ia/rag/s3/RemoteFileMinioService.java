package com.cca.ia.rag.s3;

import io.minio.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class RemoteFileMinioService implements RemoteFileService {

    @Value("${s3.endpoint}")
    private String s3Endpoint;

    @Value("${s3.username}")
    private String username;

    @Value("${s3.password}")
    private String password;

    @Value("${s3.bucket}")
    private String bucketName;

    private MinioClient minioClient = null;

    private MinioClient getMinioClient() {

        if (minioClient != null)
            return minioClient;

        minioClient = MinioClient.builder().endpoint(s3Endpoint).credentials(username, password).build();

        return minioClient;
    }

    @Override
    public void putObject(InputStream data, String pathName, String objectName) throws Exception {
        MinioClient minioClient = getMinioClient();

        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            throw new Exception("Bucket not found");
        }

        minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(pathName + "/" + objectName).stream(data, -1, 10485760).build());
    }

    @Override
    public InputStream getObject(String pathName, String objectName) throws Exception {
        MinioClient minioClient = getMinioClient();

        return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(pathName + "/" + objectName).build());

    }

    @Override
    public void deleteObject(String pathName, String objectName) throws Exception {
        MinioClient minioClient = getMinioClient();

        minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(pathName + "/" + objectName).build());
    }

}