package bank.effectivemobilebank.config;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;


@Component
public class CardNumberCipher {

    private static final String AES = "AES";
    private static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;

    private final byte[] secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public CardNumberCipher(@Value("${security.jwt.secret}") String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        byte[] normalized = new byte[32];
        System.arraycopy(keyBytes, 0, normalized, 0, Math.min(keyBytes.length, 32));
        this.secretKey = normalized;
    }

    public String encrypt(String plain) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
            SecretKeySpec keySpec = new SecretKeySpec(secretKey, AES);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
            byte[] ciphertext = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            ByteBuffer bb = ByteBuffer.allocate(iv.length + ciphertext.length);
            bb.put(iv).put(ciphertext);
            return Base64.getEncoder().encodeToString(bb.array());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encrypt PAN", e);
        }
    }

    public String decrypt(String encoded) {
        try {
            byte[] all = Base64.getDecoder().decode(encoded);
            ByteBuffer bb = ByteBuffer.wrap(all);
            byte[] iv = new byte[IV_LENGTH];
            bb.get(iv);
            byte[] ciphertext = new byte[bb.remaining()];
            bb.get(ciphertext);

            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
            SecretKeySpec keySpec = new SecretKeySpec(secretKey, AES);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            byte[] plain = cipher.doFinal(ciphertext);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to decrypt PAN", e);
        }
    }
}


