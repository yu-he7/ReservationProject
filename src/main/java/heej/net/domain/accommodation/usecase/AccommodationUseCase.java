package heej.net.domain.accommodation.usecase;

import heej.net.domain.accommodation.api.dto.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AccommodationUseCase {
    // 숙소 목록 조회 (검색, 필터링, 페이징)
    Page<AccommodationResponse> searchAccommodations(AccommodationSearchRequest request);

    // 숙소 상세 조회 (이미지 및 객실 목록 포함)
    AccommodationDetailResponse getAccommodationDetail(Long accommodationId);

    // 객실 상세 조회 (이미지 포함)
    RoomDetailResponse getRoomDetail(Long roomId);

    // 전체 객실 목록 조회
    List<RoomResponse> getAllRooms();

    // 특정 숙소의 객실 목록 조회
    Page<RoomResponse> getRoomsByAccommodation(Long accommodationId, int page, int size);

    // 숙소 등록
    AccommodationResponse createAccommodation(AccommodationCreateRequest request);

    // 객실 등록
    RoomResponse createRoom(RoomCreateRequest request);
}
