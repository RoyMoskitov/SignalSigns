package informationtheory.signalsigns.Service;

import informationtheory.signalsigns.Model.SignalRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeSignalService {
    private static final Map<Character, Integer> timeMap = new HashMap<>();
    static {
        timeMap.put('0', 33);  // длительность сигнала для бита 0
        timeMap.put('1', 66);  // длительность сигнала для бита 1
    } // T = const, t_и = var, t_п =  (ВСП)

    public static List<Double> encodeTextTime(SignalRequest signalRequest) {
        String inputText = signalRequest.getInputText();
        if (inputText == null || inputText.isEmpty()) {
            throw new IllegalArgumentException("Input text cannot be null or empty.");
        }
        List<Double> encodedMessage = new ArrayList<>();

        for (char character : inputText.toCharArray()) {
            String binaryString = String.format("%8s", Integer.toBinaryString(character)).replace(' ', '0');
            for (char bit : binaryString.toCharArray()) {
                Integer sigtime = timeMap.get(bit);
                for (int i = 0; i < 100; i++) {  // В 1 секунду 10 шагов по 0.1 секунды
                    if (sigtime < i) encodedMessage.add(0.0);
                    else encodedMessage.add(1.0);
                }

            }
        }
        return encodedMessage.stream().mapToDouble(Double::doubleValue).boxed().toList();
    }
    
    public static String decodeTimeToText(List<Double> signal) {
        if (signal == null || signal.isEmpty()) {
            throw new IllegalArgumentException("Input signal cannot be null or empty.");
        }
        StringBuilder decodedText = new StringBuilder();
        StringBuilder binaryString = new StringBuilder();

        int samplesPerBit = 100;  // В каждом бите 100 отсчетов
        for (int i = 0; i < signal.size(); i += samplesPerBit) {
            // Рассчитываем период (интервал времени) для текущего бита
            double sum = 0;
            for (int j = i+34; j < i+67; j++) {
                sum += signal.get(j);
            }
            // Сравниваем с пороговыми значениями для определения 0 или 1
            if (sum > 16.5) {
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
