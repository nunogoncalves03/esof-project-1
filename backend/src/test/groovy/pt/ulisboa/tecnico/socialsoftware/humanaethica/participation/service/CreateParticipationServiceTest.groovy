package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import spock.lang.Unroll
import java.time.LocalDateTime


@DataJpaTest
class CreateParticipationServiceTest extends SpockTest {
    public static final String EXIST = "exist"
    public static final String NO_EXIST = "noExist"

    def activity
    def volunteer

    def setup() {
        //volunteer = new UserDto(authUserService.getDemoVolunteer())
        volunteer = authUserService.loginDemoVolunteerAuth().getUser()

        def institution = institutionService.getDemoInstitution()
        def activityDto = createActivityDto(ACTIVITY_NAME_1, ACTIVITY_REGION_1, PARTICIPATION_ACTIVITY_LIMIT_1, ACTIVITY_DESCRIPTION_1, TWO_DAYS_AGO,
            ONE_DAY_AGO, IN_TWO_DAYS, [])
        activity = new Activity(activityDto, institution, [])
        activityRepository.save(activity) 
    }

    def "create participation"() {
        given: "a participation dto"
        def participationDto = new ParticipationDto()
        participationDto.setRating(PARTICIPATION_RATING_1)
        participationDto.setVolunteerId(volunteer.getId())

        when: 
        def result = participationService.createParticipation(activity.getId(), participationDto)

        then: "the returned data is correct"
        result.rating == PARTICIPATION_RATING_1
        result.acceptanceDate != null
        result.volunteerId == volunteer.id

        and: "participation is saved in the database"
        participationRepository.findAll().size() == 1

        and: "the stored data is correct"
        def storedParticipation = participationRepository.findById(result.id).get()
        storedParticipation.rating == PARTICIPATION_RATING_1
        storedParticipation.acceptanceDate != null
        storedParticipation.activity.id == activity.id
        storedParticipation.volunteer.id == volunteer.id
    }

    @Unroll
    def 'invalid arguments: volunteerId=#volunteerId'() {

        given: "an activity dto"
        def participationDto = new ParticipationDto()
        participationDto.setVolunteerId(getVolunteerId(volunteerId))

        when: 
        participationService.createParticipation(activity.getId(), participationDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == errorMessage

        and: "no participation is stored in the database"
        participationRepository.findAll().size() == 0

        where:
        volunteerId || errorMessage
        null        || ErrorMessage.VOLUNTEER_NOT_FOUND
        NO_EXIST    || ErrorMessage.VOLUNTEER_NOT_FOUND

    }

    def getVolunteerId(volunteerId) {
        if (volunteerId == EXIST)
            return volunteer.id
        else if (volunteerId == NO_EXIST)
            return 999
        return null
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}