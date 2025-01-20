package informationtheory.signalsigns.Repository;

import informationtheory.signalsigns.Model.SignalResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SignalResponseRepository extends JpaRepository<SignalResponse, Long> {
}
