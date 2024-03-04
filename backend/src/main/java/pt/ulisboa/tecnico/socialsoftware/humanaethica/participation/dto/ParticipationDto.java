package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.dto.ActivityDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain.Participation;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.dto.UserDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler;

public class ParticipationDto {
    private Integer id;
    private Integer rating;
    private String acceptanceDate;
    private UserDto volunteer;
    private ActivityDto activity;

    public ParticipationDto() {
    }

    public ParticipationDto(Participation participation) {
        setId(participation.getId());
        setAcceptanceDate(DateHandler.toISOString(participation.getAcceptanceDate()));
        setVolunteer(new UserDto(participation.getVolunteer()));
        setActivity(new ActivityDto(participation.getActivity(), false));

        if (participation.getRating() != null) {
            setRating(participation.getRating());
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getAcceptanceDate() {
        return acceptanceDate;
    }

    public void setAcceptanceDate(String acceptanceDate) {
        this.acceptanceDate = acceptanceDate;
    }

    public UserDto getVolunteer() {
        return volunteer;
    }

    public void setVolunteer(UserDto volunteer) {
        this.volunteer = volunteer;
    }

    public ActivityDto getActivity() {
        return activity;
    }

    public void setActivity(ActivityDto activity) {
        this.activity = activity;
    }

    @Override
    public String toString() {
        return "ParticipationDto{" +
                "id=" + id +
                ", rating='" + rating + '\'' +
                ", acceptanceDate='" + acceptanceDate + '\'' +
                ", activity=" + activity +
                ", volunteer=" + volunteer +
                '}';
    }
}
