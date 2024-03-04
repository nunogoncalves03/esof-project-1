package pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain.Assessment;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.dto.AssessmentDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.repository.AssessmentRepository;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.repository.InstitutionRepository;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Member;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.repository.UserRepository;

import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.INSTITUTION_NOT_FOUND;
import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.USER_NOT_FOUND;

import java.util.Comparator;
import java.util.List;

@Service
public class AssessmentService {
    @Autowired
    AssessmentRepository assessmentRepository;

    @Autowired
    InstitutionRepository institutionRepository;

    @Autowired
    UserRepository userRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<AssessmentDto> getAssessmentsByInstitution(Integer institutionId) {
        if (institutionId == null) throw new HEException(INSTITUTION_NOT_FOUND);

        Institution institution = (Institution) institutionRepository.findById(institutionId)
                .orElseThrow(() -> new HEException(INSTITUTION_NOT_FOUND, institutionId));

        return institution.getAssessments().stream()
                .map(assessment -> new AssessmentDto(assessment,true, true))
                .sorted(Comparator.comparing(AssessmentDto::getVolunteerName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AssessmentDto createAssessment(Integer institutionId, Integer volunteerId, AssessmentDto assessmentDto) {
        if (volunteerId == null) throw new HEException(USER_NOT_FOUND);
        Volunteer volunteer = (Volunteer) userRepository.findById(volunteerId).orElseThrow(() -> new HEException(USER_NOT_FOUND, volunteerId));

        if (institutionId == null) throw new HEException(INSTITUTION_NOT_FOUND);
        Institution institution = (Institution) institutionRepository.findById(institutionId)
                .orElseThrow(() -> new HEException(INSTITUTION_NOT_FOUND, institutionId));

        Assessment assessment = new Assessment(assessmentDto, institution, volunteer);
        assessmentRepository.save(assessment);

        return new AssessmentDto(assessment, true, true);
    }
}
