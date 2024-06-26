package pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions;

public enum ErrorMessage {
    INVALID_TYPE_FOR_AUTH_USER("Invalid type for auth user"),
    INVALID_AUTH_USERNAME("Username: %s, belongs to a different authentication method"),
    INVALID_INSTITUTION_NAME("Name: %s, is not valid"),
    USERNAME_ALREADY_EXIST("Username: %s, already exist"),
    NIF_ALREADY_EXIST("Institution with NIF: %s, already exist"),
    INVALID_EMAIL("The mail %s is invalid."),
    INVALID_NIF("The NIF %s is invalid."),
    INVALID_PASSWORD("The password %s is invalid."),
    INVALID_ROLE("The Role %s is invalid."),
    INVALID_STATE("The State %s is invalid"),
    AUTHUSER_NOT_FOUND("AuthUser not found with id %d"),
    USER_NOT_FOUND("User not found with username %s"),
    INSTITUTION_NOT_FOUND("Institution not found with id %d"),
    USER_NOT_APPROVED("The member of this institution is not yet approved"),
    USER_ALREADY_ACTIVE("User is already active with username %s"),
    INVALID_CONFIRMATION_TOKEN("Invalid confirmation token"),
    EXPIRED_CONFIRMATION_TOKEN("Expired confirmation token"),
    INVALID_LOGIN_CREDENTIALS("Invalid login credentials"),
    DUPLICATE_USER("Duplicate user: %s"),
    INVALID_THEME_NAME("Name: %s, is not valid"),
    THEME_NOT_FOUND("Theme not found with id %d"),
    ACCESS_DENIED("You do not have permission to view this resource"),
    THEME_ALREADY_EXISTS("This theme already exists"),
    THEME_CAN_NOT_BE_DELETED("Theme %s can not be deleted because has associated Institutions"),
    THEME_NOT_APPROVED("Theme %s is not yet approved"),

    // Activity
    ACTIVITY_NOT_FOUND("Activity not found with id %d"),
    ACTIVITY_NAME_INVALID("Activity Name: %s, is not valid"),
    ACTIVITY_REGION_NAME_INVALID("Region Name: %s, is not valid"),
    ACTIVITY_SHOULD_HAVE_ONE_TO_FIVE_PARTICIPANTS("Activity should have one to five participants"),
    ACTIVITY_DESCRIPTION_INVALID("Activity description is missing"),
    ACTIVITY_INVALID_DATE("Date format for %s is invalid"),
    ACTIVITY_APPLICATION_DEADLINE_AFTER_START("Activity application deadline is after start"),
    ACTIVITY_START_AFTER_END("Activity start is after end"),
    ACTIVITY_ALREADY_APPROVED("Activity is already approved with name %s"),
    ACTIVITY_ALREADY_EXISTS("Activity already exists in database"),
    ACTIVITY_ALREADY_SUSPENDED("Activity is already suspended with name %s"),
    ACTIVITY_ALREADY_REPORTED("Activity is already reported with name %s"),

    // Enrollment
    ENROLLMENT_MOTIVATION_SHOULD_HAVE_AT_LEAST_TEN_CHARACTERS("Motivation should have at least ten characters"),
    ENROLLMENT_VOLUNTEER_CAN_ONLY_ENROLL_IN_ACTIVITY_ONCE("Volunteer can only enroll in activity once"),
    ENROLLMENT_VOLUNTEER_CANT_ENROLL_IN_ACTIVITY_AFTER_ENDING_DATE("Volunteer can't enroll in activity after end date"),
    
    // Participation
    LIMIT_OF_ACTIVITY_PARTICIPANTS_REACHED("Activity with name %s has reached its limit of participants"),
    VOLUNTEER_CAN_PARTICIPATE_IN_ACTIVITY_ONLY_ONCE("Volunteer is already a participant in activity with name %s"),
    VOLUNTEER_CAN_ONLY_BECOME_PARTICIPANT_AFTER_APPLICATION_DEADLINE("Volunteer can't become participant before application deadline"),
    VOLUNTEER_NOT_FOUND("Volunteer not found with id %d"),

    // Assessment
    ASSESSMENT_REVIEW_INVALID("Review must have at least 10 characters"),
    ASSESSMENT_INSTITUTION_SHOULD_HAVE_ONE_FINISHED_ACTIVITY("Institution should have at least one finished activity"),
    ASSESSMENT_VOLUNTEER_ASSESSING_SAME_INSTITUTION_AGAIN("Volunteer can't assess institution more than once");

    public final String label;

    ErrorMessage(String label) {
        this.label = label;
    }
}