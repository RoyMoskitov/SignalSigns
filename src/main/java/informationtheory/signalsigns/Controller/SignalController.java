package informationtheory.signalsigns.Controller;

import informationtheory.signalsigns.Model.SignalRequest;
import informationtheory.signalsigns.Model.SignalResponse;
import informationtheory.signalsigns.Model.SignalSignType;
import informationtheory.signalsigns.Service.SignalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/")
@SessionAttributes({"pageSettings", "responseMade", "responseSignal",
        "signalResponse", "noisyX", "noisyY", "encodedX", "encodedY"})
public class SignalController {
    private final SignalService signalService;

    public SignalController(SignalService signalService) {
        this.signalService = signalService;
    }

    @ModelAttribute("pageSettings")
    public SignalRequest setupRequest() {
        return new SignalRequest();
    }

    @ModelAttribute("responseMade")
    public Boolean setupResponseFlag() {
        return false;
    }


    @GetMapping
    public String showRequestForm(@ModelAttribute("pageSettings") SignalRequest signalRequest,
                                  @ModelAttribute("responseMade") Boolean responseMade, Model model) {
        model.addAttribute("signalRequest", signalRequest);
        model.addAttribute("responseMade", responseMade);
        model.addAttribute("SignalSignType", SignalSignType.class);
        return "request-form";
    }

    @GetMapping("/process")
    public String returnReadyResponse(Model model) {
        // Если в модели уже есть сессионные данные, просто покажем старый результат
        if (model.containsAttribute("signalResponse")) {
            return "response-view";
        }
        return "redirect:/";  // Если данных нет, перенаправляем на форму
    }


    @PostMapping("/process")
    public String processRequest(@ModelAttribute("pageSettings") SignalRequest signalRequest, Model model)
            throws IOException {

        SignalResponse signalResponse = signalService.generateSignalResponse(signalRequest);
        model.addAttribute("responseMade", true);

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
