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
    public String processRequest(@ModelAttribute SignalRequest signalRequest, Model model) {
        // Здесь обработка запроса и создание SignalResponse
        //SignalRequest savedRequest = signalService.saveSignalRequest(signalRequest);
        SignalResponse signalResponse = signalService.generateSignalResponse(signalRequest);
        model.addAttribute("signalResponse", signalResponse);
        return "response-view";
    }
}
