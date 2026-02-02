package heej.net.domain.reservation.usecase;

import heej.net.domain.accommodation.infra.RoomInfra;
import heej.net.domain.accommodation.model.Room;
import heej.net.domain.holiday.usecase.HolidayUseCase;
import heej.net.domain.member.infra.MemberInfra;
import heej.net.domain.member.model.Member;
import heej.net.domain.reservation.api.dto.CheckAvailabilityResponse;
import heej.net.domain.reservation.api.dto.CreateReservationRequest;
import heej.net.domain.reservation.api.dto.ReservationCancelResponse;
import heej.net.domain.reservation.api.dto.ReservationResponse;
import heej.net.domain.reservation.infra.ReservationInfra;
import heej.net.domain.reservation.model.Reservation;
import heej.net.domain.reservation.model.ReservationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReservationUseCaseImpl implements ReservationUseCase {

    private final ReservationInfra reservationInfra;
    private final MemberInfra memberInfra;
    private final RoomInfra roomInfra;
    private final HolidayUseCase holidayUseCase;

    @Override
    @Transactional
    public ReservationResponse createReservation(Long memberId, CreateReservationRequest request) {
        log.info("Creating reservation for member: {}, room: {}", memberId, request.getRoomId());

        validateReservationRequest(request);

        Member member = memberInfra.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        Room room = roomInfra.findByIdWithLock(request.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("객실을 찾을 수 없습니다."));

        validateNotHoliday(request.getCheckInDate(), request.getCheckOutDate());

        if (reservationInfra.hasOverlappingReservationWithLock(
                request.getRoomId(),
                request.getCheckInDate(),
                request.getCheckOutDate())) {
            throw new IllegalArgumentException("해당 기간에 이미 예약이 있습니다.");
        }

        if (request.getGuestCount() > room.getMaxCapacity()) {
            throw new IllegalArgumentException(
                    String.format("최대 수용 인원(%d명)을 초과했습니다.", room.getMaxCapacity()));
        }

        long nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        BigDecimal totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        Reservation reservation = Reservation.builder()
                .member(member)
                .room(room)
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .guestCount(request.getGuestCount())
                .totalPrice(totalPrice)
                .status(ReservationStatus.PENDING)
                .specialRequests(request.getSpecialRequests())
                .build();

        Reservation savedReservation = reservationInfra.save(reservation);
        log.info("Reservation created: id={}", savedReservation.getId());

        return toResponse(savedReservation);
    }

    @Override
    @Transactional
    public void cancelReservation(Long memberId, Long reservationId, String reason) {
        log.info("Cancelling reservation: memberId={}, reservationId={}", memberId, reservationId);

        Reservation reservation = reservationInfra.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        // 본인 예약 확인
        if (!reservation.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인의 예약만 취소할 수 있습니다.");
        }

        // 이미 취소되었거나 완료된 예약인지 확인
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalArgumentException("이미 취소된 예약입니다.");
        }
        if (reservation.getStatus() == ReservationStatus.COMPLETED) {
            throw new IllegalArgumentException("이미 완료된 예약은 취소할 수 없습니다.");
        }

        reservation.cancel(reason);
        reservationInfra.save(reservation);
        log.info("Reservation cancelled successfully: id={}", reservationId);
    }

    @Override
    public List<ReservationResponse> getMyReservations(Long memberId) {
        List<Reservation> reservations = reservationInfra.findByMemberId(memberId);
        return reservations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ReservationResponse getReservation(Long memberId, Long reservationId) {
        Reservation reservation = reservationInfra.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        // 본인 예약 확인
        if (!reservation.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인의 예약만 조회할 수 있습니다.");
        }

        return toResponse(reservation);
    }
    @Override
    @Transactional(readOnly = true)
    public List<CheckAvailabilityResponse> checkAvailability(
            Long accommodationId,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            Integer guestCount) {
        log.info("Checking availability for accommodation: {}, dates: {} ~ {}, guests: {}",
                accommodationId, checkInDate, checkOutDate, guestCount);

        List<Room> rooms;
        if (accommodationId != null) {
            // 특정 숙소의 객실만 조회
            rooms = roomInfra.findAllByAccommodationId(accommodationId);
        } else {
            // 전체 객실 조회 (guestCount 필터 적용)
            if (guestCount != null) {
                rooms = roomInfra.findByMaxCapacityGreaterThanEqual(guestCount);
            } else {
                rooms = roomInfra.findAll();
            }
        }

        if (rooms.isEmpty()) {
            log.warn("No rooms found with given criteria");
            return List.of(); // 빈 리스트 반환 (예외 대신)
        }

        return rooms.stream()
                .filter(room -> {
                    if (guestCount != null && room.getMaxCapacity() < guestCount) {
                        return false;
                    }
                    return true;
                })
                .map(room -> {
                    // 예약 가능 여부 확인 (중복 예약 체크)
                    boolean hasOverlapping = reservationInfra.hasOverlappingReservation(
                            room.getId(), checkInDate, checkOutDate);
                    boolean isAvailable = !hasOverlapping;

                    // 숙박일수 계산
                    long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);

                    // 총 가격 계산
                    BigDecimal totalPrice = room.getPricePerNight()
                            .multiply(BigDecimal.valueOf(nights));

                    return CheckAvailabilityResponse.builder()
                            .roomId(room.getId())
                            .roomName(room.getName())
                            .roomType(room.getType().toString())
                            .availableCount(isAvailable ? 1 : 0)
                            .pricePerNight(room.getPricePerNight())
                            .maxOccupancy(room.getMaxCapacity())
                            .available(isAvailable)
                            .amenities(room.getAmenities())
                            .totalPrice(totalPrice)
                            .nights((int) nights)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private void validateReservationRequest(CreateReservationRequest request) {
        // 체크아웃이 체크인보다 이후인지 확인
        if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new IllegalArgumentException("체크아웃 날짜는 체크인 날짜보다 이후여야 합니다.");
        }

        // 오늘 이후 날짜인지 확인
        LocalDate today = LocalDate.now();
        if (request.getCheckInDate().isBefore(today)) {
            throw new IllegalArgumentException("체크인 날짜는 오늘 이후여야 합니다.");
        }

        // 최대 30일 이내 예약만 가능
        long nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        if (nights > 30) {
            throw new IllegalArgumentException("최대 30일까지만 예약 가능합니다.");
        }
    }

    private void validateNotHoliday(LocalDate checkInDate, LocalDate checkOutDate) {
        // 체크인 날짜부터 체크아웃 전날까지 공휴일 체크
        LocalDate date = checkInDate;
        while (date.isBefore(checkOutDate)) {
            boolean isHoliday = holidayUseCase.isHoliday(date);
            log.debug("Checking holiday for date: {}, isHoliday: {}", date, isHoliday);

            if (isHoliday) {
                String holidayName = holidayUseCase.getHoliday(date) != null
                        ? holidayUseCase.getHoliday(date).getHolidayName()
                        : "";
                log.warn("Holiday detected: date={}, name={}", date, holidayName);
                throw new IllegalArgumentException(
                        String.format("공휴일(%s, %s)에는 예약할 수 없습니다.",
                                date, holidayName));
            }
            date = date.plusDays(1);
        }
        log.info("No holidays found in reservation period: {} ~ {}", checkInDate, checkOutDate);
    }

    private ReservationResponse toResponse(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .memberId(reservation.getMember().getId())
                .memberName(reservation.getMember().getName())
                .roomId(reservation.getRoom().getId())
                .roomName(reservation.getRoom().getName())
                .accommodationName(reservation.getRoom().getAccommodation().getName())
                .checkInDate(reservation.getCheckInDate())
                .checkOutDate(reservation.getCheckOutDate())
                .guestCount(reservation.getGuestCount())
                .totalPrice(reservation.getTotalPrice())
                .status(reservation.getStatus())
                .specialRequests(reservation.getSpecialRequests())
                .cancellationReason(reservation.getCancellationReason())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }


}

