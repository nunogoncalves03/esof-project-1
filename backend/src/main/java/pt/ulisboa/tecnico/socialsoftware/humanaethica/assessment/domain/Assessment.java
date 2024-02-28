package pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer;

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
        setReviewDateTime(assessmentDto.getReviewDate());
    }
    
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
