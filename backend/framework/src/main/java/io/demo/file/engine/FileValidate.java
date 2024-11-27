package io.demo.file.engine;

import io.demo.common.exception.GenericException;
import io.demo.common.util.LogUtils;
import io.demo.common.util.Translator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileValidate {

    /**
     * Validates the file names to ensure they do not contain illegal characters.
     *
     * @param fileNames The file names to validate.
     * @throws GenericException if any file name is invalid.
     */
    public static void validateFileName(String... fileNames) {
        if (fileNames != null) {
            for (String fileName : fileNames) {
                if (StringUtils.isNotBlank(fileName) && fileName.contains("." + File.separator)) {
                    throw new GenericException(Translator.get("invalid_parameter"));
                }
            }
        }
    }

    /**
     * Deletes the specified directory and its contents.
     *
     * @param path The path to the directory to delete.
     * @throws Exception If an error occurs while deleting the directory.
     */
    public static void deleteDir(String path) throws Exception {
        File file = new File(path);
        FileUtils.deleteDirectory(file);
    }

    /**
     * Converts an InputStream to a file.
     *
     * @param ins  The InputStream to convert.
     * @param file The file to write to.
     */
    private static void inputStreamToFile(InputStream ins, File file) {
        try (OutputStream os = new FileOutputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = ins.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            LogUtils.error(e);
        }
    }

    /**
     * Converts a MultipartFile to a File.
     *
     * @param file The MultipartFile to convert.
     * @return The resulting File.
     */
    public static File multipartFileToFile(MultipartFile file) {
        if (file != null && file.getSize() > 0) {
            try (InputStream ins = file.getInputStream()) {
                validateFileName(file.getOriginalFilename());
                File toFile = new File(FileUtils.getTempDirectoryPath() + File.separator + Objects.requireNonNull(file.getOriginalFilename()));
                inputStreamToFile(ins, toFile);
                return toFile;
            } catch (IOException e) {
                LogUtils.error(e);
            }
        }
        return null;
    }

    /**
     * Zips the files in a folder into a single zip file.
     *
     * @param rootPath  The root directory path where the folder is located.
     * @param zipFolder The folder to zip.
     * @return The zip file created.
     */
    public static File zipFile(String rootPath, String zipFolder) {
        File folder = new File(rootPath + File.separator + zipFolder);
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files == null || files.length == 0) {
                return null;
            }
            File zipFile = new File(rootPath + File.separator + zipFolder + ".zip");

            try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile))) {
                for (File file : files) {
                    String fileName = file.getName();
                    try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
                        zipOutputStream.putNextEntry(new ZipEntry(fileName));
                        byte[] buffer = new byte[512];
                        int num;
                        while ((num = bis.read(buffer)) > 0) {
                            zipOutputStream.write(buffer, 0, num);
                        }
                        zipOutputStream.closeEntry();
                    } catch (IOException ignored) {
                    }
                }
            } catch (IOException e) {
                LogUtils.error(e);
            }
            return zipFile;
        }
        return null;
    }

    /**
     * Unzips a file into a target directory.
     *
     * @param file       The zip file to unzip.
     * @param targetPath The directory to extract the files to.
     * @return An array of files extracted from the zip file.
     */
    public static File[] unZipFile(File file, String targetPath) {
        try (ZipInputStream zipInput = new ZipInputStream(new FileInputStream(file));
             ZipFile zipFile = new ZipFile(file)) {
            ZipEntry entry;
            while ((entry = zipInput.getNextEntry()) != null) {
                File outFile = new File(targetPath + File.separator + entry.getName());
                if (!outFile.getParentFile().exists()) {
                    outFile.getParentFile().mkdirs();
                }
                if (!outFile.exists()) {
                    outFile.createNewFile();
                }
                try (InputStream input = zipFile.getInputStream(entry);
                     OutputStream output = new FileOutputStream(outFile)) {
                    int temp;
                    while ((temp = input.read()) != -1) {
                        output.write(temp);
                    }
                }
            }
            File folder = new File(targetPath);
            return folder.isDirectory() ? folder.listFiles() : null;
        } catch (IOException e) {
            LogUtils.error(e);
        }
        return null;
    }
}
