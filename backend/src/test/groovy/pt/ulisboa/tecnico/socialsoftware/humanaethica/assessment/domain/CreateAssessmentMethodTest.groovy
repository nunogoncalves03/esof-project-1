package pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain

import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.dto.AssessmentDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import spock.lang.Unroll
import java.time.LocalDateTime

@DataJpaTest
class CreateAssessmentMethodTest extends SpockTest {
    Assessment otherAssessment = Mock()
    Institution institution = Mock()
    Volunteer volunteer = Mock()
    Volunteer otherVolunteer = Mock()
    Activity activity = Mock()
    def assessmentDto

    def setup() {
        given: "assessement info"
        assessmentDto = new AssessmentDto()
        assessmentDto.review = REVIEW_10_CHARACTERS
        assessmentDto.reviewDate = DateHandler.toISOString(NOW)
    }

    def "create assessment with valid review, valid volunteer and valid institution"() {
        given:
        activity.getEndingDate() >> TWO_DAYS_AGO
        institution.getActivities() >> [activity]
        volunteer.getId() >> 1
        otherVolunteer.getId() >> 2
        otherAssessment.getVolunteer() >> otherVolunteer
        institution.getAssessments() >> [otherAssessment]

        when:
        def result = new Assessment(assessmentDto, institution, volunteer)

        then: "check result"
        result.getReview() == REVIEW_10_CHARACTERS
        result.getReviewDate() == NOW
        result.getInstitution() == institution
        and: "invocations"
        1 * institution.addAssessment(_)
        1 * volunteer.addAssessment(_)
    }
    
    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}