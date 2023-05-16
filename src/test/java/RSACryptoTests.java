import com.mitmws.pki.RSACrypto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.*;

public class RSACryptoTests {
    String privateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCAkTN5OtrkVP3fOpBysPeiN/CMUi18iHjvL+a6MTiu5kA+SJ2eISGty5uhzZ3pDXUY5gfTOmiG53+iBBOlIllXDV0BWh1jAN41T8dUEQd3E6lm2XsDAURg3xHtQhjCIItBClEgBtJ03aLura6pOx9KpmGejnwJZgP0NvbUgwJo/xlTkUoNg423xTUFzJcXdYd8EtbEv1PUyalBelUSkXBTm9XmMJT7YciB17/Wb6SEQEw42OJEhD906fI5Ly3sT8X2DTG1jJjskr9M7HUz8Bc5wYlPBl7qdqpmgUZRWyakxHtOkLsq09fRXnpThrJQf4hICuR+3BCRSTuoNN0Pls4vAgMBAAECggEAc73EABQ68KmO7wJy/b1RAYmolTp51o2piXKvHNmo9NM17JxEwRGEl0ggMJbB3QePbj3Pt5nyddg8b6eJ4/S7RwIxw0DUCcfDQ20xvf2iodFihMOu2TKv0yGequABF+piUsgt5k4d8rWAs6xJ+/HMptCIO0x8X52mW5mLth7G8avGDRx0Nso6bLyP25a8ty1GlsKyRmiwxNth6zzpYAl14i/2iV1R9FfNpJz92UmFFx3yiCtW1wq1uhg18aBvr1ehKArwfh5LDXq6/3dwJG8XBLNkzeD2G/29UiTlKMPCH+rbNzawG6eD2jDgfslsJuEcLT+yWIJqBXKtKxTYGTClOQKBgQDD6ffF6Uct7ExkUYCv5pyGRDWchgnLoWd6BAF+Ii3WTRAmSI/dEptL+i58X8TKgqC65yYghmVx1pukNK0PpIhz75EMV/W/8oJgqJt6Zzo8N8LLAigxW5S2iL89bgyAUyyksDDiRvmTUIkwfLHTw9TXzzgeNWZu/RkuR5fAaVjKBQKBgQCn/44mmV7/um65cAcMs8vNUZAriPw4BXDhw3nRTGh833tWbwXKfrMQnId/08lJ1qLYXKwb/THeuUpuCCj9BcEPsy7uHJ39KUPgZoa5lY0gjP63ExXvVPlnYqzihOTggcLVX39fmDYi4Hiy29xgTInoU7vPR+p0ILoKTm/2l2QJowKBgQCZdwhRLthEH+sDVljQ8XvBLWM1lkXMDkYpbUPiE1IGod05r/OJwE6IKJULdlWIMOVJI1JZfg2vK+ZFrcG3FDKTZqEfozNtRcFdiBYqvvv+OcvMDIeBinSmu93ad/8w8nZxF1djmSddf2PH6JXABZIP71HNUBVKcdasywYTgjI8JQKBgHvqUBvAfQX7qbg2Hb0M0YK8aq7x97gKq1ybJrtutJOG9B6o/YiDM+lIy9lJietg2fTlRj3O4H38Bh5q/nDDUcgWzsLrcM1PJrXaaf1xhgtAdJDtDvzdnKjZa9QZWgkPLXrdWcJVL3lCQUUMTtzpF7+6I4mc7h6CFTeM66DjwfIpAoGAXlGbQg332mUUQzhNiEy6NTydZSuu9APrQFjfZw3gEy9fbvxSNbt/YLFV5+9ST8VxvaFQECjLdspQdbNjiw70waYxpNseeQ0Fcat+5U3XTFV7D12aLb/e3t/EhJA1HdcpttXrW6imMGCv77zu49hsa/IxptYVylUQG+Zl+xrE+rw=";
    String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgJEzeTra5FT93zqQcrD3ojfwjFItfIh47y/mujE4ruZAPkidniEhrcuboc2d6Q11GOYH0zpohud/ogQTpSJZVw1dAVodYwDeNU/HVBEHdxOpZtl7AwFEYN8R7UIYwiCLQQpRIAbSdN2i7q2uqTsfSqZhno58CWYD9Db21IMCaP8ZU5FKDYONt8U1BcyXF3WHfBLWxL9T1MmpQXpVEpFwU5vV5jCU+2HIgde/1m+khEBMONjiRIQ/dOnyOS8t7E/F9g0xtYyY7JK/TOx1M/AXOcGJTwZe6naqZoFGUVsmpMR7TpC7KtPX0V56U4ayUH+ISArkftwQkUk7qDTdD5bOLwIDAQAB";
    @Test
    @DisplayName("Generate keypair")
    public void testGenerateKeyPair() throws NoSuchAlgorithmException {
        KeyPair keyPair = RSACrypto.generateKeyPair();
        assertTrue(keyPair.getPublic().toString().startsWith("Sun RSA public key, 2048 bits"));
    }

    @Test
    @DisplayName("Sign data")
    public void testSignData() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, InvalidKeySpecException {
        String sig = "LSa+vbpfjrGd5rCdn3sn/tTeq1go5QqKS4s4KQzK0U0zdfJ/WoI7612oswdi0KXWx/VdVlAUg05VC0enougr39H6p0Xh7GfzgB6jmJRWWzLvJWDvaxgpDLrzDmb/KBGcnesHV7i/7SkWV/n6ikmqmjDlIgeDVRqWCttNNP9Sp/Kq2Y9Z2oaQFgiBvI2rAPZ92DZ+aiuWd2K0dtrYJtBsZ+JSoLrtjeHZvYeWD/5KK8hEvDf4jQIpNbx4NL4TwYra0qXYeh2i/1hjnFoZD5E6P2/Feb6bm+bQ2XOiK0XdYaBgJ1dNbY7Z4vMlRGUNnnGGMjMZEH0+LJAoBviWcw5frA==";
        assertEquals(sig,RSACrypto.sign(RSACrypto.decodePrivateKeyB64(privateKey),"Websockets are fun!".getBytes(StandardCharsets.UTF_8)));
    }
    
    @Test
    @DisplayName("Verify data")
    public void testVerifyData() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, InvalidKeySpecException {
        String sig = "LSa+vbpfjrGd5rCdn3sn/tTeq1go5QqKS4s4KQzK0U0zdfJ/WoI7612oswdi0KXWx/VdVlAUg05VC0enougr39H6p0Xh7GfzgB6jmJRWWzLvJWDvaxgpDLrzDmb/KBGcnesHV7i/7SkWV/n6ikmqmjDlIgeDVRqWCttNNP9Sp/Kq2Y9Z2oaQFgiBvI2rAPZ92DZ+aiuWd2K0dtrYJtBsZ+JSoLrtjeHZvYeWD/5KK8hEvDf4jQIpNbx4NL4TwYra0qXYeh2i/1hjnFoZD5E6P2/Feb6bm+bQ2XOiK0XdYaBgJ1dNbY7Z4vMlRGUNnnGGMjMZEH0+LJAoBviWcw5frA==";
        assertTrue(RSACrypto.verify(RSACrypto.decodePublicKeyB64(publicKey),sig,"Websockets are fun!".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("Verify data - false")
    public void testVerifyDataFalse() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, InvalidKeySpecException {
        String sig = "LSa+vbpfjrGd5rCdn3sn/tTeq1go5QqKS4s4KQzK0U0zdfJ/WoI7612oswdi0KXWx/VdVlAUg05VC0enougr39H6p0Xh7GfzgB6jmJRWWzLvJWDvaxgpDLrzDmb/KBGcnesHV7i/7SkWV/n6ikmqmjDlIgeDVRqWCttNNP9Sp/Kq2Y9Z2oaQFgiBvI2rAPZ92DZ+aiuWd2K0dtrYJtBsZ+JSoLrtjeHZvYeWD/5KK8hEvDf4jQIpNbx4NL4TwYra0qXYeh2i/1hjnFoZD5E6P2/Feb6bm+bQ2XOiK0XdYaBgJ1dNbY7Z4vMlRGUNnnGGMjMZEH0+LJAoBviWcw5frA==";
        assertFalse(RSACrypto.verify(RSACrypto.decodePublicKeyB64(publicKey),sig,"Websockets ARE fun!".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("Verify data with bad sig")
    public void testVerifyDataWithBadSig() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, InvalidKeySpecException {
        String sig = "AAA+vbpfjrGd5rCdn3sn/tTeq1go5QqKS4s4KQzK0U0zdfJ/WoI7612oswdi0KXWx/VdVlAUg05VC0enougr39H6p0Xh7GfzgB6jmJRWWzLvJWDvaxgpDLrzDmb/KBGcnesHV7i/7SkWV/n6ikmqmjDlIgeDVRqWCttNNP9Sp/Kq2Y9Z2oaQFgiBvI2rAPZ92DZ+aiuWd2K0dtrYJtBsZ+JSoLrtjeHZvYeWD/5KK8hEvDf4jQIpNbx4NL4TwYra0qXYeh2i/1hjnFoZD5E6P2/Feb6bm+bQ2XOiK0XdYaBgJ1dNbY7Z4vMlRGUNnnGGMjMZEH0+LJAoBviWcw5frA==";
        assertFalse(RSACrypto.verify(RSACrypto.decodePublicKeyB64(publicKey),sig,"Websockets are fun!".getBytes(StandardCharsets.UTF_8)));
    }

}
