package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer
import spock.lang.Unroll

@DataJpaTest
class CreateParticipationMethodTest extends SpockTest {
    Activity activity = Mock()
    Volunteer volunteer = Mock()
    Participation otherParticipation = Mock()
    def participationDto

    def setup() {
        given: "participation info"
        participationDto = new ParticipationDto()
        participationDto.rating = PARTICIPATION_RATING_1

        and: "activity"
        activity.getId() >> 1
    }

    def "create participation in empty activity with rating, valid acceptance date and no previous participation of volunteer"() {
        given:
        volunteer.getParticipations() >> []
        activity.getParticipations() >> []
        activity.getParticipantsNumberLimit() >> PARTICIPATION_ACTIVITY_LIMIT_1
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
        def activity_2 = new Activity()
        activity_2.addParticipation(otherParticipation)
        activity_2.setParticipantsNumberLimit(PARTICIPATION_ACTIVITY_LIMIT_1)
        activity_2.applicationDeadline >> ONE_DAY_AGO
        volunteer.getParticipations() >> []

        when:
        new Participation(activity_2, volunteer, participationDto)

        then:
        def exception = thrown(HEException)
        exception.getErrorMessage() == ErrorMessage.LIMIT_OF_ACTIVITY_PARTICIPANTS_REACHED
    }

    @Unroll
    def "create participation and violate volunteer can only participate once in an activity invariant"() {
        given:
        activity.applicationDeadline >> ONE_DAY_AGO
        otherParticipation.getActivity() >> activity
        activity.getParticipations() >> [otherParticipation]
        volunteer.getParticipations() >> [otherParticipation]
        activity.getParticipantsNumberLimit() >> PARTICIPATION_ACTIVITY_LIMIT_2

        when:
        new Participation(activity, volunteer, participationDto)

        then:
        def exception = thrown(HEException)
        exception.getErrorMessage() == ErrorMessage.VOLUNTEER_CAN_PARTICIPATE_IN_ACTIVITY_ONLY_ONCE
    }

    def "create participation and violate participation before activity deadline invariant"() {
        given:
        activity.getParticipations() >> []
        volunteer.getParticipations() >> []
        activity.getParticipantsNumberLimit() >> 1
        activity.applicationDeadline >> IN_ONE_DAY

        when:
        new Participation(activity, volunteer, participationDto)

        then:
        def exception = thrown(HEException)
        exception.getErrorMessage() == ErrorMessage.VOLUNTEER_CAN_ONLY_BECOME_PARTICIPANT_AFTER_APPLICATION_DEADLINE
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}