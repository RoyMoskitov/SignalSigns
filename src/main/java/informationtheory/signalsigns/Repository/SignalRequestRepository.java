package informationtheory.signalsigns.Repository;

import informationtheory.signalsigns.Model.SignalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SignalRequestRepository extends JpaRepository<SignalRequest, Long> {
}
