package heej.net.domain.accommodation.usecase;

import heej.net.domain.accommodation.api.dto.*;
import heej.net.domain.accommodation.infra.AccommodationInfra;
import heej.net.domain.accommodation.infra.RoomInfra;
import heej.net.domain.accommodation.model.Accommodation;
import heej.net.domain.accommodation.model.AccommodationImage;
import heej.net.domain.accommodation.model.Room;
import heej.net.domain.accommodation.model.RoomImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccommodationUseCaseImpl implements AccommodationUseCase {

    private final AccommodationInfra accommodationInfra;
    private final RoomInfra roomInfra;

    @Override
    public Page<AccommodationResponse> searchAccommodations(AccommodationSearchRequest request) {
        log.info("숙소 검색 요청: keyword={}, city={}, region={}, type={}",
                request.getKeyword(), request.getCity(), request.getRegion(), request.getType());

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        Page<Accommodation> accommodations = accommodationInfra.searchAccommodations(
                request.getKeyword(),
                request.getCity(),
                request.getRegion(),
                request.getType(),
                request.getMinPrice(),
                request.getMaxPrice(),
                request.getMinRating(),
                pageable
        );

        return accommodations.map(this::toAccommodationResponse);
    }

    @Override
    public AccommodationDetailResponse getAccommodationDetail(Long accommodationId) {
        log.info("숙소 상세 조회 요청: accommodationId={}", accommodationId);

        Accommodation accommodation = accommodationInfra.findById(accommodationId)
                .orElseThrow(() -> new IllegalArgumentException("숙소를 찾을 수 없습니다: " + accommodationId));

        List<Room> rooms = roomInfra.findAllByAccommodationId(accommodationId);

        return AccommodationDetailResponse.builder()
                .id(accommodation.getId())
                .name(accommodation.getName())
                .type(accommodation.getType())
                .description(accommodation.getDescription())
                .address(accommodation.getAddress())
                .city(accommodation.getCity())
                .region(accommodation.getRegion())
                .latitude(accommodation.getLatitude())
                .longitude(accommodation.getLongitude())
                .phone(accommodation.getPhone())
                .mainImage(accommodation.getMainImage())
                .status(accommodation.getStatus())
                .rating(accommodation.getRating())
                .images(accommodation.getImages().stream()
                        .map(AccommodationImage::getImageUrl)
                        .collect(Collectors.toList()))
                .rooms(rooms.stream()
                        .map(this::toRoomResponse)
                        .collect(Collectors.toList()))
                .createdAt(accommodation.getCreatedAt())
                .updatedAt(accommodation.getUpdatedAt())
                .build();
    }

    @Override
    public RoomDetailResponse getRoomDetail(Long roomId) {
        log.info("객실 상세 조회 요청: roomId={}", roomId);

        Room room = roomInfra.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("객실을 찾을 수 없습니다: " + roomId));

        return RoomDetailResponse.builder()
                .id(room.getId())
                .accommodationId(room.getAccommodation().getId())
                .accommodationName(room.getAccommodation().getName())
                .name(room.getName())
                .type(room.getType())
                .capacity(room.getCapacity())
                .maxCapacity(room.getMaxCapacity())
                .pricePerNight(room.getPricePerNight())
                .description(room.getDescription())
                .size(room.getSize())
                .mainImage(room.getMainImage())
                .images(room.getImages().stream()
                        .map(RoomImage::getImageUrl)
                        .collect(Collectors.toList()))
                .status(room.getStatus())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }

    @Override
    public List<RoomResponse> getAllRooms() {
        log.info("전체 객실 목록 조회 요청");
        List<Room> rooms = roomInfra.findAll();
        return rooms.stream()
                .map(this::toRoomResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<RoomResponse> getRoomsByAccommodation(Long accommodationId, int page, int size) {
        log.info("숙소별 객실 목록 조회 요청: accommodationId={}", accommodationId);

        Pageable pageable = PageRequest.of(page, size);
        Page<Room> rooms = roomInfra.findByAccommodationId(accommodationId, pageable);

        return rooms.map(this::toRoomResponse);
    }

    @Override
    @Transactional
    public AccommodationResponse createAccommodation(AccommodationCreateRequest request) {
        log.info("숙소 등록 요청: name={}, type={}, city={}", request.getName(), request.getType(), request.getCity());

        Accommodation accommodation = Accommodation.builder()
                .name(request.getName())
                .type(request.getType())
                .description(request.getDescription())
                .address(request.getAddress())
                .city(request.getCity())
                .region(request.getRegion())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .phone(request.getPhone())
                .mainImage(request.getMainImage())
                .status(heej.net.domain.accommodation.model.AccommodationStatus.ACTIVE)
                .rating(0)
                .build();

        Accommodation saved = accommodationInfra.save(accommodation);
        log.info("숙소 등록 완료: id={}", saved.getId());

        return toAccommodationResponse(saved);
    }

    @Override
    @Transactional
    public RoomResponse createRoom(RoomCreateRequest request) {
        log.info("객실 등록 요청: accommodationId={}, name={}, type={}",
                request.getAccommodationId(), request.getName(), request.getType());

        Accommodation accommodation = accommodationInfra.findById(request.getAccommodationId())
                .orElseThrow(() -> new IllegalArgumentException("숙소를 찾을 수 없습니다: " + request.getAccommodationId()));

        Room room = Room.builder()
                .accommodation(accommodation)
                .name(request.getName())
                .type(request.getType())
                .capacity(request.getCapacity())
                .maxCapacity(request.getMaxCapacity())
                .pricePerNight(request.getPricePerNight())
                .description(request.getDescription())
                .size(request.getSize())
                .mainImage(request.getMainImage())
                .status(heej.net.domain.accommodation.model.RoomStatus.AVAILABLE)
                .build();

        Room saved = roomInfra.save(room);
        log.info("객실 등록 완료: id={}", saved.getId());

        return toRoomResponse(saved);
    }

    private AccommodationResponse toAccommodationResponse(Accommodation accommodation) {
        return AccommodationResponse.builder()
                .id(accommodation.getId())
                .name(accommodation.getName())
                .type(accommodation.getType())
                .description(accommodation.getDescription())
                .address(accommodation.getAddress())
                .city(accommodation.getCity())
                .region(accommodation.getRegion())
                .latitude(accommodation.getLatitude())
                .longitude(accommodation.getLongitude())
                .phone(accommodation.getPhone())
                .mainImage(accommodation.getMainImage())
                .status(accommodation.getStatus())
                .rating(accommodation.getRating())
                .createdAt(accommodation.getCreatedAt())
                .updatedAt(accommodation.getUpdatedAt())
                .build();
    }

    private RoomResponse toRoomResponse(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .accommodationId(room.getAccommodation().getId())
                .accommodationName(room.getAccommodation().getName())
                .name(room.getName())
                .type(room.getType())
                .capacity(room.getCapacity())
                .maxCapacity(room.getMaxCapacity())
                .pricePerNight(room.getPricePerNight())
                .description(room.getDescription())
                .size(room.getSize())
                .mainImage(room.getMainImage())
                .status(room.getStatus())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }
}

