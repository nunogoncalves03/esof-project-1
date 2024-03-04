package pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain

import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.dto.AssessmentDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage;
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

        and: "an institution"
        institution.getActivities() >> [activity]
        institution.getAssessments() >> [otherAssessment]

        and: "a different assessment with an associated volunteer"
        otherAssessment.getVolunteer() >> otherVolunteer
    }

    def "create assessment with valid review, valid volunteer and valid institution"() {
        given:
        activity.getEndingDate() >> TWO_DAYS_AGO
        volunteer.getId() >> 1
        otherVolunteer.getId() >> 2

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
    
    @Unroll
    def "create assessment and violate review minimum length invariant: review=#review"() {
        given:
        activity.getEndingDate() >> TWO_DAYS_AGO
        volunteer.getId() >> 1
        otherVolunteer.getId() >> 2

        and: "an assessmentDto"
        assessmentDto.setReview(review)

        when:
        new Assessment(assessmentDto, institution, volunteer)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.ASSESSMENT_REVIEW_INVALID

        where:
        review << [null, REVIEW_0_CHARACTERS, REVIEW_10_SPACES, REVIEW_5_CHARACTERS]
    }

    @Unroll
    def "create assessment and violate volunteer cant assess the same institution more than once"() {
        given: "otherVolunteer with same unique id as volunteer"
        volunteer.getId() >> 1
        otherVolunteer.getId() >> 1
        activity.getEndingDate() >> TWO_DAYS_AGO

        when:
        new Assessment(assessmentDto, institution, volunteer)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.ASSESSMENT_VOLUNTEER_ASSESSING_SAME_INSTITUTION_AGAIN
    }

    @Unroll
    def "create assessment and violate institution has at least one finished activity"() {
        given:
        activity.getEndingDate() >> date
        volunteer.getId() >> 1
        otherVolunteer.getId() >> 2

        when:
        new Assessment(assessmentDto, institution, volunteer)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.ASSESSMENT_INSTITUTION_SHOULD_HAVE_ONE_FINISHED_ACTIVITY

        where:
        date << [IN_ONE_DAY, IN_TWO_DAYS, IN_THREE_DAYS]
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}