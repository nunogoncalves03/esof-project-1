package pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.domain.Enrollment
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.dto.EnrollmentDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer
import spock.lang.Unroll

@DataJpaTest
class GetEnrollmentsByActivityServiceTest extends SpockTest {
    public static final String EXIST = "exist"
    public static final String NO_EXIST = "noExist"

    def activity1
    def activity2
    def activity3

    def setup() {
        given: "2 volunteers and a demo institution"
        def volunteer = new Volunteer()
        userRepository.save(volunteer)
        def volunteer2 = new Volunteer()
        userRepository.save(volunteer2)

        def institution = institutionService.getDemoInstitution()

        and: "3 activities"
        def activityDto = createActivityDto(ACTIVITY_NAME_1, ACTIVITY_REGION_1, 1, ACTIVITY_DESCRIPTION_1,
                IN_ONE_DAY, IN_TWO_DAYS, IN_THREE_DAYS, [])
        activity1 = new Activity(activityDto, institution, [])
        activityRepository.save(activity1)

        activityDto.name = ACTIVITY_NAME_2
        activity2 = new Activity(activityDto, institution, [])
        activityRepository.save(activity2)

        activityDto.name = ACTIVITY_NAME_3
        activity3 = new Activity(activityDto, institution, [])
        activityRepository.save(activity3)

        and: "two enrollments associated with activity1"
        def enrollmentDto = new EnrollmentDto()
        enrollmentDto.setMotivation(ENROLLMENT_MOTIVATION_1)
        def enrollment1 = new Enrollment(activity1, volunteer, enrollmentDto)
        enrollmentRepository.save(enrollment1)

        enrollmentDto.setMotivation(ENROLLMENT_MOTIVATION_2)
        def enrollment2 = new Enrollment(activity1, volunteer2, enrollmentDto)
        enrollmentRepository.save(enrollment2)

        and: "one with activity2"
        enrollmentDto.setMotivation(ENROLLMENT_MOTIVATION_3)
        def enrollment3 = new Enrollment(activity2, volunteer, enrollmentDto)
        enrollmentRepository.save(enrollment3)
    }

    def 'get sorted enrollments associated with activities'() {
        when:
        def result = enrollmentService.getEnrollmentsByActivity(activity1.getId())
        def result2 = enrollmentService.getEnrollmentsByActivity(activity2.getId())
        def result3 = enrollmentService.getEnrollmentsByActivity(activity3.getId())

        then:
        result.size() == 2
        result.get(0).motivation == ENROLLMENT_MOTIVATION_1
        result.get(1).motivation == ENROLLMENT_MOTIVATION_2
        result2.size() == 1
        result2.get(0).motivation == ENROLLMENT_MOTIVATION_3
        result3.size() == 0
    }

    @Unroll
    def 'invalid arguments: activityId=#activityId'() {
        when:
        enrollmentService.getEnrollmentsByActivity(getActivityId(activityId))

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.ACTIVITY_NOT_FOUND

        where:
        activityId << [null, NO_EXIST]
    }

    def getActivityId(activityId) {
        if (activityId == EXIST)
            return activity1.id
        else if (activityId == NO_EXIST)
            return 222
        return null
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
