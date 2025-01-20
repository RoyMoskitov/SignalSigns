package informationtheory.signalsigns.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
public class SignalResponse {
    public void setId(Long id) {
        this.id = id;
    }

    public void setEncodedSignal(List<Double> encodedSignal) {
        this.encodedSignal = encodedSignal;
    }

    public void setNoisySignal(List<Double> noisySignal) {
        this.noisySignal = noisySignal;
    }

    public void setEncodedText(String encodedText) {
        this.encodedText = encodedText;
    }

    public void setDecodedText(String decodedText) {
        this.decodedText = decodedText;
    }

    public Long getId() {
        return id;
    }

    public List<Double> getEncodedSignal() {
        return encodedSignal;
    }

    public List<Double> getNoisySignal() {
        return noisySignal;
    }

    public String getEncodedText() {
        return encodedText;
    }

    public String getDecodedText() {
        return decodedText;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ElementCollection
    @CollectionTable(name = "encoded_signal", joinColumns = @JoinColumn(name = "request_id"))
    @Column(name = "data_point")
    private List<Double> encodedSignal;
    @ElementCollection
    @CollectionTable(name = "noisy_signal", joinColumns = @JoinColumn(name = "request_id"))
    @Column(name = "data_point")
    private List<Double> noisySignal;
//    @ElementCollection
//    @CollectionTable(name = "decoded_signal", joinColumns = @JoinColumn(name = "request_id"))
//    @Column(name = "data_point")
//    private List<Double> decodedSignal;
    private String encodedText;
    private String decodedText;
}
