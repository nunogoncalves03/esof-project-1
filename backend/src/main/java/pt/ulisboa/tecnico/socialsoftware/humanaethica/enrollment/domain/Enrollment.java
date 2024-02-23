package pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.domain;

import jakarta.persistence.*;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.dto.EnrollmentDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer;

import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "enrollment")
public class Enrollment {

    private static final int MINIMUM_MOTIVATION_LENGTH = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String motivation;

    private LocalDateTime enrollmentDateTime;

    @ManyToOne
    private Activity activity;

    @ManyToOne
    private Volunteer volunteer;

    public Enrollment() {
    }

    public Enrollment(Activity activity, Volunteer volunteer, EnrollmentDto enrollmentDto) {
        setMotivation(enrollmentDto.getMotivation());
        setEnrollmentDateTime(LocalDateTime.now());
        setActivity(activity);
        setVolunteer(volunteer);

        verifyInvariants();
    }

    public Integer getId() {
        return id;
    }

    public String getMotivation() {
        return motivation;
    }

    public void setMotivation(String motivation) {
        this.motivation = motivation;
    }

    public LocalDateTime getEnrollmentDateTime() {
        return enrollmentDateTime;
    }

    public void setEnrollmentDateTime(LocalDateTime enrollmentDateTime) {
        this.enrollmentDateTime = enrollmentDateTime;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
        activity.addEnrollment(this);
    }

    public Volunteer getVolunteer() {
        return volunteer;
    }

    public void setVolunteer(Volunteer volunteer) {
        this.volunteer = volunteer;
        volunteer.addEnrollment(this);
    }

    private void verifyInvariants() {
        motivationHasAtLeastTenCharacters();
        volunteerCanOnlyEnrollInActivityOnce();
        volunteerCantEnrollInActivityAfterEndingDate();
    }

    private void motivationHasAtLeastTenCharacters() {
        if (this.motivation.length() < Enrollment.MINIMUM_MOTIVATION_LENGTH) {
            throw new HEException(ENROLLMENT_MOTIVATION_SHOULD_HAVE_AT_LEAST_TEN_CHARACTERS);
        }
    }

    private void volunteerCanOnlyEnrollInActivityOnce() {
        if (this.volunteer.getEnrollments().stream()
                .anyMatch(enrollment -> enrollment.getActivity().equals(this.activity))) {
            throw new HEException(ENROLLMENT_VOLUNTEER_CAN_ONLY_ENROLL_IN_ACTIVITY_ONCE);
        }
    }

    private void volunteerCantEnrollInActivityAfterEndingDate() {
        if (this.activity.getEndingDate().isBefore(this.enrollmentDateTime)) {
            throw new HEException(ENROLLMENT_VOLUNTEER_CANT_ENROLL_IN_ACTIVITY_AFTER_ENDING_DATE);
        }
    }
}
