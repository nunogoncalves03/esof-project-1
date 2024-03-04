package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain.Participation;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler;

public class ParticipationDto {
    private Integer id;
    private Integer rating;
    private String acceptanceDate;
    private Integer volunteerId;
    private Integer activityId;

    public ParticipationDto() {
    }

    public ParticipationDto(Participation participation) {
        setId(participation.getId());
        setAcceptanceDate(DateHandler.toISOString(participation.getAcceptanceDate()));
        setVolunteerId(participation.getVolunteer().getId());
        setActivityId(participation.getActivity().getId());

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

    public Integer getVolunteerId() {
        return volunteerId;
    }

    public void setVolunteerId(Integer volunteerId) {
        this.volunteerId = volunteerId;
    }

    public Integer getActivityId() {
        return activityId;
    }

    public void setActivityId(Integer activityId) {
        this.activityId = activityId;
    }

    @Override
    public String toString() {
        return "ParticipationDto{" +
                "id=" + id +
                ", rating='" + rating + '\'' +
                ", acceptanceDate='" + acceptanceDate + '\'' +
                ", activity=" + activityId +
                ", volunteer=" + volunteerId +
                '}';
    }
}
