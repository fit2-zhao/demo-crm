package io.demo.file.engine;

import io.demo.common.exception.GenericException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.util.FileUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

/**
 * Local file repository implementation, providing operations for storing, deleting, and retrieving local files.
 * <p>
 * This class implements the {@link FileRepository} interface and provides specific implementations for saving, deleting, and retrieving files, suitable for local storage scenarios.
 * </p>
 */
@Component
public class LocalRepository implements FileRepository {
    private static final String DEFAULT_FOLDER = "/opt/demo/data/files/";

    /**
     * Saves a file to local storage.
     *
     * @param multipartFile The file to be saved, of type {@link MultipartFile}.
     * @param request       File request information containing the storage path, file name, etc.
     * @return The path of the saved file.
     * @throws IOException If an I/O error occurs during file saving.
     */
    @Override
    public String saveFile(MultipartFile multipartFile, FileRequest request) throws IOException {
        if (multipartFile == null || request == null || StringUtils.isEmpty(request.getFileName()) || StringUtils.isEmpty(request.getFolder())) {
            return null;
        }
        FileValidate.validateFileName(request.getFolder(), request.getFileName());
        createFileDir(request);
        File file = new File(getFilePath(request));
        FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), file);
        return file.getPath();
    }

    /**
     * Creates the directory for the file.
     *
     * @param request File request information containing the directory path.
     * @throws RuntimeException If the directory creation fails.
     */
    private void createFileDir(FileRequest request) {
        String dir = getFileDir(request);
        File fileDir = new File(dir);
        if (!fileDir.exists() && !fileDir.mkdirs()) {
            throw new RuntimeException("Failed to create directory: " + dir);
        }
    }

    /**
     * Saves a file represented by a byte array to local storage.
     *
     * @param bytes   The byte array representing the file content.
     * @param request File request information containing the storage path, file name, etc.
     * @throws IOException If an I/O error occurs during file saving.
     */
    @Override
    public void saveFile(byte[] bytes, FileRequest request) throws IOException {
        File file = new File(getFilePath(request));
        // Check if the parent directory exists, create it if it does not
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean dirsCreated = parentDir.mkdirs(); // Create parent directory
            if (!dirsCreated) {
                throw new IOException("Failed to create directories: " + parentDir.getAbsolutePath());
            }
        }
        try (OutputStream ops = new FileOutputStream(file)) {
            ops.write(bytes);
        }
    }

    /**
     * Saves a file represented by an input stream to local storage.
     *
     * @param inputStream The input stream of the file content.
     * @param request     File request information containing the storage path, file name, etc.
     * @return The path of the saved file.
     * @throws Exception If an error occurs during file saving.
     */
    @Override
    public String saveFile(InputStream inputStream, FileRequest request) throws Exception {
        File file = new File(getFilePath(request));
        FileUtils.copyInputStreamToFile(inputStream, file);
        return file.getPath();
    }

    /**
     * Deletes a file.
     *
     * @param request File request information containing the path and name of the file to be deleted.
     * @throws Exception If an error occurs during file deletion.
     */
    @Override
    public void delete(FileRequest request) throws Exception {
        String path = StringUtils.join(getFilePath(request));
        File file = new File(path);
        FileUtil.deleteContents(file);
        if (file.exists() && !file.delete()) {
            throw new RuntimeException("Failed to delete file: " + path);
        }
    }

    /**
     * Deletes a specified folder and all its contents.
     *
     * @param request File request information containing the path of the folder to be deleted.
     * @throws Exception If an error occurs during folder deletion.
     */
    @Override
    public void deleteFolder(FileRequest request) throws Exception {
        FileValidate.validateFileName(request.getFolder(), request.getFileName());
        this.delete(request);
    }

    /**
     * Retrieves the byte content of a file.
     *
     * @param request File request information containing the path and name of the file.
     * @return The byte array of the file content.
     * @throws Exception If an error occurs during file retrieval.
     */
    @Override
    public byte[] getFile(FileRequest request) throws Exception {
        File file = new File(getFilePath(request));
        return Files.readAllBytes(file.toPath());
    }

    /**
     * Retrieves the input stream of a file.
     *
     * @param request File request information containing the path and name of the file.
     * @return The input stream of the file content.
     * @throws Exception If an error occurs during file retrieval.
     */
    @Override
    public InputStream getFileAsStream(FileRequest request) throws Exception {
        return new FileInputStream(getFilePath(request));
    }

    /**
     * Downloads a file to the specified local path (not implemented).
     *
     * @param request   File request information containing the path of the file to be downloaded.
     * @param localPath The local path to which the file will be downloaded.
     * @throws UnsupportedOperationException This method is not implemented and will throw an exception when called.
     */
    @Override
    public void downloadFile(FileRequest request, String localPath) {
        throw new UnsupportedOperationException("Download file is not supported in LocalFileRepository.");
    }

    /**
     * Retrieves a list of file names in a specified folder (not implemented).
     *
     * @param request File request information containing the folder path.
     * @return The list of file names in the folder.
     */
    @Override
    public List<String> getFolderFileNames(FileRequest request) {
        return null;
        // Return null or unimplemented logic
    }

    /**
     * Copies a file to a specified directory (not implemented).
     *
     * @param request File copy request information.
     * @throws GenericException If an unsupported operation occurs during file copy.
     */
    @Override
    public void copyFile(FileCopyRequest request) throws Exception {
        throw new GenericException("Not support copy file");
    }

    /**
     * Retrieves the size of a specified file.
     *
     * @param request File request information containing the path and name of the file.
     * @return The size of the file in bytes.
     * @throws Exception If an error occurs during file size retrieval.
     */
    @Override
    public long getFileSize(FileRequest request) throws Exception {
        File file = new File(getFilePath(request));
        return file.length();
    }

    /**
     * Retrieves the full path of the file.
     *
     * @param request File request information containing the folder path and file name.
     * @return The full path of the file.
     */
    private String getFilePath(FileRequest request) {
        FileValidate.validateFileName(request.getFolder(), request.getFileName());
        return StringUtils.join(DEFAULT_FOLDER, getFileDir(request), "/", request.getFileName());
    }

    /**
     * Retrieves the directory path where the file is located.
     *
     * @param request File request information containing the folder path.
     * @return The directory path.
     */
    private String getFileDir(FileRequest request) {
        FileValidate.validateFileName(request.getFolder(), request.getFileName());
        return request.getFolder();
    }
}