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
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain.Assessment
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.dto.AssessmentDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Member
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer
import spock.lang.Unroll

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GetInstitutionAssessmentsWebServiceIT extends SpockTest {
    public static final Integer NO_EXIST = 999

    @LocalServerPort
    private int port

    def institution
    def volunteerId

    def setup() {
        deleteAll()

        given:
        webClient = WebClient.create("http://localhost:" + port)
        headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        and: "an institution"
        institution = new Institution()
        institution = institutionRepository.save(institution)

        and: "a finished activity associated with the institution"
        def activityDto = createActivityDto(ACTIVITY_NAME_1, ACTIVITY_REGION_1, 1, ACTIVITY_DESCRIPTION_1,
                THREE_DAYS_AGO, TWO_DAYS_AGO, ONE_DAY_AGO, [])
        def activity = new Activity(activityDto, institution, [])
        activityRepository.save(activity)

        and: "a volunteer"
        def volunteer = new Volunteer()
        userRepository.save(volunteer)
        volunteerId = volunteer.getId()

        and: "an assessment from the volunteer for that institution"
        def assessmentDto = createAssessmentDto(REVIEW_10_CHARACTERS)
        def assessment = new Assessment(assessmentDto, institution, volunteer)
        assessmentRepository.save(assessment)
    }

    def "don't login, get institution assessments"() {
        when:
        def response = webClient.get()
                .uri('/assessments/' + institution.getId())
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .retrieve()
                .bodyToFlux(AssessmentDto.class)
                .collectList()
                .block()

        then: "check response"
        response.size() == 1
        response.get(0).review == REVIEW_10_CHARACTERS
        response.get(0).reviewDate != null

        cleanup:
        deleteAll()
    }

    @Unroll
    def "login as a member, get institution assessments"() {
        given: "a member"
        demoMemberLogin()

        when:
        def response = webClient.get()
                .uri('/assessments/' + institution.getId())
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .retrieve()
                .bodyToFlux(AssessmentDto.class)
                .collectList()
                .block()

        then: "check response"
        response.size() == 1
        response.get(0).review == REVIEW_10_CHARACTERS
        response.get(0).reviewDate != null

        cleanup:
        deleteAll()
    }

    def "login as a volunteer, get institution assessments"() {
        given: "a volunteer"
        demoVolunteerLogin()

        when:
        def response = webClient.get()
                .uri('/assessments/' + institution.getId())
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .retrieve()
                .bodyToFlux(AssessmentDto.class)
                .collectList()
                .block()

        then: "check response"
        response.size() == 1
        response.get(0).review == REVIEW_10_CHARACTERS
        response.get(0).reviewDate != null

        cleanup:
        deleteAll()
    }

    def "login as an admin, get institution assessments"() {
        given: "an admin"
        demoAdminLogin()

        when:
        def response = webClient.get()
                .uri('/assessments/' + institution.getId())
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .retrieve()
                .bodyToFlux(AssessmentDto.class)
                .collectList()
                .block()

        then: "check response"
        response.size() == 1
        response.get(0).review == REVIEW_10_CHARACTERS
        response.get(0).reviewDate != null

        cleanup:
        deleteAll()
    }
}
