package hellojpa;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Embeddable
@Getter @Setter
public class Period {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
