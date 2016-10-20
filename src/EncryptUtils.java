import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.security.AlgorithmParameters;
import java.security.NoSuchAlgorithmException;

public class EncryptUtils {

    public static void testAlgorithm(String algorithm, SecretKey key, byte[] value) {
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(value);
            AlgorithmParameters parameters = cipher.getParameters();
            cipher.init(Cipher.DECRYPT_MODE, key, parameters);
            cipher.doFinal(encrypted);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("No Such Algorithm " + algorithm);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
