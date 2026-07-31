package com.pitchbooking.app.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter @Entity
@Table(name = "payment_transaction")
public class PaymentTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 32) private String txnRef;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private PaymentType paymentType;
    @Column(nullable = false) private Long entityId;
    @Column(nullable = false) private long expectedAmount;
    @Column(nullable = false) private String orderInfo;
    private String responseCode;
    private boolean successful;
    private LocalDateTime createdAt;
    private LocalDateTime callbackAt;
}
