package informationtheory.signalsigns.Service;

import org.springframework.stereotype.Component;
import java.util.List;
import java.text.DecimalFormat;

@Component
public class ThymeleafUtils {
    private static final DecimalFormat df = new DecimalFormat("0.00"); // Округление до 2 знаков

    public String formatAndHighlight(List<Double> encoded, List<Double> noisy) {
        if (encoded == null || noisy == null || encoded.size() != noisy.size()) {
            return "Incorrect input text";
        }

        StringBuilder result = new StringBuilder();
        result.append("<table style='border-collapse: collapse; text-align: center;'>");

        // Заголовки
        result.append("<tr><th style='padding: 5px; border: 1px solid black;'>Type</th>");
        for (int i = 0; i < encoded.size(); i++) {
            result.append("<th style='padding: 5px; border: 1px solid black; min-width: 40px; white-space: nowrap;'>")
                    .append(i + 1)
                    .append("</th>");
        }
        result.append("</tr>");

        // Оригинальные данные
        result.append("<tr><td style='padding: 5px; border: 1px solid black; font-weight: bold;'>Original</td>");
        for (double value : encoded) {
            result.append("<td style='padding: 5px; border: 1px solid black; min-width: 40px; white-space: nowrap;'>")
                    .append(df.format(value))
                    .append("</td>");
        }
        result.append("</tr>");

        // Зашумленные данные
        result.append("<tr><td style='padding: 5px; border: 1px solid black; font-weight: bold;'>Noisy</td>");
        for (int i = 0; i < noisy.size(); i++) {
            double original = encoded.get(i);
            double modified = noisy.get(i);
            String style = Math.abs(original - modified) > 0.05 ? "color: red;" : ""; // Если разница > 0.05, выделяем
            result.append("<td style='padding: 5px; border: 1px solid black; min-width: 40px; white-space: nowrap; ")
                    .append(style).append("'>")
                    .append(df.format(modified))
                    .append("</td>");
        }
        result.append("</tr></table>");
        return result.toString();
    }
}
