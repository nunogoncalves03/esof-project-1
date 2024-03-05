package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto;

@RestController
@RequestMapping("/participations")
public class ParticipationController {
    @Autowired
    private ParticipationService participationService;

    private static final Logger logger = LoggerFactory.getLogger(ParticipationService.class);

    @PostMapping("/{activityId}")
    @PreAuthorize("hasRole('ROLE_MEMBER')")
    public ParticipationDto createParticipation(@PathVariable Integer activityId, @Valid @RequestBody ParticipationDto participationDto) {
        return participationService.createParticipation(activityId, participationDto);
    }
}
