package pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.webservice

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.dto.AssessmentDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.dto.AuthUserDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.domain.Theme
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.dto.UserDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RegisterAssessmentWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    Institution institution
    AssessmentDto assessmentDto

    def setup() {
        deleteAll()

        webClient = WebClient.create("http://localhost:" + port)
        headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        given: "assessment info"
        assessmentDto = createAssessmentDto(REVIEW_20_CHARACTERS, TWO_DAYS_AGO)

        and: "an institution"
        institution = institutionService.getDemoInstitution()

        and: "a theme"
        def themes = new ArrayList<>()
        themes.add(createTheme(THEME_NAME_1, Theme.State.APPROVED,null))

        and: "a finished activity"
        def activityDto = createActivityDto(ACTIVITY_NAME_1,ACTIVITY_REGION_1,1,ACTIVITY_DESCRIPTION_1,
                THREE_DAYS_AGO,TWO_DAYS_AGO,ONE_DAY_AGO,null)
        def activity = new Activity(activityDto, institution, themes)
        activityRepository.save(activity)
    }

    def "login as volunteer, and create an assessment"() {
        given:
        AuthUserDto user = demoVolunteerLogin()

        when:
        def response = webClient.post()
                .uri('/assessments/' + institution.getId())
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .bodyValue(assessmentDto)
                .retrieve()
                .bodyToMono(AssessmentDto.class)
                .block()

        then: "check response data"
        response.review == REVIEW_20_CHARACTERS
        response.reviewDate == DateHandler.toISOString(TWO_DAYS_AGO)
        response.institution.getId() == institution.getId()
        response.volunteer.getId() == user.getId()
        and: "check database data"
        assessmentRepository.count() == 1
        def assessment = assessmentRepository.findAll().get(0)
        assessment.getReview() == REVIEW_20_CHARACTERS
        assessment.getReviewDate().withNano(0) == TWO_DAYS_AGO.withNano(0)
        assessment.getInstitution().getId() == institution.getId()
        assessment.volunteer.getId() == user.getId()

        cleanup:
        deleteAll()
    }

    def "login as member, and create an assessment"() {
        given: "a member"
        demoMemberLogin()

        when: "the member registers the assessment"
        def response = webClient.post()
                .uri('/assessments/' + institution.getId())
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .bodyValue(assessmentDto)
                .retrieve()
                .bodyToMono(AssessmentDto.class)
                .block()

        then: "an error is returned"
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN
        assessmentRepository.count() == 0

        cleanup:
        deleteAll()
    }

    def "login as admin, and create an assessment"() {
        given: "a admin"
        demoAdminLogin()

        when: "the admin registers the assessment"
        def response = webClient.post()
                .uri('/assessments/' + institution.getId())
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .bodyValue(assessmentDto)
                .retrieve()
                .bodyToMono(AssessmentDto.class)
                .block()

        then: "an error is returned"
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN
        assessmentRepository.count() == 0

        cleanup:
        deleteAll()
    }
}
