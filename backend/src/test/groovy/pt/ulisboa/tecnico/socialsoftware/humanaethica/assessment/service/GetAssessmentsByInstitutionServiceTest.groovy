package pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain.Assessment
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthNormalUser
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.domain.Theme
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer

@DataJpaTest
public class GetAssessmentsByInstitutionServiceTest extends SpockTest {
    def setup() {
        def institution = institutionService.getDemoInstitution()

        given: "assessment info"
        def assessmentDto = createAssessmentDto(REVIEW_10_CHARACTERS)

        and: "a theme"
        def themes = new ArrayList<>()
        themes.add(createTheme(THEME_NAME_1, Theme.State.APPROVED,null))

        and: "a finished activity"
        def activityDto = createActivityDto(ACTIVITY_NAME_1,ACTIVITY_REGION_1,1,ACTIVITY_DESCRIPTION_1,
                THREE_DAYS_AGO,TWO_DAYS_AGO,ONE_DAY_AGO,null)
        def activity = new Activity(activityDto, institution, themes)
        activityRepository.save(activity)

        and: "a volunteer"
        def volunteer = new Volunteer(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, AuthUser.Type.NORMAL, User.State.SUBMITTED)
        userRepository.save(volunteer)

        and: "an assessment"
        def assessment = new Assessment(assessmentDto, institution, volunteer)
        assessmentRepository.save(assessment)

        and: "another volunteer"
        volunteer = new Volunteer(USER_2_NAME, USER_2_USERNAME, USER_2_EMAIL, AuthUser.Type.NORMAL, User.State.SUBMITTED)
        userRepository.save(volunteer)

        and: "another assessment"
        assessmentDto.setReview(REVIEW_20_CHARACTERS)
        assessment = new Assessment(assessmentDto, institution, volunteer)
        assessmentRepository.save(assessment)
    }

    def "get two assessments"() {
        when:
        def result = assessmentService.getAssessmentsByInstitution(institutionService.getDemoInstitution().getId())

        then:
        result.size() == 2
        result.get(0).getReview() == REVIEW_10_CHARACTERS
        result.get(1).getReview() == REVIEW_20_CHARACTERS
    }

    def "get assessments of institution that does not exist"() {
        when:
        assessmentService.getAssessmentsByInstitution(null)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.INSTITUTION_NOT_FOUND
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
