package heej.net.config;

import heej.net.domain.accommodation.model.*;
import heej.net.domain.accommodation.infra.AccommodationJpaRepository;
import heej.net.domain.accommodation.infra.RoomJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    @Bean
    @Profile("!test") // 테스트 환경에서는 실행하지 않음
    public CommandLineRunner initData(
            AccommodationJpaRepository accommodationRepository,
            RoomJpaRepository roomRepository) {

        return args -> {
            // 이미 데이터가 있으면 초기화하지 않음
            if (accommodationRepository.count() > 0) {
                log.info("Data already exists. Skipping initialization.");
                return;
            }

            log.info("Initializing sample data...");

            // 숙소 1: 그랜드 호텔
            Accommodation hotel = Accommodation.builder()
                    .name("그랜드 호텔")
                    .type(AccommodationType.HOTEL)
                    .description("도심 속 프리미엄 비즈니스 호텔")
                    .phone("02-1234-5678")
                    .address("서울특별시 강남구 테헤란로 123")
                    .city("서울")
                    .region("강남구")
                    .latitude(new BigDecimal("37.5012"))
                    .longitude(new BigDecimal("127.0396"))
                    .mainImage("/images/hotel1.jpg")
                    .status(AccommodationStatus.ACTIVE)
                    .rating(5)
                    .rooms(new ArrayList<>())
                    .images(new ArrayList<>())
                    .build();
            accommodationRepository.save(hotel);

            // 객실 1-1: 스탠다드 더블룸
            Room room1 = Room.builder()
                    .accommodation(hotel)
                    .name("스탠다드 더블룸")
                    .type(RoomType.DOUBLE)
                    .capacity(2)
                    .maxCapacity(2)
                    .pricePerNight(BigDecimal.valueOf(100000))
                    .description("편안한 더블 침대가 있는 스탠다드 룸")
                    .size(33)
                    .mainImage("/images/room1.jpg")
                    .status(RoomStatus.AVAILABLE)
                    .images(new ArrayList<>())
                    .build();
            room1.setAmenities(List.of("무료 Wi-Fi", "에어컨", "TV", "미니바", "헤어드라이어"));
            roomRepository.save(room1);

            // 객실 1-2: 디럭스 트윈룸
            Room room2 = Room.builder()
                    .accommodation(hotel)
                    .name("디럭스 트윈룸")
                    .type(RoomType.TWIN)
                    .capacity(2)
                    .maxCapacity(3)
                    .pricePerNight(BigDecimal.valueOf(150000))
                    .description("넓은 공간과 트윈 침대가 있는 디럭스 룸")
                    .size(40)
                    .mainImage("/images/room2.jpg")
                    .status(RoomStatus.AVAILABLE)
                    .images(new ArrayList<>())
                    .build();
            room2.setAmenities(List.of("무료 Wi-Fi", "에어컨", "TV", "미니바", "헤어드라이어", "커피머신", "욕조"));
            roomRepository.save(room2);

            // 숙소 2: 해변 리조트
            Accommodation resort = Accommodation.builder()
                    .name("해변 리조트")
                    .type(AccommodationType.RESORT)
                    .description("아름다운 바다 전망의 리조트")
                    .phone("064-1234-5678")
                    .address("제주특별자치도 서귀포시 중문관광로 72번길 75")
                    .city("제주")
                    .region("서귀포시")
                    .latitude(new BigDecimal("33.2463"))
                    .longitude(new BigDecimal("126.4125"))
                    .mainImage("/images/resort1.jpg")
                    .status(AccommodationStatus.ACTIVE)
                    .rating(5)
                    .rooms(new ArrayList<>())
                    .images(new ArrayList<>())
                    .build();
            accommodationRepository.save(resort);

            // 객실 2-1: 오션뷰 스위트
            Room room3 = Room.builder()
                    .accommodation(resort)
                    .name("오션뷰 스위트")
                    .type(RoomType.SUITE)
                    .capacity(4)
                    .maxCapacity(4)
                    .pricePerNight(BigDecimal.valueOf(300000))
                    .description("탁 트인 바다 전망의 프리미엄 스위트룸")
                    .size(66)
                    .mainImage("/images/room3.jpg")
                    .status(RoomStatus.AVAILABLE)
                    .images(new ArrayList<>())
                    .build();
            room3.setAmenities(List.of("무료 Wi-Fi", "에어컨", "TV", "미니바", "헤어드라이어", "커피머신", "욕조", "발코니", "주방"));
            roomRepository.save(room3);

            // 숙소 3: 도시 게스트하우스
            Accommodation guesthouse = Accommodation.builder()
                    .name("도시 게스트하우스")
                    .type(AccommodationType.GUESTHOUSE)
                    .description("합리적인 가격의 깔끔한 게스트하우스")
                    .phone("02-9876-5432")
                    .address("서울특별시 마포구 홍익로 123")
                    .city("서울")
                    .region("마포구")
                    .latitude(new BigDecimal("37.5563"))
                    .longitude(new BigDecimal("126.9245"))
                    .mainImage("/images/guesthouse1.jpg")
                    .status(AccommodationStatus.ACTIVE)
                    .rating(4)
                    .rooms(new ArrayList<>())
                    .images(new ArrayList<>())
                    .build();
            accommodationRepository.save(guesthouse);

            // 객실 3-1: 도미토리 4인실
            Room room4 = Room.builder()
                    .accommodation(guesthouse)
                    .name("도미토리 4인실")
                    .type(RoomType.FAMILY)
                    .capacity(4)
                    .maxCapacity(4)
                    .pricePerNight(BigDecimal.valueOf(30000))
                    .description("경제적인 도미토리 룸")
                    .size(20)
                    .mainImage("/images/room4.jpg")
                    .status(RoomStatus.AVAILABLE)
                    .images(new ArrayList<>())
                    .build();
            room4.setAmenities(List.of("무료 Wi-Fi", "에어컨", "공용 샤워실", "사물함"));
            roomRepository.save(room4);

            log.info("Sample data initialized successfully!");
            log.info("Created {} accommodations and {} rooms",
                    accommodationRepository.count(),
                    roomRepository.count());
        };
    }
}

