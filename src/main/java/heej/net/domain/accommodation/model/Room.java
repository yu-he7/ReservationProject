package heej.net.domain.accommodation.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_id", nullable = false)
    private Accommodation accommodation;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomType type;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Integer maxCapacity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Integer size;

    @Column(length = 200)
    private String mainImage;

    @Column(columnDefinition = "TEXT")
    private String amenitiesJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomStatus status;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RoomImage> images = new ArrayList<>();

    @Version
    private Long version;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public List<String> getAmenities() {
        if (amenitiesJson == null || amenitiesJson.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(amenitiesJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    public void setAmenities(List<String> amenities) {
        try {
            this.amenitiesJson = objectMapper.writeValueAsString(amenities);
        } catch (JsonProcessingException e) {
            this.amenitiesJson = "[]";
        }
    }

    public void updateInfo(String name, String description, Integer capacity, Integer maxCapacity, Integer size) {
        this.name = name;
        this.description = description;
        this.capacity = capacity;
        this.maxCapacity = maxCapacity;
        this.size = size;
    }

    public void updatePrice(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public void updateMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public void changeStatus(RoomStatus status) {
        this.status = status;
    }
}

