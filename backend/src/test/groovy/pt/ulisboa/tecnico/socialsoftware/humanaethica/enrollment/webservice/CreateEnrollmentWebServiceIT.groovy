package pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.webservice

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.dto.EnrollmentDto

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CreateEnrollmentWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def enrollmentDto
    def activity
    def activityId
    def volunteerId

    def setup() {
        deleteAll()

        webClient = WebClient.create("http://localhost:" + port)
        headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        def institution = institutionService.getDemoInstitution()
        def activityDto = createActivityDto(ACTIVITY_NAME_1, ACTIVITY_REGION_1, 1, ACTIVITY_DESCRIPTION_1,
                IN_ONE_DAY, IN_TWO_DAYS, IN_THREE_DAYS, [])
        activity = new Activity(activityDto, institution, [])
        activityRepository.save(activity)

        activityId = activity.getId()
        volunteerId = authUserService.loginDemoVolunteerAuth().getUser().getId()

        enrollmentDto = new EnrollmentDto()
        enrollmentDto.setMotivation(ENROLLMENT_MOTIVATION_10_CHARACTERS)
    }

    def "login as volunteer, and create an enrollment"() {
        given: "a volunteer"
        demoVolunteerLogin()

        when:
        def response = webClient.post()
                .uri('/enrollments/' + activityId)
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .bodyValue(enrollmentDto)
                .retrieve()
                .bodyToMono(EnrollmentDto.class)
                .block()

        then: "check response data"
        response.motivation == ENROLLMENT_MOTIVATION_10_CHARACTERS
        response.enrollmentDateTime != null
        response.activityId == activityId
        response.volunteerId == volunteerId
        and: 'check database data'
        enrollmentRepository.count() == 1

        def enrollment = enrollmentRepository.findById(response.id).get()
        enrollment.getMotivation() == ENROLLMENT_MOTIVATION_10_CHARACTERS
        enrollment.getEnrollmentDateTime() != null
        enrollment.getActivity().getId() == activityId
        enrollment.getVolunteer().getId() == volunteerId

        cleanup:
        deleteAll()
    }

    def "login as volunteer, and create an enrollment with error"() {
        given: 'a volunteer'
        demoVolunteerLogin()
        and: 'a motivation with less than 10 characters'
        enrollmentDto.motivation = ENROLLMENT_MOTIVATION_9_CHARACTERS

        when: 'the volunteer creates the enrollment'
        webClient.post()
                .uri('/enrollments/' + activityId)
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .bodyValue(enrollmentDto)
                .retrieve()
                .bodyToMono(EnrollmentDto.class)
                .block()

        then: "check response status"
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.BAD_REQUEST
        enrollmentRepository.count() == 0

        cleanup:
        deleteAll()
    }

    def "login as member, and create an enrollment"() {
        given: 'a member'
        demoMemberLogin()

        when: 'the member creates the enrollment'
        webClient.post()
                .uri('/enrollments/' + activityId)
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .bodyValue(enrollmentDto)
                .retrieve()
                .bodyToMono(EnrollmentDto.class)
                .block()

        then: "an error is returned"
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN
        enrollmentRepository.count() == 0

        cleanup:
        deleteAll()
    }

    def "login as admin, and create an enrollment"() {
        given: 'a member'
        demoAdminLogin()

        when: 'the admin creates the enrollment'
        webClient.post()
                .uri('/enrollments/' + activityId)
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .bodyValue(enrollmentDto)
                .retrieve()
                .bodyToMono(EnrollmentDto.class)
                .block()

        then: "an error is returned"
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN
        enrollmentRepository.count() == 0

        cleanup:
        deleteAll()
    }
}
