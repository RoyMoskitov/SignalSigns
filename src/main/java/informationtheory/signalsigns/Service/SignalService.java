package informationtheory.signalsigns.Service;

import informationtheory.signalsigns.Model.SignalRequest;
import informationtheory.signalsigns.Model.SignalResponse;
import informationtheory.signalsigns.Repository.SignalRequestRepository;
import informationtheory.signalsigns.Repository.SignalResponseRepository;
import org.springframework.stereotype.Service;

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
                response.setDecodedText(decodeFrequencyToText(restoreFrequencySignal(response.getNoisySignal(), 20)));
            }
            case AMPLITUDE -> {
                response.setEncodedText(request.getInputText());
                response.setEncodedSignal(encodeTextAmplitude(request));
                response.setNoisySignal(makeSignalNoisy(response.getEncodedSignal(), request));
                response.setDecodedText(decodeAmplitudeToText(response.getNoisySignal()));
            }
            case PHASE -> {
                response.setEncodedText(request.getInputText());
                response.setEncodedSignal(encodeTextPhase(request));
                response.setNoisySignal(makeSignalNoisy(response.getEncodedSignal(), request));
                response.setDecodedText(decodePhaseToText(response.getNoisySignal()));
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
                noisySignal.add(el + (Math.random() * signalRequest.getMaxErrorValue() * (Math.random() > 0.5 ? -1 : 1)));
                count++;
            } else {
                noisySignal.add(el);
            }
        }

        return noisySignal;
    }


    private static final Map<Character, Double> polarMap = new HashMap<>();
    static {
        polarMap.put('0', -1.0);  // частота для бита 0
        polarMap.put('1', 1.0);  // частота для бита 1
    }// Метод для кодирования строки ПСП
    private List<Double> encodeTextPolar(SignalRequest signalRequest) {
        String inputText = signalRequest.getInputText();
        if (inputText == null || inputText.isEmpty()) {
            throw new IllegalArgumentException("Input text cannot be null or empty.");
        }
        List<Double> encodedMessage = new ArrayList<>();

        for (char c : inputText.toCharArray()) {
            // Получаем бинарное представление символа (8 бит)
            String binaryString = String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0');

            // Преобразуем каждый бит: 0 -> -1.0, 1 -> 1.0
            for (char bit : binaryString.toCharArray()) {
                Double amplitude = polarMap.get(bit);
                for (int i = 0; i < 100; i++) {  // В 1 секунду(бит) 100 шагов по 0.01 секунды
                    encodedMessage.add(amplitude);
                }
            }
        }
        return encodedMessage.stream().mapToDouble(Double::doubleValue).boxed().toList();
    }

    // Восстанавливаем текст из массива +1 и -1
    private String decodePolarToText(List<Double> signal) {
        if (signal == null || signal.isEmpty()) {
            throw new IllegalArgumentException("Input signal cannot be null or empty.");
        }
        StringBuilder decodedText = new StringBuilder();
        StringBuilder binaryString = new StringBuilder();

        int samplesPerBit = 100;  // В каждом бите 100 отсчетов
        for (int i = 0; i < signal.size(); i += samplesPerBit) {
            // Рассчитываем период (интервал времени) для текущего бита
            double sum = 0;
            for (int j = i; j < i+100; j++) {
                sum += signal.get(j);
            }
            // Сравниваем с пороговыми значениями для определения 0 или 1
            if (sum/samplesPerBit > 0.0) {
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


    private static final Map<Character, Double> frequencyMap = new HashMap<>();
    static {
        frequencyMap.put('0', 1.0);  // частота для бита 0
        frequencyMap.put('1', 2.0);  // частота для бита 1
    }// Метод для кодирования строки ЧСП
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

    public static List<Double> restoreFrequencySignal(List<Double> noisySignal, int windowSize) {
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
            restoredSignal.add(2*sum / count);
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
                    f = 1;
                }
                if(signal.get(j) < -0.4 & f == 1){
                    sumMax += 1;
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


    private static final Map<Character, Double> amplitudeMap = new HashMap<>();
    static {
        amplitudeMap.put('0', 0.0);  // амплитуда для бита 0
        amplitudeMap.put('1', 2.0);  // амплитуда для бита 1
    }// Метод для кодирования строки АСП
    public static List<Double> encodeTextAmplitude(SignalRequest signalRequest) {
        String inputText = signalRequest.getInputText();
        if (inputText == null || inputText.isEmpty()) {
            throw new IllegalArgumentException("Input text cannot be null or empty.");
        }
        List<Double> encodedMessage = new ArrayList<>();

        for (char c : inputText.toCharArray()) {
            // Получаем бинарное представление символа (8 бит)
            String binaryString = String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0');

            // Преобразуем каждый бит: 0 -> 0.0, 1 -> 2.0
            for (char bit : binaryString.toCharArray()) {
                Double amplitude = amplitudeMap.get(bit);
                for (int i = 0; i < 100; i++) {  // В 1 секунду(бит) 100 шагов по 0.01 секунды
                    encodedMessage.add(amplitude);
                }
            }
        }
        return encodedMessage.stream().mapToDouble(Double::doubleValue).boxed().toList();
    }

    public static String decodeAmplitudeToText(List<Double> signal) {
        if (signal == null || signal.isEmpty()) {
            throw new IllegalArgumentException("Input signal cannot be null or empty.");
        }
        StringBuilder decodedText = new StringBuilder();
        StringBuilder binaryString = new StringBuilder();

        int samplesPerBit = 100;  // В каждом бите 100 отсчетов
        for (int i = 0; i < signal.size(); i += samplesPerBit) {
            // Рассчитываем период (интервал времени) для текущего бита
            double sum = 0;
            for (int j = i; j < i+100; j++) {
                sum += signal.get(j);
            }
            // Сравниваем с пороговыми значениями для определения 0 или 1
            if (sum/samplesPerBit > 1.0) {
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

    private static final Map<Character, Double> phaseMap = new HashMap<>();
    static {
        phaseMap.put('0', 0.0);  // фаза для бита 0
        phaseMap.put('1', 1.0);  // фаза для бита 1
    }// Метод для кодирования строки ФСП
    public static List<Double> encodeTextPhase(SignalRequest signalRequest) {
        String inputText = signalRequest.getInputText();
        if (inputText == null || inputText.isEmpty()) {
            throw new IllegalArgumentException("Input text cannot be null or empty.");
        }
        List<Double> encodedMessage = new ArrayList<>();

        for (char character : inputText.toCharArray()) {
            String binaryString = String.format("%8s", Integer.toBinaryString(character)).replace(' ', '0');
            for (char bit : binaryString.toCharArray()) {
                Double phase = phaseMap.get(bit);
                for (int i = 0; i < 100; i++) {  // В 1 секунду 10 шагов по 0.1 секунды
                    double t = i * 0.01;  // Время для этого шага
                    double w = 3 * Math.PI; // Угловая частота
                    double amplitude = Math.sin(w * t + phase * Math.PI);
                    encodedMessage.add(amplitude);
                }
            }
        }
        return encodedMessage.stream().mapToDouble(Double::doubleValue).boxed().toList();
    }

    public static String decodePhaseToText(List<Double> signal) {
        if (signal == null || signal.isEmpty()) {
            throw new IllegalArgumentException("Input signal cannot be null or empty.");
        }
        StringBuilder decodedText = new StringBuilder();
        StringBuilder binaryString = new StringBuilder();

        int samplesPerBit = 100;  // В каждом бите 100 отсчетов
        for (int i = 0; i < signal.size(); i += samplesPerBit) {
            // Рассчитываем период (интервал времени) для текущего бита
            double sum = 0;
            for (int j = i; j < i+33; j++) {
                sum += signal.get(j);
            }
            for (int j = i+67; j < i+100; j++) {
                sum += signal.get(j);
            }
            // Сравниваем с пороговыми значениями для определения 0 или 1
            if (sum/samplesPerBit > 0.0) {
                binaryString.append("0");
            } else {
                binaryString.append("1");
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
