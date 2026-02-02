package heej.net.domain.accommodation.model;

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
@Table(name = "accommodations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Accommodation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccommodationType type;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, length = 200)
    private String address;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 50)
    private String region;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(length = 20)
    private String phone;

    @Column(length = 200)
    private String mainImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccommodationStatus status;

    @Column(nullable = false)
    private Integer rating;

    @OneToMany(mappedBy = "accommodation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Room> rooms = new ArrayList<>();

    @OneToMany(mappedBy = "accommodation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AccommodationImage> images = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void updateInfo(String name, String description, String address, String city, String region) {
        this.name = name;
        this.description = description;
        this.address = address;
        this.city = city;
        this.region = region;
    }

    public void updateLocation(BigDecimal latitude, BigDecimal longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void updateMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public void changeStatus(AccommodationStatus status) {
        this.status = status;
    }

    public void updateRating(Integer rating) {
        this.rating = rating;
    }
}
