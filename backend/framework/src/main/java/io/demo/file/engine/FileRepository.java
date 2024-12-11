package io.demo.file.engine;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * File repository interface, defining common methods for file operations such as save, delete, download, and retrieve.
 * <p>
 * Classes implementing this interface are responsible for providing specific file storage and management operations.
 * </p>
 */
public interface FileRepository {

    /**
     * Saves a file to the file repository.
     *
     * @param file    The file to be saved, of type {@link MultipartFile}.
     * @param request File request information containing metadata of the file.
     * @return The identifier of the saved file (usually the file path or a unique ID).
     * @throws Exception If an error occurs during file saving.
     */
    String saveFile(MultipartFile file, FileRequest request) throws Exception;

    /**
     * Saves a file represented by a byte array to the file repository.
     *
     * @param bytes   The byte array representing the file content.
     * @param request File request information containing metadata of the file.
     * @throws Exception If an error occurs during file saving.
     */
    void saveFile(byte[] bytes, FileRequest request) throws Exception;

    /**
     * Saves a file represented by an input stream to the file repository.
     *
     * @param inputStream The input stream of the file content.
     * @param request     File request information containing metadata of the file.
     * @return The identifier of the saved file (usually the file path or a unique ID).
     * @throws Exception If an error occurs during file saving.
     */
    String saveFile(InputStream inputStream, FileRequest request) throws Exception;

    /**
     * Deletes a specified file.
     *
     * @param request File request information containing the identifier or path of the file to be deleted.
     * @throws Exception If an error occurs during file deletion.
     */
    void delete(FileRequest request) throws Exception;

    /**
     * Deletes a specified folder and all its contents.
     *
     * @param request File request information containing the identifier or path of the folder to be deleted.
     * @throws Exception If an error occurs during folder deletion.
     */
    void deleteFolder(FileRequest request) throws Exception;

    /**
     * Retrieves the byte content of a specified file. Not recommended for large files.
     *
     * @param request File request information containing the identifier or path of the file to be retrieved.
     * @return The byte array of the file content.
     * @throws Exception If an error occurs during file retrieval.
     */
    byte[] getFile(FileRequest request) throws Exception;

    /**
     * Retrieves the input stream of a specified file for streaming file content.
     *
     * @param request File request information containing the identifier or path of the file to be retrieved.
     * @return The input stream of the file content.
     * @throws Exception If an error occurs during file retrieval.
     */
    InputStream getFileAsStream(FileRequest request) throws Exception;

    /**
     * Downloads a file in a streaming manner, saving memory by downloading in chunks.
     *
     * @param request   File request information containing the identifier or path of the file to be downloaded.
     * @param localPath The local path to which the file will be downloaded.
     * @throws Exception If an error occurs during file download.
     */
    void downloadFile(FileRequest request, String localPath) throws Exception;

    /**
     * Retrieves a list of file names in a specified folder.
     *
     * @param request File request information containing the identifier or path of the target folder.
     * @return The list of file names in the folder.
     * @throws Exception If an error occurs during folder file list retrieval.
     */
    List<String> getFolderFileNames(FileRequest request) throws Exception;

    /**
     * Copies a file to a specified directory.
     *
     * @param request File copy request information containing source file and target directory information.
     * @throws Exception If an error occurs during file copy.
     */
    void copyFile(FileCopyRequest request) throws Exception;

    /**
     * Retrieves the size of a specified file (in bytes).
     *
     * @param request File request information containing the identifier or path of the file whose size is to be retrieved.
     * @return The size of the file in bytes.
     * @throws Exception If an error occurs during file size retrieval.
     */
    long getFileSize(FileRequest request) throws Exception;
}