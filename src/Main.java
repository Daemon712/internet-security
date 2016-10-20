import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Main extends Application {
    private static final List<String> algorithms = Arrays.asList("DES", "DESEDE", "RIJNDAEL", "RC2");
    private static final List<String> modes = Arrays.asList("ECB", "CBC", "CFB", "OFB");
    private static final int repeats = 16;
    private BarChart<Number, String> barChart;

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Информационная безопасность. Практическая работа №2");

        BorderPane borderPane = new BorderPane();
        Button button = new Button("Выбрать файл");
        button.setOnMouseClicked(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File("."));
            File file = fileChooser.showOpenDialog(stage);
            if (file == null) return;
            try {
                byte[] value = Files.readAllBytes(Paths.get(file.getPath()));
                calculateData(value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        borderPane.setTop(button);
        initChart();
        borderPane.setCenter(barChart);
        Scene scene = new Scene(borderPane, 800, 600);
        stage.setScene(scene);
        stage.show();

    }

    private void initChart() throws Exception {
        NumberAxis xAxis = new NumberAxis();
        CategoryAxis yAxis = new CategoryAxis();
        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Сравнение производительности симметричных алгоритмов шифрования");
        xAxis.setLabel("Время (сек)");
        xAxis.setTickLabelRotation(90);
        yAxis.setLabel("Алгоритм");
        calculateData("Hello World!".getBytes());
    }

    public void calculateData(byte[] value) throws InterruptedException, ExecutionException {
        barChart.getData().clear();

        CompletableFuture[] futures = modes.stream()
                .map(algorithm -> getSeriesFuture(algorithm, value))
                .toArray(CompletableFuture[]::new);

        List<XYChart.Series<Number, String>> seriesList = CompletableFuture.allOf(futures)
                .thenApply(v -> Arrays.stream(futures)
                        .map(f -> (XYChart.Series<Number, String>) f.join())
                        .collect(Collectors.toList())).get();

        barChart.getData().addAll(seriesList);
    }

    private CompletableFuture<XYChart.Series<Number, String>> getSeriesFuture(String mode, byte[] value) {
        CompletableFuture[] futures = algorithms.stream()
                .map(algorithm -> getData(algorithm, mode, value))
                .map(CompletableFuture::supplyAsync)
                .toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(futures).thenApply(v -> {
            XYChart.Series<Number, String> series = new XYChart.Series<>();
            series.setName(mode);
            List<XYChart.Data<Number, String>> data = Arrays.stream(futures)
                    .map(f -> (XYChart.Data<Number, String>) f.join())
                    .collect(Collectors.toList());
            series.getData().addAll(data);
            return series;
        });
    }

    private Supplier<XYChart.Data<Number, String>> getData(String algorithm, String mode, byte[] value) {
        return () -> {
            SecretKey key = generateKey(algorithm);

            String a = algorithm + "/" + mode + "/PKCS5Padding";
            long startTime = System.nanoTime();

            for (int i = 0; i < repeats; i++) {
                EncryptUtils.testAlgorithm(a, key, value);
            }

            long endTime = System.nanoTime();
            float result = (endTime - startTime) / repeats / 1000000000f;

            return new XYChart.Data<>(result, algorithm);
        };
    }

    private SecretKey generateKey(String algorithm) {
        try {
            return KeyGenerator.getInstance(algorithm).generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
