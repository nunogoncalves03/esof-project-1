package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.webservice

import org.springframework.http.MediaType
import org.springframework.http.HttpStatus
import org.springframework.http.HttpHeaders
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler

import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CreateParticipationWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def ParticipationDto
    def activity
    def activityId
    def volunteerId

    def setup() {
        deleteAll()

        webClient = WebClient.create("http://localhost:" + port)
        headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        def institution = institutionService.getDemoInstitution()
        def activityDto = createActivityDto(ACTIVITY_NAME_1,ACTIVITY_REGION_1,1,ACTIVITY_DESCRIPTION_1,
                IN_ONE_DAY,IN_TWO_DAYS,IN_THREE_DAYS, [])
        activity = new Activity(activityDto, institution, [])
        activityRepository.save(activity)

        activityId = activity.getId()
        volunteerId = authUserService.loginDemoVolunteerAuth().getUser().getId()

        participationDto = new ParticipationDto()
        participationDto.setRating(PARTICIPATION_RATING_1)
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
        response.activityId == activityId
        response.volunteerId == volunteerId
        response.rating == PARTICIPATION_RATING_1
        response.acceptanceDate != null

        and: "check database data"
        participationRepository.count() == 1
        def participation = participationRepository.findAll().get(0)
        participation.getActivity().getId() == activityId
        participation.getVolunteer().getId() == volunteerId
        participation.getRating() == PARTICIPATION_RATING_1
        participation.getAcceptanceDate() != null

        cleanup:
        deleteAll()
    }

    def "login as member, and create a participation with error"() {
        given: "a member"
        demoMemberLogin()
        and: "a participation with a deadline in the future"
        participationDto.acceptanceDate = DateHandler.toISOString(IN_ONE_DAY)

        when:
        def response = webClient.post()
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
        def response = webClient.post()
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
        def response = webClient.post()
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
