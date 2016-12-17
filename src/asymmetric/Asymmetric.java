package asymmetric;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import javax.crypto.Cipher;
import java.io.File;
import java.io.PrintWriter;
import java.security.*;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Scanner;

public class Asymmetric extends Application {
    private static final String ALGORITHM = "RSA";
    private PrivateKey privateKey;
    private PublicKey publicKey;

    @FXML
    private TextArea publicKeyText;

    @FXML
    private TextArea privateKeyText;

    @FXML
    private TextArea encryptedText;

    @FXML
    private TextArea decryptedText;

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("fxml.fxml"));
        Scene scene = new Scene(root, 960, 600);
        stage.setTitle("Asymmetric Algorithms");
        stage.setScene(scene);
        stage.show();
    }

    public void encrypt(MouseEvent event) throws Exception{
        if (publicKey == null){
            alert("Public key is undefined");
        } else {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            String text = decryptedText.getText();
            byte[] bytes = Arrays.copyOf(text.getBytes(), 117);
            byte[] encrypted = cipher.doFinal(bytes);
            decryptedText.setText("");
            encryptedText.setText(PemUtils.encode(encrypted));
        }
    }

    public void decrypt(MouseEvent event) throws Exception {
        if (privateKey == null){
            alert("Private key is undefined");
        } else {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] encrypted = PemUtils.decode(encryptedText.getText());
            String decrypted = new String(cipher.doFinal(encrypted));
            decryptedText.setText(decrypted);
            encryptedText.setText("");
        }
    }

    private void alert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void exportPrivate(MouseEvent event) throws Exception{
        if (privateKey == null){
            alert("Private key is undefined");
        } else {
            export(event, PemUtils.encode(privateKey.getEncoded()));
        }
    }

    private void export(MouseEvent event, String encode) throws Exception {
        FileChooser fileChooser = new FileChooser();
        Window window = ((Node) event.getTarget()).getScene().getWindow();
        File file = fileChooser.showSaveDialog(window);
        try (PrintWriter pw = new PrintWriter(file)) {
            pw.print(encode);
        }
    }

    public void importPrivate(MouseEvent event) throws Exception {
        FileChooser fileChooser = new FileChooser();
        Window window = ((Node) event.getTarget()).getScene().getWindow();
        File file = fileChooser.showOpenDialog(window);
        try (Scanner scanner = new Scanner(file)){
            byte[] bytes = PemUtils.decode(scanner.useDelimiter("\\A").next());
            privateKey = KeyFactory.getInstance(ALGORITHM).generatePrivate(new X509EncodedKeySpec(bytes));
            privateKeyText.setText(PemUtils.encode(privateKey.getEncoded()));
        }
    }

    public void exportPublic(MouseEvent event) throws Exception{
        if (publicKey == null){
            alert("Public key is undefined");
        } else {
            export(event, PemUtils.encode(publicKey.getEncoded()));
        }
    }

    public void importPublic(MouseEvent event) throws Exception{
        FileChooser fileChooser = new FileChooser();
        Window window = ((Node) event.getTarget()).getScene().getWindow();
        File file = fileChooser.showOpenDialog(window);
        try (Scanner scanner = new Scanner(file)){
            String text = scanner.useDelimiter("\\A").next();
            KeySpec keySpec = new X509EncodedKeySpec(PemUtils.decode(text));
            publicKey = KeyFactory.getInstance(ALGORITHM).generatePublic(keySpec);
            publicKeyText.setText(PemUtils.encode(publicKey.getEncoded()));
        }
    }

    public void generate(MouseEvent event) throws Exception{
        KeyPair keyPair = KeyPairGenerator.getInstance(ALGORITHM).generateKeyPair();
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();

        privateKeyText.setText(PemUtils.encode(privateKey.getEncoded()));
        publicKeyText.setText(PemUtils.encode(publicKey.getEncoded()));
    }
}
