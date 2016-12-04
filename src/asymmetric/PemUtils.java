package asymmetric;

import java.util.Base64;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PemUtils {

    public static final int LINE_LEN = 64;

    public static String encode(byte[] bytes){
        String temp = Base64.getEncoder().encodeToString(bytes);
        return IntStream.rangeClosed(0, temp.length()/ LINE_LEN)
                .mapToObj(i -> temp.substring(i * LINE_LEN, Math.min((i + 1) * LINE_LEN, temp.length())))
                .collect(Collectors.joining("\n"));
    }

    public static byte[] decode(String string){
        return Base64.getDecoder().decode(string.replace("\n", ""));
    }
//
//    public static void main(String[] args) {
//        byte[] bytes = new byte[]{17, 12, 0, 1, 2, 123, -100, 0, 100, -128, 127, -1};
//        String encoded = encode(bytes);
//        byte[] decoded = decode(encoded);
//        System.out.println(Arrays.equals(bytes, decoded));
//    }
}
