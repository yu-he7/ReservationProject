package heej.net.domain.accommodation.api;

import heej.net.domain.accommodation.api.dto.RoomCreateRequest;
import heej.net.domain.accommodation.api.dto.RoomDetailResponse;
import heej.net.domain.accommodation.api.dto.RoomResponse;
import heej.net.domain.accommodation.usecase.AccommodationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomEndpoint {

    private final AccommodationUseCase accommodationUseCase;

    /**
     * 전체 객실 목록 조회
     * GET /api/rooms
     */
    @GetMapping
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        log.info("전체 객실 목록 조회 API 호출");
        List<RoomResponse> rooms = accommodationUseCase.getAllRooms();
        return ResponseEntity.ok(rooms);
    }

    /**
     * 객실 상세 조회 (이미지 포함)
     * GET /api/rooms/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoomDetailResponse> getRoomDetail(@PathVariable Long id) {
        log.info("객실 상세 조회 API 호출: roomId={}", id);

        RoomDetailResponse response = accommodationUseCase.getRoomDetail(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 객실 등록
     * POST /api/rooms
     */
    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(
            @jakarta.validation.Valid @RequestBody RoomCreateRequest request
    ) {
        log.info("객실 등록 API 호출: accommodationId={}, name={}", request.getAccommodationId(), request.getName());

        RoomResponse response = accommodationUseCase.createRoom(request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(response);
    }
}

