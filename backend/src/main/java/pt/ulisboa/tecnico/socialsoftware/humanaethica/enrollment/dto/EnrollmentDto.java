package pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.dto;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.dto.ActivityDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.domain.Enrollment;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.dto.UserDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler;

public class EnrollmentDto {
    private Integer id;
    private String motivation;
    private String enrollmentDateTime;
    private int activityId;
    private int volunteerId;

    public EnrollmentDto() {
    }

    public EnrollmentDto(Enrollment enrollment) {
        setId(enrollment.getId());
        setMotivation(enrollment.getMotivation());
        setEnrollmentDateTime(DateHandler.toISOString(enrollment.getEnrollmentDateTime()));
        setActivityId(enrollment.getActivity().getId());
        setVolunteerId(enrollment.getVolunteer().getId());
    }

    public Integer getId() {
      return id;
    }

    public void setId(Integer id) {
      this.id = id;
    }

    public String getMotivation() {
      return motivation;
    }

    public void setMotivation(String motivation) {
      this.motivation = motivation;
    }

    public String getEnrollmentDateTime() {
      return enrollmentDateTime;
    }

    public void setEnrollmentDateTime(String enrollmentDateTime) {
      this.enrollmentDateTime = enrollmentDateTime;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public int getVolunteerId() {
        return volunteerId;
    }

    public void setVolunteerId(int volunteerId) {
        this.volunteerId = volunteerId;
    }

    @Override
    public String toString() {
        return "EnrollmentDto{" +
                "id=" + id +
                ", motivation='" + motivation + '\'' +
                ", enrollmentDateTime='" + enrollmentDateTime + '\'' +
                ", activityId=" + activityId +
                ", volunteerId=" + volunteerId +
                '}';
    }
}
