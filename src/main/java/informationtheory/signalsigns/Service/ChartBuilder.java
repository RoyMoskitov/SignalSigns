package informationtheory.signalsigns.Service;

import org.knowm.xchart.*;
import org.knowm.xchart.style.markers.None;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.Base64;

public class ChartBuilder {

    public static String generateChartBase64(List<Double> yData) throws IOException {
        List<Integer> xData = IntStream.range(0, yData.size()).boxed().collect(Collectors.toList());

        CategoryChart chart = new CategoryChartBuilder()
                .width(800)
                .height(600)
                .title("Signal Graph")
                .xAxisTitle("Index")
                .yAxisTitle("Value")
                .build();

        // Настройка стиля
        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setHasAnnotations(false);
        chart.getStyler().setXAxisTicksVisible(true);
        chart.getStyler().setPlotGridLinesVisible(false);

        // Добавляем данные
        chart.addSeries("Signal", xData, yData);

        // Сохранение графика в поток и конвертация в Base64
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BitmapEncoder.saveBitmap(chart, byteArrayOutputStream, BitmapEncoder.BitmapFormat.PNG);

        byte[] chartBytes = byteArrayOutputStream.toByteArray();
        return Base64.getEncoder().encodeToString(chartBytes);
    }
}
