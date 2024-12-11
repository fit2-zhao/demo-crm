package io.demo.file.engine;

import io.demo.common.exception.GenericException;
import io.demo.common.util.LogUtils;
import io.minio.*;
import io.minio.messages.Item;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MinIO file repository implementation class.
 * Provides operations for uploading, downloading, deleting, and copying files.
 */
@Component
public class MinioRepository implements FileRepository {

    private MinioClient client;

    // Constant definitions
    private static final int BUFFER_SIZE = 8192;
    public static final String BUCKET = "demo";
    public static final String ENDPOINT = "endpoint";
    public static final String ACCESS_KEY = "accessKey";
    public static final String SECRET_KEY = "secretKey";

    /**
     * Initializes the MinIO client.
     *
     * @param client MinIO client instance
     */
    public void init(MinioClient client) {
        if (this.client == null) {
            this.client = client;
        }
    }

    /**
     * Dynamically initializes the MinIO client based on the configuration.
     *
     * @param minioConfig MinIO configuration information
     */
    public void init(Map<String, Object> minioConfig) {
        if (minioConfig == null || minioConfig.isEmpty()) {
            LogUtils.info("MinIO initialization failed, parameter [minioConfig] is empty");
            return;
        }

        try {
            String serverUrl = (String) minioConfig.get(ENDPOINT);
            if (StringUtils.isNotEmpty(serverUrl)) {
                // Create MinioClient client
                client = MinioClient.builder()
                        .endpoint(serverUrl)
                        .credentials((String) minioConfig.get(ACCESS_KEY), (String) minioConfig.get(SECRET_KEY))
                        .build();
                boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(BUCKET).build());
                if (!exists) {
                    client.makeBucket(MakeBucketArgs.builder().bucket(BUCKET).build());
                }
            }
        } catch (Exception e) {
            LogUtils.error("MinIOClient initialization failed!", e);
        }
    }

    /**
     * Gets the file storage path.
     *
     * @param request File request object
     * @return File path
     * @throws GenericException if the folder name is invalid
     */
    private String getPath(FileRequest request) {
        String folder = request.getFolder();
        if (!StringUtils.startsWithAny(folder, "system", "project", "organization")) {
            throw new GenericException("folder.error");
        }
        return StringUtils.join(folder, "/", request.getFileName());
    }

    /**
     * Saves a file to MinIO.
     *
     * @param file    Uploaded file
     * @param request File request object containing storage path and file name
     * @return File storage path
     * @throws Exception if the upload fails
     */
    @Override
    public String saveFile(MultipartFile file, FileRequest request) throws Exception {
        String filePath = getPath(request);
        client.putObject(PutObjectArgs.builder()
                .bucket(BUCKET)
                .object(filePath)
                .stream(file.getInputStream(), file.getSize(), -1)
                .build());
        return filePath;
    }

    /**
     * Saves a byte array file to MinIO.
     *
     * @param bytes   File byte array
     * @param request File request object containing storage path and file name
     * @throws Exception if the upload fails
     */
    @Override
    public void saveFile(byte[] bytes, FileRequest request) throws Exception {
        String filePath = getPath(request);
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            client.putObject(PutObjectArgs.builder()
                    .bucket(BUCKET)
                    .object(filePath)
                    .stream(inputStream, bytes.length, -1)
                    .build());
        }
    }

    /**
     * Saves an input stream file to MinIO.
     *
     * @param inputStream Input stream
     * @param request     File request object containing storage path and file name
     * @return File path
     * @throws Exception if the upload fails
     */
    @Override
    public String saveFile(InputStream inputStream, FileRequest request) throws Exception {
        String filePath = getPath(request);
        client.putObject(PutObjectArgs.builder()
                .bucket(BUCKET)
                .object(filePath)
                .stream(inputStream, -1, 5242880)
                .build());
        return filePath;
    }

    /**
     * Deletes a file.
     *
     * @param request File request object containing the file information to be deleted
     * @throws Exception if the deletion fails
     */
    @Override
    public void delete(FileRequest request) throws Exception {
        String filePath = getPath(request);
        removeObject(BUCKET, filePath);
    }

    /**
     * Deletes a folder and its contents.
     *
     * @param request File request object containing the folder information to be deleted
     * @throws Exception if the deletion fails
     */
    @Override
    public void deleteFolder(FileRequest request) throws Exception {
        String filePath = getPath(request);
        removeObjects(BUCKET, filePath);
    }

    /**
     * Gets all file names in the specified folder.
     *
     * @param request File request object containing folder information
     * @return List of file names
     * @throws Exception if the retrieval fails
     */
    @Override
    public List<String> getFolderFileNames(FileRequest request) throws Exception {
        return listObjects(BUCKET, getPath(request));
    }

    /**
     * Copies a file.
     *
     * @param request Copy file request object containing source and target file information
     * @throws Exception if the copy fails
     */
    @Override
    public void copyFile(FileCopyRequest request) throws Exception {
        String sourcePath = StringUtils.join(request.getCopyFolder(), "/", request.getCopyfileName());
        String targetPath = getPath(request);
        client.copyObject(CopyObjectArgs.builder()
                .bucket(BUCKET)
                .object(targetPath)
                .source(CopySource.builder()
                        .bucket(BUCKET)
                        .object(sourcePath)
                        .build())
                .build());
    }

    /**
     * Deletes the specified object.
     *
     * @param bucketName Bucket name
     * @param objectName File name
     * @throws Exception if the deletion fails
     */
    private void removeObject(String bucketName, String objectName) throws Exception {
        client.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
    }

    /**
     * Recursively deletes all files under the specified path.
     *
     * @param bucketName Bucket name
     * @param objectName Path name
     * @throws Exception if the deletion fails
     */
    public void removeObjects(String bucketName, String objectName) throws Exception {
        List<String> objects = listObjects(bucketName, objectName);
        for (String object : objects) {
            removeObject(bucketName, object);
        }
    }

    /**
     * Gets all files under the specified path.
     *
     * @param bucketName Bucket name
     * @param objectName Path name
     * @return List of files
     * @throws Exception if the retrieval fails
     */
    public List<String> listObjects(String bucketName, String objectName) throws Exception {
        List<String> list = new ArrayList<>(12);
        Iterable<Result<Item>> results = client.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(objectName)
                        .build());
        for (Result<Item> result : results) {
            Item item = result.get();
            if (item.isDir()) {
                list.addAll(listObjects(bucketName, item.objectName()));
            } else {
                list.add(item.objectName());
            }
        }
        return list;
    }

    /**
     * Gets the file content as a byte array.
     *
     * @param request File request object
     * @return File byte array
     * @throws Exception if the retrieval fails
     */
    @Override
    public byte[] getFile(FileRequest request) throws Exception {
        return getFileAsStream(request).readAllBytes();
    }

    /**
     * Downloads a file to the specified path.
     *
     * @param request  File request object
     * @param fullPath Full path to download the file
     * @throws Exception if the download fails
     */
    @Override
    public void downloadFile(FileRequest request, String fullPath) throws Exception {
        String fileName = getPath(request);
        try (InputStream inputStream = client.getObject(
                GetObjectArgs.builder()
                        .bucket(BUCKET)
                        .object(fileName)
                        .build());
             BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(fullPath))) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    /**
     * Gets the input stream of the file.
     *
     * @param request File request object
     * @return File input stream
     * @throws Exception if the retrieval fails
     */
    @Override
    public InputStream getFileAsStream(FileRequest request) throws Exception {
        String fileName = getPath(request);
        return client.getObject(GetObjectArgs.builder()
                .bucket(BUCKET)
                .object(fileName)
                .build());
    }

    /**
     * Gets the file size.
     *
     * @param request File request object
     * @return File size
     * @throws Exception if the retrieval fails
     */
    @Override
    public long getFileSize(FileRequest request) throws Exception {
        String fileName = getPath(request);
        return client.statObject(StatObjectArgs.builder()
                .bucket(BUCKET)
                .object(fileName)
                .build()).size();
    }
}