package pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import spock.lang.Unroll

@DataJpaTest
class CreateAssessmentServiceTest extends SpockTest {
    public static final String EXIST = "exist"
    public static final String NO_EXIST = "noExist"

    def volunteer
    def institution

    def setup() {
        volunteer = authUserService.loginDemoVolunteerAuth().getUser()
        institution = institutionService.getDemoInstitution()

        def activityDto = createActivityDto(ACTIVITY_NAME_1, ACTIVITY_REGION_1, 1, ACTIVITY_DESCRIPTION_1,
                THREE_DAYS_AGO, TWO_DAYS_AGO, ONE_DAY_AGO, [])
        def activity = new Activity(activityDto, institution, [])
        activityRepository.save(activity)
        institution = institutionRepository.save(institution)
    }

    def "create assessment"() {
        given: "an assessment dto"
        def assessmentDto = createAssessmentDto(REVIEW_10_CHARACTERS)

        when:
        def result = assessmentService.createAssessment(volunteer.getId(), institution.getId(), assessmentDto)

        then: "the returned data is correct"
        result.review == REVIEW_10_CHARACTERS
        result.reviewDate != null
        and: "the assessment is saved in the database"
        assessmentRepository.findAll().size() == 1
        and: "the stored data is correct"
        def storedAssessment = assessmentRepository.findById(result.id).get()
        storedAssessment.review == REVIEW_10_CHARACTERS
        storedAssessment.reviewDate != null
        storedAssessment.institution.id == institution.id
        storedAssessment.volunteer.id == volunteer.id
    }

    @Unroll
    def 'invalid arguments: review=#review | institutionId=#institutionId | volunteerId=#volunteerId'() {
        given: "an assessment dto"
        def assessmentDto = createAssessmentDto(review)

        when:
        assessmentService.createAssessment(getVolunteerId(volunteerId), getInstitutionId(institutionId), assessmentDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == errorMessage
        and: "no assessment is stored in the database"
        assessmentRepository.findAll().size() == 0

        where:
        review               | institutionId | volunteerId || errorMessage
        REVIEW_0_CHARACTERS  | EXIST         | EXIST       || ErrorMessage.ASSESSMENT_REVIEW_INVALID
        REVIEW_10_CHARACTERS | null          | EXIST       || ErrorMessage.INSTITUTION_NOT_FOUND
        REVIEW_10_CHARACTERS | NO_EXIST      | EXIST       || ErrorMessage.INSTITUTION_NOT_FOUND
        REVIEW_10_CHARACTERS | EXIST         | null        || ErrorMessage.USER_NOT_FOUND
        REVIEW_10_CHARACTERS | EXIST         | NO_EXIST    || ErrorMessage.USER_NOT_FOUND
    }

    def getVolunteerId(volunteerId){
        if (volunteerId == EXIST)
            return volunteer.id
        else if (volunteerId == NO_EXIST)
            return 222
        return null
    }

    def getInstitutionId(institutionId){
        if (institutionId == EXIST)
            return institution.id
        else if (institutionId == NO_EXIST)
            return 222
        return null
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}