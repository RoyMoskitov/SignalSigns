package informationtheory.signalsigns.Service;

import informationtheory.signalsigns.Model.SignalRequest;
import informationtheory.signalsigns.Model.SignalResponse;
import informationtheory.signalsigns.Repository.SignalRequestRepository;
import informationtheory.signalsigns.Repository.SignalResponseRepository;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

import static informationtheory.signalsigns.Service.AmplitudeSignalService.decodeAmplitudeToText;
import static informationtheory.signalsigns.Service.AmplitudeSignalService.encodeTextAmplitude;
import static informationtheory.signalsigns.Service.FrequencySignalService.*;
import static informationtheory.signalsigns.Service.PhaseSignalService.decodePhaseToText;
import static informationtheory.signalsigns.Service.PhaseSignalService.encodeTextPhase;
import static informationtheory.signalsigns.Service.PolarSignalService.decodePolarToText;
import static informationtheory.signalsigns.Service.PolarSignalService.encodeTextPolar;
import static informationtheory.signalsigns.Service.TimeSignalService.decodeTimeToText;
import static informationtheory.signalsigns.Service.TimeSignalService.encodeTextTime;

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
            case TIME -> {
                response.setEncodedText(request.getInputText());
                response.setEncodedSignal(encodeTextTime(request));
                response.setNoisySignal(makeSignalNoisy(response.getEncodedSignal(), request));
                response.setDecodedText(decodeTimeToText(response.getNoisySignal()));
            }
            default -> throw new RuntimeException("This type of signal is not supported yet");
        }

        return signalResponseRepository.save(response);
    }


    private List<Double> makeSignalNoisy(List<Double> originalSignal, SignalRequest signalRequest) {
        List<Double> noisySignal = new ArrayList<>();

        for (double el : originalSignal) {
            if (Math.random() <= signalRequest.getNoiseIntensity()) {
                noisySignal.add(el + (Math.random() * signalRequest.getMaxErrorValue() * (Math.random() > 0.5 ? -1 : 1)));
            } else {
                noisySignal.add(el);
            }
        }

        return noisySignal;
    }



}
