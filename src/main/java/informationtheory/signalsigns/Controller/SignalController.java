package informationtheory.signalsigns.Controller;

import informationtheory.signalsigns.Model.SignalRequest;
import informationtheory.signalsigns.Model.SignalResponse;
import informationtheory.signalsigns.Model.SignalSignType;
import informationtheory.signalsigns.Service.SignalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/")
public class SignalController {
    private final SignalService signalService;

    public SignalController(SignalService signalService) {
        this.signalService = signalService;
    }

    @GetMapping
    public String showRequestForm(Model model) {
        model.addAttribute("signalRequest", new SignalRequest());
        model.addAttribute("SignalSignType", SignalSignType.class);
        return "request-form";
    }

    @PostMapping("/process")
    public String processRequest(@ModelAttribute SignalRequest signalRequest, Model model) throws IOException {
        SignalResponse signalResponse = signalService.generateSignalResponse(signalRequest);

        // Генерация данных для графиков
        List<Double> noisyY = signalResponse.getNoisySignal();
        List<Double> noisyX = IntStream.range(0, noisyY.size())
                .mapToDouble(i -> i * 0.01)
                .boxed()
                .collect(Collectors.toList());

        List<Double> encodedY = signalResponse.getEncodedSignal();
        List<Double> encodedX = IntStream.range(0, encodedY.size())
                .mapToDouble(i -> i * 0.01)
                .boxed()
                .collect(Collectors.toList());

        model.addAttribute("noisyX", noisyX);
        model.addAttribute("noisyY", noisyY);
        model.addAttribute("encodedX", encodedX);
        model.addAttribute("encodedY", encodedY);
        model.addAttribute("signalResponse", signalResponse);

        return "response-view";
    }
}
