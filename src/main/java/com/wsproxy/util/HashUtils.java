package com.wsproxy.util;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public final class HashUtils {
    public static String sha256sum(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(bytes);
        return new String(Base64.getEncoder().encode(hash));
    }

    public static String sha256sumFile( String filePath ) throws NoSuchAlgorithmException, IOException {
        byte[] fileContent = FileUtils.getFileContent(filePath);
        return sha256sum(fileContent);
    }

    public static String sha1sum(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] hash = digest.digest(bytes);
        return new String(Base64.getEncoder().encode(hash));
    }

    public static String md5sum(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] hash = digest.digest(bytes);
        return new String(Base64.getEncoder().encode(hash));
    }
}
