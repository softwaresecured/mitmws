package com.mitmws.pki;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public final class RSACrypto {

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    public static String sign(PrivateKey privateKey, byte[] data) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        String sigb64 = null;
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data);
        byte[] sigbytes = signature.sign();
        if ( sigbytes != null) {
            sigb64 = new String(Base64.getEncoder().encode(sigbytes));
        }
        return sigb64;
    }

    public static boolean verify(PublicKey publicKey, String sigb64, byte[] data) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(data);
        return signature.verify(Base64.getDecoder().decode(sigb64));
    }

    public static PrivateKey decodePrivateKeyB64( String privb64 ) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privb64));
        return kf.generatePrivate(keySpecPKCS8);
    }

    public static PublicKey decodePublicKeyB64( String pubb64 ) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory kf = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpecPKCS8 = new X509EncodedKeySpec(Base64.getDecoder().decode(pubb64));
        return kf.generatePublic(keySpecPKCS8);
    }
}
