package informationtheory.signalsigns.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class SignalRequest {
    public void setId(Long id) {
        this.id = id;
    }

    public void setInputText(String inputText) {
        this.inputText = inputText;
    }

    public void setType(SignalSignType type) {
        this.type = type;
    }

    public void setNoiseIntensity(Double noiseIntensity) {
        this.noiseIntensity = noiseIntensity;
    }

    public void setMaxErrorValue(Double maxErrorValue) {
        this.maxErrorValue = maxErrorValue;
    }

    public void setMaxErrors(Integer maxErrors) {
        this.maxErrors = maxErrors;
    }

    public Long getId() {
        return id;
    }

    public String getInputText() {
        return inputText;
    }

    public SignalSignType getType() {
        return type;
    }

    public Double getNoiseIntensity() {
        return noiseIntensity;
    }

    public Double getMaxErrorValue() {
        return maxErrorValue;
    }

    public Integer getMaxErrors() {
        return maxErrors;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String inputText;
    private SignalSignType type;
    private Double noiseIntensity;
    private Double maxErrorValue;
    private Integer maxErrors;
}
