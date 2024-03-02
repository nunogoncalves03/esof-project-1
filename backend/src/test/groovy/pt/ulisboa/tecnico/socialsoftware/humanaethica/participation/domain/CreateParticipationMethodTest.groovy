package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain.Participation
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler
import spock.lang.Unroll
import java.time.LocalDateTime

@DataJpaTest
class CreateParticipationMethodTest extends SpockTest {
    Activity activity = Mock()
    Volunteer volunteer = Mock()
    Participation otherParticipation = Mock()
    Integer rating = 1
    def participationDto

    def setup() {
        given: "participation info"
        participationDto = new ParticipationDto()
        participationDto.rating = PARTICIPATION_RATING_1
        participationDto.acceptanceDate = DateHandler.toISOString(NOW);
    }

    def "create participation in empty activity with rating, valid acceptance date and no previous participation of volunteer"() {
        given:
        volunteer.getParticipations() >> []
        activity.getParticipations() >> []
        activity.getParticipantsNumberLimit() >> 2
        activity.applicationDeadline >> ONE_DAY_AGO

        when:
        def result = new Participation(activity, volunteer, participationDto)

        then: "check result"
        result.getRating() == PARTICIPATION_RATING_1
        result.getAcceptanceDate() != null
        result.getActivity() == activity
        result.getVolunteer() == volunteer

        and: "invocations"
        1 * activity.addParticipation(_)
        1 * volunteer.addParticipation(_)
    }

    @Unroll
    def "create participation and violate participants limit invariant"() {
        given:
        activity.getParticipations() >> [otherParticipation]
        activity.getParticipantsNumberLimit() >> 1

        when:
        new Participation(activity, volunteer, participationDto)

        then:
        def exception = thrown(HEException)
        exception.getErrorMessage() == ErrorMessage.LIMIT_OF_ACTIVITY_PARTICIPANTS_REACHED
    }

    @Unroll
    def "create participation and violate volunteer can only participate once in an activity invariant"() {
        given:
        otherParticipation.getActivity() >> activity
        volunteer.getParticipations() >> [otherParticipation]

        when:
        new Participation(activity, volunteer, participationDto)

        then:
        def exception = thrown(HEException)
        exception.getErrorMessage() == ErrorMessage.VOLUNTEER_CAN_PARTICIPATE_IN_ACTIVITY_ONLY_ONCE
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}