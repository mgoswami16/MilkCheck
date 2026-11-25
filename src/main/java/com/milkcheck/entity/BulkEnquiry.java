package com.milkcheck.entity;

import com.milkcheck.enums.EnquiryStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "bulk_enquiries")
@Getter
@Setter
@NoArgsConstructor
public class BulkEnquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String refNumber;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 150)
    private String businessName;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(nullable = false, length = 50)
    private String businessType;

    @Column(nullable = false, length = 120)
    private String city;

    @Column(nullable = false, length = 500)
    private String products;

    @Column(nullable = false, length = 100)
    private String estimatedQuantity;

    @Column(nullable = false, length = 40)
    private String frequency;

    @Column(length = 300)
    private String deliveryAddress;

    @Column(nullable = false, length = 1500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private EnquiryStatus status = EnquiryStatus.NEW;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}