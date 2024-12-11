package io.demo.file.engine;

import lombok.Data;

/**
 * File copy request class, representing the request parameters for a file copy operation.
 * <p>
 * This class extends {@link FileRequest} and includes relevant information needed for copying files, such as the target directory and file name.
 * </p>
 */
@Data
public class FileCopyRequest extends FileRequest {

    /**
     * The target folder path for the copy operation.
     * <p>
     * This field indicates the directory to which the file should be copied. A valid directory path must be provided.
     * </p>
     */
    private String copyFolder;

    /**
     * The name of the copied file.
     * <p>
     * This field indicates the name of the file after it is copied. It can be the same as or different from the original file name.
     * </p>
     */
    private String copyfileName;
}