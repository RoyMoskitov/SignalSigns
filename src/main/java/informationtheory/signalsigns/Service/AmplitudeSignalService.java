package informationtheory.signalsigns.Service;

import informationtheory.signalsigns.Model.SignalRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AmplitudeSignalService {

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
}
