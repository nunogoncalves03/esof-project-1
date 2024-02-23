package pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.domain

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.dto.EnrollmentDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler
import spock.lang.Unroll

@DataJpaTest
class CreateEnrollmentMethodTest extends SpockTest {
    Activity activity = Mock()
    Volunteer volunteer = Mock()
    def enrollmentDto

    def setup() {
        given: "enrollment info"
        enrollmentDto = new EnrollmentDto()
        enrollmentDto.motivation = ENROLLMENT_MOTIVATION_1
    }

    def "create enrollment with valid motivation, valid enrollmentDateTime and no previous enrollment of volunteer"() {
        given:
        volunteer.getEnrollments() >> []
        activity.getEndingDate() >> IN_TWO_DAYS

        when:
        def result = new Enrollment(activity, volunteer, enrollmentDto)

        then: "check result"
        result.getMotivation() == ENROLLMENT_MOTIVATION_1
        result.getEnrollmentDateTime() != null
        result.getActivity() == activity
        result.getVolunteer() == volunteer
        and: "invocations"
        1 * activity.addEnrollment(_)
        1 * volunteer.addEnrollment(_)
    }

    @Unroll
    def "create enrollment and violate motivation minimum length invariant: motivation=#motivation"() {
        given:
        volunteer.getEnrollments() >> []
        activity.getEndingDate() >> IN_TWO_DAYS

        and: "an enrollment dto"
        enrollmentDto.setMotivation(motivation)

        when:
        new Enrollment(activity, volunteer, enrollmentDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.ENROLLMENT_MOTIVATION_SHOULD_HAVE_AT_LEAST_TEN_CHARACTERS

        where:
        motivation << [null, ENROLLMENT_MOTIVATION_0_CHARACTERS, ENROLLMENT_MOTIVATION_9_CHARACTERS, ENROLLMENT_MOTIVATION_SPACES]
    }

    def "create enrollment and violate single enrollment by volunteer invariant"() {
        given:
        Enrollment pastEnrollment = Mock()
        pastEnrollment.getActivity() >> activity
        volunteer.getEnrollments() >> [pastEnrollment]
        activity.getEndingDate() >> IN_TWO_DAYS

        when:
        new Enrollment(activity, volunteer, enrollmentDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.ENROLLMENT_VOLUNTEER_CAN_ONLY_ENROLL_IN_ACTIVITY_ONCE
    }

    def "create enrollment and violate enrollment after ending date invariant"() {
        given:
        volunteer.getEnrollments() >> []
        activity.getEndingDate() >> ONE_DAY_AGO

        when:
        new Enrollment(activity, volunteer, enrollmentDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.ENROLLMENT_VOLUNTEER_CANT_ENROLL_IN_ACTIVITY_AFTER_ENDING_DATE
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}