package heej.net.domain.reservation.model;

public enum ReservationStatus {
    PENDING,      // 예약 대기
    CONFIRMED,    // 예약 확정
    CANCELLED,    // 예약 취소
    COMPLETED,    // 이용 완료
    NO_SHOW       // 노쇼
}

