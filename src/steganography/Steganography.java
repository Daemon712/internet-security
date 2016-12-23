package steganography;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;

public class Steganography extends Application {
    @FXML
    private ImageView inputImage;
    @FXML
    private ImageView outputImage;
    @FXML
    private TextField fileNameField;
    @FXML
    private TextField passwordField;

    private static final String SALT = "RAINBOW+";

    private File file;
    private BufferedImage image;

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("fxml.fxml"));
        Scene scene = new Scene(root, 1800, 600);
        stage.setTitle("Steganography");
        stage.setScene(scene);
        stage.show();
    }

    public void chooseImage(MouseEvent event) throws Exception {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg"));
        Window window = ((Node) event.getTarget()).getScene().getWindow();
        File file = fileChooser.showOpenDialog(window);
        if (file != null) {
            this.image = ImageIO.read(file);
            inputImage.setImage(new Image(file.toURI().toString()));
        }
    }

    public void chooseFile(MouseEvent event) throws Exception {
        FileChooser fileChooser = new FileChooser();
        Window window = ((Node) event.getTarget()).getScene().getWindow();
        this.file = fileChooser.showOpenDialog(window);
        if (file != null) {
            fileNameField.setText(file.getName());
        }
    }

    public void encode(MouseEvent event) throws Exception {
        if (file == null || image == null){
            alert("Please, select a file and an image first");
            return;
        }
        Path path = Paths.get(file.getPath());
        byte[] data = encode(Files.readAllBytes(path), passwordField.getText());

        if (data.length > image.getWidth() * image.getHeight()){
            alert("Image should be bigger for this file.\n" +
                    "Please, select another file or another image");
            return;
        }

        int[] stats = new int[Byte.MAX_VALUE - Byte.MIN_VALUE + 1];
        int n = 0;
        int[] pixel = new int[3];
        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth() & n < data.length; j++, n++) {
                image.getRaster().getPixel(j, i, pixel);
                pixel[0] = (pixel[0] & 0b11111000) | (data[n] & 0b11100000) >> 5;
                pixel[1] = (pixel[1] & 0b11111000) | (data[n] & 0b00011100) >> 2;
                pixel[2] = (pixel[2] & 0b11111100) | (data[n] & 0b00000011);
                image.getRaster().setPixel(j, i, pixel);
                stats[data[n] - Byte.MIN_VALUE]++;
            }
        }
        System.out.println(Arrays.toString(stats));
        outputImage.setImage(SwingFXUtils.toFXImage(image, null));
    }

    public void saveImage(MouseEvent event) throws Exception {
        if (outputImage.getImage() == null) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");
        Window window = ((Node) event.getTarget()).getScene().getWindow();
        File file = fileChooser.showSaveDialog(window);
        if (file != null) {
            ImageIO.write(image, "png", file);
        }
    }

    private static SecretKey generateKey(String password) throws Exception {
        byte[] key = (SALT + password).getBytes("UTF-8");
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16); // use only first 128 bit
        return new SecretKeySpec(key, "AES");
    }

    private static byte[] encode(byte[] decoded, String password) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, generateKey(password));
        return cipher.doFinal(decoded);
    }

    private static byte[] decode(byte[] encoded, String password) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, generateKey(password));
        return cipher.doFinal(encoded);
    }

    private static void alert(String message){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Alert");
        alert.setContentText(message);
        alert.showAndWait();
    }

}
