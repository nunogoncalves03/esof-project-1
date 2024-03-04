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
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.domain.Enrollment
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.dto.EnrollmentDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Member
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer
import spock.lang.Unroll

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GetEnrollmentsByActivityWebServiceIT extends SpockTest {
    public static final String EXIST = "exist"
    public static final String NO_EXIST = "noExist"

    @LocalServerPort
    private int port

    def activity1
    def activity2
    def volunteerId

    def setup() {
        deleteAll()

        given:
        webClient = WebClient.create("http://localhost:" + port)
        headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        and: "a demo member"
        def memberId = authUserService.loginDemoMemberAuth().getUser().getId()
        def member = (Member) userRepository.findById(memberId).get()

        and: "an activity associated with the member's institution"
        def activityDto = createActivityDto(ACTIVITY_NAME_1, ACTIVITY_REGION_1, 1, ACTIVITY_DESCRIPTION_1,
                IN_ONE_DAY, IN_TWO_DAYS, IN_THREE_DAYS, [])
        activity1 = new Activity(activityDto, member.getInstitution(), [])
        activityRepository.save(activity1)

        and: "a volunteer"
        def volunteer = new Volunteer()
        userRepository.save(volunteer)
        volunteerId = volunteer.getId()

        and: "an enrollment from the volunteer for that activity"
        def enrollmentDto = new EnrollmentDto()
        enrollmentDto.setMotivation(ENROLLMENT_MOTIVATION_1)
        def enrollment = new Enrollment(activity1, volunteer, enrollmentDto)
        enrollmentRepository.save(enrollment)

        and: "a new institution with an activity with no enrollments"
        def institution = new Institution()
        institutionRepository.save(institution)
        activity2 = new Activity(activityDto, institution, [])
        activityRepository.save(activity2)
    }

    def "login as institution member and get enrollments by activity"() {
        given:
        demoMemberLogin()

        when:
        def response = webClient.get()
                .uri('/enrollments/' + activity1.getId())
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .retrieve()
                .bodyToFlux(EnrollmentDto.class)
                .collectList()
                .block()

        then: "check response"
        response.size() == 1
        response.get(0).motivation == ENROLLMENT_MOTIVATION_1
        response.get(0).enrollmentDateTime != null
        response.get(0).activityId == activity1.getId()
        response.get(0).volunteerId == volunteerId

        cleanup:
        deleteAll()
    }

    @Unroll
    def "login as a member that doesn't belong to the institution and get enrollments by activity"() {
        given:
        demoMemberLogin()

        when:
        def response = webClient.get()
                .uri('/enrollments/' + getActivity2Id(activityId))
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .retrieve()
                .bodyToFlux(EnrollmentDto.class)
                .collectList()
                .block()

        then: "an error is returned"
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN

        cleanup:
        deleteAll()

        where:
        activityId << [EXIST, NO_EXIST]
    }

    def "login as a volunteer and get enrollments by activity"() {
        given:
        demoVolunteerLogin()

        when:
        def response = webClient.get()
                .uri('/enrollments/' + activity1.getId())
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .retrieve()
                .bodyToFlux(EnrollmentDto.class)
                .collectList()
                .block()

        then: "an error is returned"
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN

        cleanup:
        deleteAll()
    }

    def "login as an admin and get enrollments by activity"() {
        given:
        demoAdminLogin()

        when:
        def response = webClient.get()
                .uri('/enrollments/' + activity1.getId())
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .retrieve()
                .bodyToFlux(EnrollmentDto.class)
                .collectList()
                .block()

        then: "an error is returned"
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN

        cleanup:
        deleteAll()
    }

    def getActivity2Id(activityId) {
        if (activityId == EXIST)
            return activity2.id
        else if (activityId == NO_EXIST)
            return 999999
        return null
    }
}
