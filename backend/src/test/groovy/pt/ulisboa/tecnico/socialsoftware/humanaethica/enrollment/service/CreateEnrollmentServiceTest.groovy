package pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.dto.EnrollmentDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import spock.lang.Unroll

@DataJpaTest
class CreateEnrollmentServiceTest extends SpockTest {
    public static final String EXIST = "exist"
    public static final String NO_EXIST = "noExist"

    def activity
    def volunteer

    def setup() {
        volunteer = authUserService.loginDemoVolunteerAuth().getUser()

        def institution = institutionService.getDemoInstitution()
        def activityDto = createActivityDto(ACTIVITY_NAME_1, ACTIVITY_REGION_1, 1, ACTIVITY_DESCRIPTION_1,
                IN_ONE_DAY, IN_TWO_DAYS, IN_THREE_DAYS, [])
        activity = new Activity(activityDto, institution, [])
        activityRepository.save(activity)
    }

    def "create enrollment"() {
        given: "an enrollment dto"
        def enrollmentDto = new EnrollmentDto()
        enrollmentDto.setMotivation(ENROLLMENT_MOTIVATION_10_CHARACTERS)

        when:
        def result = enrollmentService.createEnrollment(volunteer.getId(), activity.getId(), enrollmentDto)

        then: "the returned data is correct"
        result.motivation == ENROLLMENT_MOTIVATION_10_CHARACTERS
        result.enrollmentDateTime != null
        result.activityId == activity.id
        result.volunteerId == volunteer.id
        and: "the enrollment is saved in the database"
        enrollmentRepository.findAll().size() == 1
        and: "the stored data is correct"
        def storedEnrollment = enrollmentRepository.findById(result.id).get()
        storedEnrollment.motivation == ENROLLMENT_MOTIVATION_10_CHARACTERS
        storedEnrollment.enrollmentDateTime != null
        storedEnrollment.activity.id == activity.id
        storedEnrollment.volunteer.id == volunteer.id
    }

    @Unroll
    def 'invalid arguments: motivation=#motivation | volunteerId=#volunteerId | activityId=#activityId'() {
        given: "an enrollment dto"
        def enrollmentDto = new EnrollmentDto()
        enrollmentDto.setMotivation(motivation)

        when:
        enrollmentService.createEnrollment(getVolunteerId(volunteerId), getActivityId(activityId), enrollmentDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == errorMessage
        and: "no enrollment is stored in the database"
        enrollmentRepository.findAll().size() == 0

        where:
        motivation                          | volunteerId | activityId || errorMessage
        null                                | EXIST       | EXIST      || ErrorMessage.ENROLLMENT_MOTIVATION_SHOULD_HAVE_AT_LEAST_TEN_CHARACTERS
        ENROLLMENT_MOTIVATION_10_CHARACTERS | null        | EXIST      || ErrorMessage.USER_NOT_FOUND
        ENROLLMENT_MOTIVATION_10_CHARACTERS | NO_EXIST    | EXIST      || ErrorMessage.USER_NOT_FOUND
        ENROLLMENT_MOTIVATION_10_CHARACTERS | EXIST       | null       || ErrorMessage.ACTIVITY_NOT_FOUND
        ENROLLMENT_MOTIVATION_10_CHARACTERS | EXIST       | NO_EXIST   || ErrorMessage.ACTIVITY_NOT_FOUND
    }

    def getVolunteerId(volunteerId) {
        if (volunteerId == EXIST)
            return volunteer.id
        else if (volunteerId == NO_EXIST)
            return 222
        return null
    }

    def getActivityId(activityId) {
        if (activityId == EXIST)
            return activity.id
        else if (activityId == NO_EXIST)
            return 222
        return null
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
