package com.pitchbooking.app.repository;

import com.pitchbooking.app.domain.Equipment;
import com.pitchbooking.app.domain.RentalPaymentStatus;
import com.pitchbooking.app.domain.RentalTool;
import com.pitchbooking.app.domain.RentalToolStatus;
import com.pitchbooking.app.domain.RentalType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class RentalToolRepositoryTest {

    @Autowired
    private RentalToolRepository rentalToolRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Test
    void countDailyRentalByEquipment_usesEquipmentIdInsteadOfRentalId() {
        Equipment equipment = new Equipment();
        equipment.setName("Bóng thử nghiệm");
        equipment.setQuantity(10);
        equipment.setBookingStockQuantity(10);
        equipment = equipmentRepository.saveAndFlush(equipment);

        LocalDate rentalDate = LocalDate.of(2026, 8, 10);
        Long courtId = 77L;

        RentalTool unrelatedRental = buildRental(
                equipment.getId() + 1000, courtId, rentalDate,
                RentalType.DAILY, RentalToolStatus.CANCELLED, 500);
        rentalToolRepository.saveAndFlush(unrelatedRental);

        RentalTool targetRental = buildRental(
                equipment.getId(), courtId, rentalDate,
                RentalType.DAILY, RentalToolStatus.RENTING, 100);
        targetRental = rentalToolRepository.saveAndFlush(targetRental);

        RentalTool onSiteRental = buildRental(
                equipment.getId(), courtId, rentalDate,
                RentalType.ON_SITE, RentalToolStatus.RENTING,
                RentalPaymentStatus.PAID, 1, 900);
        rentalToolRepository.saveAndFlush(onSiteRental);

        RentalTool unpaidDailyRental = buildRental(
                equipment.getId(), courtId, rentalDate,
                RentalType.DAILY, RentalToolStatus.RENTING,
                RentalPaymentStatus.UNPAID, 2, 700);
        rentalToolRepository.saveAndFlush(unpaidDailyRental);

        assertThat(targetRental.getId()).isNotEqualTo(equipment.getId());
        assertThat(rentalToolRepository.countEquipmentDailyRentalByCourtAndDateRange(
                equipment.getId(), courtId, rentalDate, rentalDate))
                .isEqualTo(2);
        assertThat(rentalToolRepository.countEquipmentDailyRentalByCourtAndDateRange(
                equipment.getId() + 1000, courtId, rentalDate, rentalDate))
                .isZero();
        assertThat(rentalToolRepository.countDailyRentalByCourtAndDateRange(
                courtId, rentalDate, rentalDate))
                .isEqualTo(2);
        assertThat(rentalToolRepository.sumDailyRevenueByCourtAndDateRange(
                courtId, rentalDate, rentalDate))
                .isEqualTo(100);

        List<Object[]> topEquipments = rentalToolRepository.findTopDailyRentedEquipments(
                null, rentalDate, rentalDate, PageRequest.of(0, 5));
        assertThat(topEquipments).hasSize(1);
        assertThat(topEquipments.get(0)[1]).isEqualTo(3L);
        assertThat(topEquipments.get(0)[2]).isEqualTo(100.0);
    }

    private RentalTool buildRental(
            Long equipmentId,
            Long productId,
            LocalDate rentalDate,
            RentalType type,
            RentalToolStatus status,
            double rentalPrice) {
        return buildRental(
                equipmentId, productId, rentalDate, type, status,
                RentalPaymentStatus.PAID, 1, rentalPrice);
    }

    private RentalTool buildRental(
            Long equipmentId,
            Long productId,
            LocalDate rentalDate,
            RentalType type,
            RentalToolStatus status,
            RentalPaymentStatus paymentStatus,
            int quantity,
            double rentalPrice) {
        RentalTool rental = new RentalTool();
        rental.setEquipmentId(equipmentId);
        rental.setProductId(productId);
        rental.setRentalDate(rentalDate);
        rental.setReturnDate(rentalDate);
        rental.setType(type);
        rental.setStatus(status);
        rental.setPaymentStatus(paymentStatus);
        rental.setQuantity(quantity);
        rental.setQuantityDay(1);
        rental.setRentalPrice(rentalPrice);
        return rental;
    }
}
