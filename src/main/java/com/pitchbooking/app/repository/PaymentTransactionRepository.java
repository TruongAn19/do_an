package com.pitchbooking.app.repository;

import com.pitchbooking.app.domain.PaymentTransaction;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PaymentTransaction p where p.txnRef = :txnRef")
    Optional<PaymentTransaction> findByTxnRefWithLock(@Param("txnRef") String txnRef);
}
