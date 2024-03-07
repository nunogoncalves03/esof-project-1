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
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain.Participation

import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Member
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer

import spock.lang.Unroll


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GetParticipationsByActivityWebServiceIT extends SpockTest {

    public static final String EXIST = "exist"
    public static final String NO_EXIST = "noExist"

    @LocalServerPort
    private int port

    def activity

    def emptyActivity

    def volunteer
    def volunteerId

    def setup() {
        deleteAll()

        webClient = WebClient.create("http://localhost:" + port)
        headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        and: "a demo member"
        def memberId = authUserService.loginDemoMemberAuth().getUser().getId()
        def member = userRepository.findById(memberId).get()

        and: "an activity associated to the member institution"
        def activityDto = createActivityDto(ACTIVITY_NAME_1,ACTIVITY_REGION_1,PARTICIPATION_ACTIVITY_LIMIT_1,ACTIVITY_DESCRIPTION_1,
                ONE_DAY_AGO,IN_TWO_DAYS,IN_THREE_DAYS, [])
        activity = new Activity(activityDto, member.getInstitution(), [])
        activityRepository.save(activity)

        and: "a volunteer"
        volunteer = new Volunteer()
        userRepository.save(volunteer)
        volunteerId = volunteer.getId()

        and: "a participation associated to the volunteer and to the activity"
        def participationDto = new ParticipationDto()
        participationDto.setRating(PARTICIPATION_RATING_1)
        def participation = new Participation(activity, volunteer, participationDto)
        participationRepository.save(participation)

        and: "another activity with no participations"
        def institution = new Institution()
        institutionRepository.save(institution)
        emptyActivity = new Activity(activityDto, institution, [])
        activityRepository.save(emptyActivity)
    }

    def "login as institution member and get participations by activity"() {
        given: "an institution member"
        demoMemberLogin()

        when:
        def response = webClient.get()
            .uri('/participations/' + activity.getId())
            .headers(httpHeaders -> httpHeaders.putAll(headers))
            .retrieve()
            .bodyToFlux(ParticipationDto.class)
            .collectList()
            .block()

        then: "check response data"
        response.size() == 1
        response.get(0).acceptanceDate != null
        response.get(0).volunteerId == volunteerId
        response.get(0).rating == PARTICIPATION_RATING_1

        cleanup:
        deleteAll()
    }

    @Unroll
    def "login as member that doesn't belong to the institution and get participations by activity"() {
        given: "a member"
        demoMemberLogin()

        when:
        def response = webClient.get()
            .uri('/participations/' + getEmptyActivityId(activityId))
            .headers(httpHeaders -> httpHeaders.putAll(headers))
            .retrieve()
            .bodyToFlux(ParticipationDto.class)
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

    def "login as a volunteer and get participations by activity"() {
        given:
        demoVolunteerLogin()

        when:
        def response = webClient.get()
            .uri('/participations/' + activity.getId())
            .headers(httpHeaders -> httpHeaders.putAll(headers))
            .retrieve()
            .bodyToFlux(ParticipationDto.class)
            .collectList()
            .block()

        then: "an error is returned"
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN

        cleanup:
        deleteAll()
    }

    def "login as an admin and get participations by activity"() {
        given:
        demoAdminLogin()

        when:
        def response = webClient.get()
            .uri('/participations/' + activity.getId())
            .headers(httpHeaders -> httpHeaders.putAll(headers))
            .retrieve()
            .bodyToFlux(ParticipationDto.class)
            .collectList()
            .block()

        then: "an error is returned"
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN

        cleanup:
        deleteAll()
    }

    def getEmptyActivityId(activityId) {
        if (activityId == EXIST)
            return emptyActivity.id
        else if (activityId == NO_EXIST)
            return 999
        return null
    }

}
