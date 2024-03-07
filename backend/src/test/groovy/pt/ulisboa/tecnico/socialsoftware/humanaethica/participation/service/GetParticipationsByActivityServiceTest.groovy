package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain.Participation
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer
import spock.lang.Unroll

@DataJpaTest
class GetParticipationsByActivityServiceTest extends SpockTest{
    public static final String EXIST = "exist"
    public static final String NO_EXIST = "noExist"

    def activity1
    def activity2

    def setup() {
        given: "2 volunteers and 1 institution"
        def volunteer1 = new Volunteer()
        def volunteer2 = new Volunteer()
        userRepository.save(volunteer1)
        userRepository.save(volunteer2)
        def institution = institutionService.getDemoInstitution()

        and: "2 activities"
        def activityDto = createActivityDto(ACTIVITY_NAME_1,ACTIVITY_REGION_1,PARTICIPATION_ACTIVITY_LIMIT_2,ACTIVITY_DESCRIPTION_1,
                TWO_DAYS_AGO,IN_TWO_DAYS,IN_THREE_DAYS,[])
        activity1 = new Activity(activityDto, institution, [])
        activityRepository.save(activity1)

        activityDto.name = ACTIVITY_NAME_2
        activity2 = new Activity(activityDto, institution, [])
        activityRepository.save(activity2)

        and: "2 participations with activity 1"
        def participationDto = new ParticipationDto()
        participationDto.setRating(PARTICIPATION_RATING_1)

        def participation1 = new Participation(activity1, volunteer1, participationDto)
        participationRepository.save(participation1)

        participationDto.setRating(PARTICIPATION_RATING_2)
        def participation2 = new Participation(activity1, volunteer2, participationDto)
        participationRepository.save(participation2)
    }

    def 'get two participations from same activity'() {
        when:
        def result = participationService.getParticipationsByActivity(activity1.getId())

        then:
        result.size() == 2
        result.get(0).rating == PARTICIPATION_RATING_1
        result.get(1).rating == PARTICIPATION_RATING_2
    }

    def 'get no participations'() {
        when:
        def result = participationService.getParticipationsByActivity(activity2.getId())

        then:
        result.size() == 0
    }

    def 'get participations with invalid argument: activityId=#activityId'() {
        when:
        participationService.getParticipationsByActivity(getActivityId(activityId))

        then:
        def exception = thrown(HEException)
        exception.getErrorMessage() == ErrorMessage.ACTIVITY_NOT_FOUND

        where:
        activityId << [null, NO_EXIST]
    }

    def getActivityId(activityId){
        if (activityId == EXIST) {
            return activity1.getId()
        } else if (activityId == NO_EXIST)
            return 999
        return null
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
