package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain;

import jakarta.persistence.*;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException;
import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "participation")
public class Participation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer rating;

    private LocalDateTime acceptanceDate;

    @ManyToOne
    private Volunteer volunteer;

    @ManyToOne
    private Activity activity;

    public Participation() {
    }

    public Participation(Activity activity, Volunteer volunteer, ParticipationDto participationDto) {
        setRating(participationDto.getRating());
        setActivity(activity);
        setVolunteer(volunteer);
        setAcceptanceDate(DateHandler.now());

        verifyInvariants();
    }

    public Integer getId() {
        return id;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public LocalDateTime getAcceptanceDate() {
        return acceptanceDate;
    }

    public void setAcceptanceDate(LocalDateTime acceptanceDate) {
        this.acceptanceDate = acceptanceDate;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
        activity.addParticipation(this);
    }

    public Volunteer getVolunteer() {
        return volunteer;
    }

    public void setVolunteer(Volunteer volunteer) {
        this.volunteer = volunteer;
        volunteer.addParticipation(this);
    }

    private void verifyInvariants() {
        limitOfActivityParticipantsReached();
        volunteerCanParticipateInActivityOnlyOnce();
        volunteerCanOnlyBecomeParticipantAfterApplicationDeadline();
    }    

    private void limitOfActivityParticipantsReached() {
        if (this.activity.getParticipations() != null &&
                this.activity.getParticipations().size() >= this.activity.getParticipantsNumberLimit()) {
            throw new HEException(LIMIT_OF_ACTIVITY_PARTICIPANTS_REACHED, this.activity.getName());
        }
    }

    private void volunteerCanParticipateInActivityOnlyOnce() {
        if (this.volunteer.getParticipations() != null && this.volunteer.getParticipations().stream()
            .anyMatch(participation -> participation != this && participation.getActivity().getId().equals(this.activity.getId()))) {
                throw new HEException(VOLUNTEER_CAN_PARTICIPATE_IN_ACTIVITY_ONLY_ONCE, this.activity.getName());
            }
    }

    private void volunteerCanOnlyBecomeParticipantAfterApplicationDeadline() {
        if (this.activity.getApplicationDeadline().isAfter(this.acceptanceDate)) {
            throw new HEException(VOLUNTEER_CAN_ONLY_BECOME_PARTICIPANT_AFTER_APPLICATION_DEADLINE);
        }
    }

}
