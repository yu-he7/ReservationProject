package heej.net.domain.holiday.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "holidays", indexes = {
        @Index(name = "idx_holiday_date", columnList = "holidayDate")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate holidayDate;

    @Column(nullable = false, length = 100)
    private String holidayName;

    @Column(length = 200)
    private String description;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer month;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void updateInfo(String holidayName, String description) {
        this.holidayName = holidayName;
        this.description = description;
    }
}

