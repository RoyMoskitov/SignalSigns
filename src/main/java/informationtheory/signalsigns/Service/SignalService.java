package informationtheory.signalsigns.Service;

import informationtheory.signalsigns.Model.SignalRequest;
import informationtheory.signalsigns.Model.SignalResponse;
import informationtheory.signalsigns.Repository.SignalRequestRepository;
import informationtheory.signalsigns.Repository.SignalResponseRepository;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SignalService {
    private final SignalRequestRepository signalRequestRepository;
    private final SignalResponseRepository signalResponseRepository;

    public SignalService(SignalRequestRepository signalRequestRepository, SignalResponseRepository signalResponseRepository) {
        this.signalRequestRepository = signalRequestRepository;
        this.signalResponseRepository = signalResponseRepository;
    }

    public SignalRequest saveSignalRequest(SignalRequest request) {
        return signalRequestRepository.save(request);
    }

    public SignalResponse generateSignalResponse(SignalRequest request) {
        SignalResponse response = new SignalResponse();

        switch (request.getType()){
            case POLAR -> {
                response.setEncodedText(request.getInputText());
                response.setEncodedSignal(encodeTextPolar(request));
                response.setNoisySignal(makeSignalNoisy(response.getEncodedSignal(), request));
                response.setDecodedText(decodePolarToText(response.getNoisySignal()));
            }
            case FREQUENCY -> {
                response.setEncodedText(request.getInputText());
                response.setEncodedSignal(encodeTextFrequency(request));
                response.setNoisySignal(makeSignalNoisy(response.getEncodedSignal(), request));
                response.setDecodedText(decodeFrequencyToText(restoreSignal(response.getNoisySignal(), 25)));
            }
            default -> throw new RuntimeException("This type of signal is not supported yet");
        }

        return signalResponseRepository.save(response);
    }


    private List<Double> makeSignalNoisy(List<Double> originalSignal, SignalRequest signalRequest) {
        int count = 0;
        List<Double> noisySignal = new ArrayList<>();

        for (int i = 0; i < originalSignal.size(); i++) {
            double el = originalSignal.get(i);

            if (count >= signalRequest.getMaxErrors()) {
                noisySignal.addAll(originalSignal.subList(i, originalSignal.size()));
                break;
            }

            if (Math.random() <= signalRequest.getNoiseIntensity()) {
                noisySignal.add(el + Math.random() * signalRequest.getMaxErrorValue());
                count++;
            } else {
                noisySignal.add(el);
            }
        }

        return noisySignal;
    }


    // Кодируем текст в массив +1 и -1
    private List<Double> encodeTextPolar(SignalRequest signalRequest) {
        String inputText = signalRequest.getInputText();
        if (inputText == null || inputText.isEmpty()) {
            throw new IllegalArgumentException("Input text cannot be null or empty.");
        }

        List<Double> encodedList = new ArrayList<>();

        for (char c : inputText.toCharArray()) {
            // Получаем бинарное представление символа (8 бит)
            String binaryString = String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0');

            // Преобразуем каждый бит: 0 -> -1, 1 -> +1
            for (char bit : binaryString.toCharArray()) {
                encodedList.add(bit == '0' ? -1.0 : 1.0);
            }
        }

        // Конвертируем List<Double> в массив double[]
        return encodedList.stream().mapToDouble(Double::doubleValue).boxed().toList();
    }

    // Восстанавливаем текст из массива +1 и -1
    private String decodePolarToText(List<Double> encodedSignal) {
        if (encodedSignal == null || encodedSignal.size() % 8 != 0) {
            throw new IllegalArgumentException("Invalid encoded signal length. Must be a multiple of 8.");
        }

        StringBuilder decodedText = new StringBuilder();

        for (int i = 0; i < encodedSignal.size(); i += 8) {
            StringBuilder binaryString = new StringBuilder();

            // Обрабатываем 8 бит
            for (int j = i; j < i + 8; j++) {
                double value = encodedSignal.get(j);

                // Преобразуем зашумленный сигнал в четкое значение
                if (value > 0) {
                    binaryString.append('1'); // Ближайшее к +1
                } else if (value < 0) {
                    binaryString.append('0'); // Ближайшее к -1
                } else {
                    // Если значение ровно 0, выбираем случайно
                    binaryString.append(Math.random() < 0.5 ? '0' : '1');
                }
            }

            // Преобразуем строку бита в символ
            int asciiCode = Integer.parseInt(binaryString.toString(), 2);
            decodedText.append((char) asciiCode);
        }

        return decodedText.toString();
    }

    // Карта для отображения бита в соответствующую частоту
    private static final Map<Character, Double> frequencyMap = new HashMap<>();
    static {
        // Для простоты используем 2 частоты:
        // Низкая частота для бита 0 (1 Гц)
        // Высокая частота для бита 1 (2 Гц)
        frequencyMap.put('0', 1.0);  // частота для бита 0
        frequencyMap.put('1', 2.0);  // частота для бита 1
    }

    // Метод для кодирования строки ЧСП
    public static List<Double> encodeTextFrequency(SignalRequest signalRequest) {
        String inputText = signalRequest.getInputText();
        if (inputText == null || inputText.isEmpty()) {
            throw new IllegalArgumentException("Input text cannot be null or empty.");
        }
        List<Double> encodedMessage = new ArrayList<>();

        for (char character : inputText.toCharArray()) {
            String binaryString = String.format("%8s", Integer.toBinaryString(character)).replace(' ', '0');
            for (char bit : binaryString.toCharArray()) {
                Double freq = frequencyMap.get(bit);
                for (int i = 0; i < 100; i++) {  // В 1 секунду 100 шагов по 0.01 секунды
                    double t = i * 0.01;  // Время для этого шага
                    double w = 2 * Math.PI * freq; // Угловая частота
                    double amplitude = Math.sin(w * t);
                    encodedMessage.add(amplitude);
                }
                // сдесь должна быть функция
            }
        }
        return encodedMessage.stream().mapToDouble(Double::doubleValue).boxed().toList();
    }

    public static List<Double> restoreSignal(List<Double> noisySignal, int windowSize) {
        List<Double> restoredSignal = new ArrayList<>();

        for (int i = 0; i < noisySignal.size(); i++) {
            // Вычисляем среднее значение в окне
            int start = Math.max(0, i - windowSize / 2);
            int end = Math.min(noisySignal.size() - 1, i + windowSize / 2);
            double sum = 0.0;
            int count = 0;

            for (int j = start; j <= end; j++) {
                sum += noisySignal.get(j);
                count++;
            }

            // Среднее значение
            restoredSignal.add(sum / count);
        }

        return restoredSignal;
    }

    public static String decodeFrequencyToText(List<Double> signal) {
        StringBuilder decodedText = new StringBuilder();
        StringBuilder binaryString = new StringBuilder();
        int samplesPerBit = 100;  // В каждом бите 100 отсчетов
        for (int i = 0; i < signal.size(); i += samplesPerBit) {
            // Рассчитываем период (интервал времени) для текущего бита
            double sumMax = 0;
            int f = 0;
            for (int j = i; j < i+100; j++) {
                if(signal.get(j) > 0.4 & f == 0){
                    sumMax += 1;
                    f = 1;
                }
                if(signal.get(j) < 0.4 & f == 1){
                    f = 0;
                }
            }
            // Сравниваем с пороговыми значениями для определения 0 или 1
            if (sumMax > 1) {
                binaryString.append("1");
            } else {
                binaryString.append("0");
            }

            // После накопления 8 битов восстанавливаем один символ
            if (binaryString.length() == 8) {
                String byteString = binaryString.toString();
                char decodedChar = (char) Integer.parseInt(byteString, 2);
                decodedText.append(decodedChar);
                binaryString.setLength(0);  // Очищаем строку для следующего байта
            }
        }

        return decodedText.toString();
    }


/*    public static void visualizeSignal(List<Double> encodedMessage) {
        // Создаем серию для графика
        XYSeries series = new XYSeries("Encoded Signal");

        // Добавляем данные в серию (время t и соответствующее значение амплитуды)
        for (int i = 0; i < encodedMessage.size(); i++) {
            double time = i * 0.01;  // Время шагами по 0.1 секунды
            double amplitude = encodedMessage.get(i);
            series.add(time, amplitude);
        }

        // Создаем коллекцию для отображения серии
        XYSeriesCollection dataset = new XYSeriesCollection(series);

        // Создаем график
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Signal Visualization", // Заголовок
                "Time (s)",            // Ось X
                "Amplitude",           // Ось Y
                dataset,               // Данные
                PlotOrientation.VERTICAL,
                true,                  // Легенда
                true,                  // Подсказки
                false                  // URL-обработчики
        );

        // Настройка панели для отображения графика
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));

        // Создаем окно для отображения графика
        JFrame frame = new JFrame("Signal Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(chartPanel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }*/

}
