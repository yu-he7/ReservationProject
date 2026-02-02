package heej.net.domain.reservation.model;

import heej.net.domain.accommodation.model.Room;
import heej.net.domain.member.model.Member;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations", indexes = {
        @Index(name = "idx_reservation_member", columnList = "member_id"),
        @Index(name = "idx_reservation_room", columnList = "room_id"),
        @Index(name = "idx_reservation_dates", columnList = "checkInDate, checkOutDate")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private LocalDate checkInDate;

    @Column(nullable = false)
    private LocalDate checkOutDate;

    @Column(nullable = false)
    private Integer guestCount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(length = 500)
    private String specialRequests;

    @Column(length = 500)
    private String cancellationReason;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
    }

    public void cancel(String reason) {
        this.status = ReservationStatus.CANCELLED;
        this.cancellationReason = reason;
    }

    public void complete() {
        this.status = ReservationStatus.COMPLETED;
    }

    public boolean isActive() {
        return status == ReservationStatus.PENDING || status == ReservationStatus.CONFIRMED;
    }
}

