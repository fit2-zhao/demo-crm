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
 * MinIO 文件存储库实现类。
 * 提供文件的上传、下载、删除、复制等操作。
 */
@Component
public class MinioRepository implements FileRepository {

    private MinioClient client;

    // 常量定义
    private static final int BUFFER_SIZE = 8192;
    public static final String BUCKET = "demo";
    public static final String ENDPOINT = "endpoint";
    public static final String ACCESS_KEY = "accessKey";
    public static final String SECRET_KEY = "secretKey";

    /**
     * 初始化 MinIO 客户端。
     *
     * @param client MinIO 客户端实例
     */
    public void init(MinioClient client) {
        if (this.client == null) {
            this.client = client;
        }
    }

    /**
     * 根据配置动态初始化 MinIO 客户端。
     *
     * @param minioConfig MinIO 配置信息
     */
    public void init(Map<String, Object> minioConfig) {
        if (minioConfig == null || minioConfig.isEmpty()) {
            LogUtils.info("MinIO初始化失败，参数[minioConfig]为空");
            return;
        }

        try {
            String serverUrl = (String) minioConfig.get(ENDPOINT);
            if (StringUtils.isNotEmpty(serverUrl)) {
                // 创建 MinioClient 客户端
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
            LogUtils.error("MinIOClient初始化失败！", e);
        }
    }

    /**
     * 获取文件存储路径。
     *
     * @param request 文件请求对象
     * @return 文件路径
     * @throws GenericException 如果文件夹名无效抛出异常
     */
    private String getPath(FileRequest request) {
        String folder = request.getFolder();
        if (!StringUtils.startsWithAny(folder, "system", "project", "organization")) {
            throw new GenericException("folder.error");
        }
        return StringUtils.join(folder, "/", request.getFileName());
    }

    /**
     * 保存文件到 MinIO。
     *
     * @param file    上传的文件
     * @param request 文件请求对象，包含存储路径和文件名
     * @return 文件存储路径
     * @throws Exception 如果上传失败抛出异常
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
     * 保存字节数组文件到 MinIO。
     *
     * @param bytes   文件字节数组
     * @param request 文件请求对象，包含存储路径和文件名
     * @throws Exception 如果上传失败抛出异常
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
     * 保存输入流文件到 MinIO。
     *
     * @param inputStream 输入流
     * @param request     文件请求对象，包含存储路径和文件名
     * @return 文件路径
     * @throws Exception 如果上传失败抛出异常
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
     * 删除文件。
     *
     * @param request 文件请求对象，包含待删除的文件信息
     * @throws Exception 如果删除失败抛出异常
     */
    @Override
    public void delete(FileRequest request) throws Exception {
        String filePath = getPath(request);
        removeObject(BUCKET, filePath);
    }

    /**
     * 删除文件夹及其内容。
     *
     * @param request 文件请求对象，包含待删除的文件夹信息
     * @throws Exception 如果删除失败抛出异常
     */
    @Override
    public void deleteFolder(FileRequest request) throws Exception {
        String filePath = getPath(request);
        removeObjects(BUCKET, filePath);
    }

    /**
     * 获取指定文件夹下的所有文件名。
     *
     * @param request 文件请求对象，包含文件夹信息
     * @return 文件名列表
     * @throws Exception 如果获取失败抛出异常
     */
    @Override
    public List<String> getFolderFileNames(FileRequest request) throws Exception {
        return listObjects(BUCKET, getPath(request));
    }

    /**
     * 复制文件。
     *
     * @param request 复制文件请求对象，包含源文件和目标文件信息
     * @throws Exception 如果复制失败抛出异常
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
     * 删除指定对象。
     *
     * @param bucketName 存储桶名
     * @param objectName 文件名
     * @throws Exception 如果删除失败抛出异常
     */
    private void removeObject(String bucketName, String objectName) throws Exception {
        client.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
    }

    /**
     * 递归删除指定路径下的所有文件。
     *
     * @param bucketName 存储桶名
     * @param objectName 路径名
     * @throws Exception 如果删除失败抛出异常
     */
    public void removeObjects(String bucketName, String objectName) throws Exception {
        List<String> objects = listObjects(bucketName, objectName);
        for (String object : objects) {
            removeObject(bucketName, object);
        }
    }

    /**
     * 获取指定路径下的所有文件。
     *
     * @param bucketName 存储桶名
     * @param objectName 路径名
     * @return 文件列表
     * @throws Exception 如果获取失败抛出异常
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
     * 获取文件内容为字节数组。
     *
     * @param request 文件请求对象
     * @return 文件字节数组
     * @throws Exception 如果获取失败抛出异常
     */
    @Override
    public byte[] getFile(FileRequest request) throws Exception {
        return getFileAsStream(request).readAllBytes();
    }

    /**
     * 下载文件到指定路径。
     *
     * @param request  文件请求对象
     * @param fullPath 下载文件的完整路径
     * @throws Exception 如果下载失败抛出异常
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
     * 获取文件的输入流。
     *
     * @param request 文件请求对象
     * @return 文件输入流
     * @throws Exception 如果获取失败抛出异常
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
     * 获取文件大小。
     *
     * @param request 文件请求对象
     * @return 文件大小
     * @throws Exception 如果获取失败抛出异常
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
