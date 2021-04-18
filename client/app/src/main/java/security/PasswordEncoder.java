package security;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static java.util.Objects.requireNonNull;

public interface PasswordEncoder {

    EncodedResult encodePassword(String password)
            throws InvalidKeySpecException, NoSuchAlgorithmException;

    EncodedResult encodePassword(String password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException;

    class EncodedResult {
        private final String password;
        private final byte[] salt;

        EncodedResult(String password, byte[] salt) {
            this.password = requireNonNull(password);
            this.salt = requireNonNull(salt);
        }

        public String getPassword() {
            return password;
        }

        public byte[] getSalt() {
            return salt;
        }
    }
}

