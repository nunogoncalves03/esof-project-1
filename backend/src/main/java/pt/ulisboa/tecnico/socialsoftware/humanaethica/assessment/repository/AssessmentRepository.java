package pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain.Assessment;

@Repository
@Transactional
public interface AssessmentRepository extends JpaRepository<Assessment, Integer> {
}
