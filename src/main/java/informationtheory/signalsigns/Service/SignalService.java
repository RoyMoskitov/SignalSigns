package informationtheory.signalsigns.Service;

import informationtheory.signalsigns.Model.SignalRequest;
import informationtheory.signalsigns.Model.SignalResponse;
import informationtheory.signalsigns.Repository.SignalRequestRepository;
import informationtheory.signalsigns.Repository.SignalResponseRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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


}
