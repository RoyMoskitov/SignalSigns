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

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import java.awt.image.BufferedImage;

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

    public static String visualizeSignal(List<Double> encodedMessage, String signalType) {
        // Создаем серию для графика
        XYSeries series = new XYSeries("Encoded Signal");

        // Добавляем данные в серию (время t и соответствующее значение амплитуды)
        for (int i = 0; i < encodedMessage.size(); i++) {
            double time = i * 0.01;  // Время шагами по 0.01 секунды
            double amplitude = encodedMessage.get(i);
            series.add(time, amplitude);
        }

        // Создаем коллекцию для отображения серии
        XYSeriesCollection dataset = new XYSeriesCollection(series);

        // Создаем график
        JFreeChart chart = ChartFactory.createXYLineChart(
                signalType,              // Заголовок
                "Time (s)",             // Ось X
                "Amplitude",            // Ось Y
                dataset,                // Данные
                PlotOrientation.VERTICAL,
                true,                   // Легенда
                true,                   // Подсказки
                false                   // URL-обработчики
        );

        // Создаем изображение из графика
        BufferedImage chartImage = chart.createBufferedImage(800, 600);

        // Сохраняем изображение в поток
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(chartImage, "png", baos);
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Конвертируем поток в Base64
        byte[] imageBytes = baos.toByteArray();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        // Закрываем поток
        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Возвращаем строку Base64 с префиксом для изображения
        return base64Image;
    }

    public static String convertToChartData(List<Double> signalData) {
        List<Integer> xData = IntStream.range(0, signalData.size()).boxed().collect(Collectors.toList());
        return "[" + xData.stream().map(String::valueOf).collect(Collectors.joining(",")) + "],[" +
                signalData.stream().map(String::valueOf).collect(Collectors.joining(",")) + "]";
    }
}
