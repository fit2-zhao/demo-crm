package io.demo.file.engine;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * File request class, encapsulating file-related request information.
 * <p>
 * This class is used to store basic information related to file operations, such as the folder where the file is located, storage type, and file name.
 * </p>
 */
@Data
@NoArgsConstructor
public class FileRequest {

    /**
     * The folder path where the file is located.
     * <p>
     * This field indicates the directory where the file is stored, usually used to locate the file.
     * </p>
     */
    private String folder;

    /**
     * Storage type, indicating the method or location of file storage.
     * <p>
     * For example, it can be an identifier for storage types such as "MINIO", "LOCAL", etc.
     * </p>
     */
    private String storage;

    /**
     * File name.
     * <p>
     * This field indicates the name of the file, usually combined with the file extension (e.g., .txt, .jpg) to determine the complete identifier of the file.
     * </p>
     */
    private String fileName;

    /**
     * Constructor, creating a file request object containing the folder path, storage type, and file name.
     *
     * @param folder   The folder path where the file is located.
     * @param storage  Storage type, indicating the method or location of file storage.
     * @param fileName The name of the file.
     */
    public FileRequest(String folder, String storage, String fileName) {
        this.folder = folder;
        this.storage = storage;
        this.fileName = fileName;
    }
}