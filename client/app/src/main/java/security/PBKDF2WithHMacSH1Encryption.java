package security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import static java.util.Objects.requireNonNull;

public class PBKDF2WithHMacSH1Encryption implements PasswordEncoder {
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 128;

    @Override
    public EncodedResult encodePassword(String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
        requireNonNull(password);
        return encodePassword(password, generateSalt());
    }

    @Override
    public EncodedResult encodePassword(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        requireNonNull(password);

        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHMacSHA1");
        byte[] encodedPassword = factory.generateSecret(keySpec).getEncoded();

        return new EncodedResult(Arrays.toString(encodedPassword), generateSalt());
    }

    private static byte[] generateSalt() {
        SecureRandom randomise = new SecureRandom();
        byte[] salt = new byte[16];
        randomise.nextBytes(salt);

        return salt;
    }
}





