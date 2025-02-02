package informationtheory.signalsigns.Service;

import informationtheory.signalsigns.Model.SignalRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhaseSignalService {

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
}
