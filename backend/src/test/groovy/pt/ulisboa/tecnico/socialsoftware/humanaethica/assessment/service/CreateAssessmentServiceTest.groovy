package pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.dto.ThemeDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.domain.Theme
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler
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
        def assessmentDto = createAssessmentDto(REVIEW_10_CHARACTERS, NOW)

        when:
        def result = assessmentService.createAssessment(institution.getId(), volunteer.getId(), assessmentDto)

        then: "the returned data is correct"
        result.review == REVIEW_10_CHARACTERS
        result.reviewDate != null
        result.institution.id == institution.id
        result.volunteer.id == volunteer.id
        and: "the assessment is saved in the database"
        assessmentRepository.findAll().size() == 1
        and: "the stored data is correct"
        def storedAssessment = assessmentRepository.findById(result.id).get()
        storedAssessment.review == REVIEW_10_CHARACTERS
        storedAssessment.reviewDate != null
        storedAssessment.institution.id == institution.id
        storedAssessment.volunteer.id == volunteer.id
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