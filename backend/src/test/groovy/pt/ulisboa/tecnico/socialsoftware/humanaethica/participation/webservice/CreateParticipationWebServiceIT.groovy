package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.webservice

import org.springframework.http.MediaType
import org.springframework.http.HttpStatus
import org.springframework.http.HttpHeaders
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import org.springframework.web.reactive.function.client.WebClient
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import org.springframework.web.reactive.function.client.WebClientResponseException

import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CreateParticipationWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def participationDto
    def activity
    def activityId
    def volunteerId
    def institution

    def setup() {
        deleteAll()

        webClient = WebClient.create("http://localhost:" + port)
        headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        institution = institutionService.getDemoInstitution()
        def activityDto = createActivityDto(ACTIVITY_NAME_1,ACTIVITY_REGION_1,1,ACTIVITY_DESCRIPTION_1,
                ONE_DAY_AGO,IN_TWO_DAYS,IN_THREE_DAYS, [])

        activity = new Activity(activityDto, institution, [])
        activityRepository.save(activity)

        activityId = activity.getId()
        volunteerId = authUserService.loginDemoVolunteerAuth().getUser().getId()

        participationDto = new ParticipationDto()
        participationDto.setRating(PARTICIPATION_RATING_1)
        participationDto.setVolunteerId(volunteerId)

    }

    def "login as member, and create a participation"() {
        given: "a member"
        demoMemberLogin()

        when:
        def response = webClient.post()
                .uri('/participations/' + activityId)
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .bodyValue(participationDto)
                .retrieve()
                .bodyToMono(ParticipationDto.class)
                .block()

        then: "check response data"
        response.volunteerId == volunteerId
        response.rating == PARTICIPATION_RATING_1
        response.acceptanceDate != null

        and: "check database data"
        participationRepository.count() == 1
        def participation = participationRepository.findAll().get(0)
        participation.getVolunteer().getId() == volunteerId
        participation.getRating() == PARTICIPATION_RATING_1
        participation.getAcceptanceDate() != null

        cleanup:
        deleteAll()
    }

    def "login as member, and create a participation with error"() {
        given: "a member"
        demoMemberLogin()

        and: "a participation with invalid volunteer id"
        participationDto.setVolunteerId(null)

        when:
        webClient.post()
                .uri('/participations/' + activityId)
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .bodyValue(participationDto)
                .retrieve()
                .bodyToMono(ParticipationDto.class)
                .block()

        then: "check response status"
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.BAD_REQUEST
        participationRepository.count() == 0

        cleanup:
        deleteAll()
    }

    def "login as volunteer, and create a participation"() {
        given: "a volunteer"
        demoVolunteerLogin()

        when:
        webClient.post()
                .uri('/participations/' + activityId)
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .bodyValue(participationDto)
                .retrieve()
                .bodyToMono(ParticipationDto.class)
                .block()

        then: "an error is returned"
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN
        participationRepository.count() == 0

        cleanup:
        deleteAll()
    }

    def "login as admin, and create a participation"() {
        given: "an admin"
        demoAdminLogin()

        when:
        webClient.post()
                .uri('/participations/' + activityId)
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .bodyValue(participationDto)
                .retrieve()
                .bodyToMono(ParticipationDto.class)
                .block()

        then: "an error is returned"
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN
        participationRepository.count() == 0

        cleanup:
        deleteAll()
    }
}
