package pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.dto.AssessmentDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler;

import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*;

@Entity
@Table(name = "assessment")
public class Assessment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String review;

    private LocalDateTime reviewDate;
    
    @ManyToOne
    private Institution institution;
    
    @ManyToOne
    private Volunteer volunteer;
    
    public Assessment() {
    }
    
    public Assessment(AssessmentDto assessmentDto, Institution institution, Volunteer volunteer) {
        setVolunteer(volunteer);
        setInstitution(institution);
        setReview(assessmentDto.getReview());
        setReviewDateTime(DateHandler.toLocalDateTime(assessmentDto.getReviewDate()));
    }

    private void reviewHasAtLeast10Characters() {
        if (this.review == null || this.review.length() < 10) {
            throw new HEException(ASSESSMENT_REVIEW_INVALID);
        }
    }
    
    private void institutionHasOneFinishedActivity() {
        if (!this.institution.getActivities().stream()
            .anyMatch(activity -> activity.getEndingDate().isBefore(LocalDateTime.now()))) {
                throw new HEException(ASSESSMENT_INSTITUTION_SHOULD_HAVE_ONE_FINISHED_ACTIVITY);
        }
    }

    private void volunteerAssessingInstitutionAgain() {
        if (this.institution.getAssessments().stream()
                .anyMatch(assessment -> assessment.getVolunteer().getId().equals(this.volunteer.getId()))) {
            throw new HEException(ASSESSMENT_VOLUNTEER_ASSESSING_SAME_INSTITUTION_AGAIN);
        }
    }

    public void setId(Integer id) { this.id = id; }

    public void setReview(String review) {
        this.review = review;
    }

    public void setReviewDateTime(LocalDateTime reviewDate) {
        this.reviewDate = reviewDate;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
        institution.addAssessment(this);
    }

    public void setVolunteer(Volunteer volunteer) {
        this.volunteer = volunteer;
        volunteer.addAssessment(this);
    }

    public Integer getId() { return this.id; }

    public String getReview() {
        return this.review;
    }

    public LocalDateTime getReviewDate() {
        return this.reviewDate;
    }

    public Institution getInstitution() {
        return this.institution;
    }

    public Volunteer getVolunteer() {
        return this.volunteer;
    }
}
