package com.wsproxy.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class FileUtils {

    public static byte[] getFileContent(String filePath) throws IOException {
        byte[] content = null;
        Path path = Paths.get(filePath);
        content = Files.readAllBytes(path);
        return content;
    }

    public static void putFileContent(String fileName, byte[] data) throws IOException {
        if ( fileName != null ) {
            FileOutputStream fos = new FileOutputStream(fileName);
            fos.write(data);
            fos.flush();
            fos.close();
        }
    }

}
