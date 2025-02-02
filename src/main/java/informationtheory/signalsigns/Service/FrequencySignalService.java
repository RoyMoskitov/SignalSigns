package informationtheory.signalsigns.Service;

import informationtheory.signalsigns.Model.SignalRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrequencySignalService {

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
}
