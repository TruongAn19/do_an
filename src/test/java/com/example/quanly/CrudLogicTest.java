package com.example.quanly;

import com.example.quanly.domain.*;
import com.example.quanly.domain.dto.ProductResponseDTO;
import com.example.quanly.mapper.ProductMapper;
import com.example.quanly.mapper.UserMapper;
import com.example.quanly.repository.*;
import com.example.quanly.service.ProductService;
import com.example.quanly.service.RacketService;
import com.example.quanly.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrudLogicTest {

    @Mock ProductRepository productRepository;
    @Mock TimeRepository timeRepository;
    @Mock SubCourtRepository subCourtRepository;
    @Mock SubCourtAvailableTimeRepository subCourtAvailableTimeRepository;
    @Mock ProductMapper productMapper;
    @Mock RacketRepository racketRepository;
    @Mock RacketStockByDateRepository stockRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock UserMapper userMapper;

    @Test
    void productUpdatePersistsAllEditableFieldsAndAddsMissingSubCourts() {
        ProductService service = new ProductService(
                productRepository, timeRepository, subCourtRepository,
                subCourtAvailableTimeRepository, productMapper, racketRepository);
        Product existing = new Product();
        existing.setId(1L);
        existing.setQuantity(1);
        SubCourt currentCourt = new SubCourt();
        AvailableTime time = new AvailableTime();
        time.setId(10L);

        Product updates = new Product();
        updates.setName("Court");
        updates.setPrice(200_000);
        updates.setQuantity(3);
        updates.setSale(10);
        updates.setAddress("Address");
        updates.setAddressDetail("Floor 2");
        updates.setShortDesc("Short");
        updates.setDetailDesc("Detail");
        updates.setDepositPrice(50_000);

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(subCourtRepository.findByProduct(existing)).thenReturn(List.of(currentCourt));
        when(productRepository.save(existing)).thenReturn(existing);
        when(timeRepository.findAll()).thenReturn(List.of(time));
        when(subCourtRepository.save(any(SubCourt.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productMapper.toDTO(existing)).thenReturn(new ProductResponseDTO());

        service.updateProduct(1L, updates);

        assertEquals("Floor 2", existing.getAddressDetail());
        assertEquals("Short", existing.getShortDesc());
        assertEquals(50_000, existing.getDepositPrice());
        assertEquals(3, existing.getQuantity());
        verify(subCourtRepository, times(2)).save(any(SubCourt.class));
        verify(subCourtAvailableTimeRepository, times(2)).save(any(SubCourtAvailableTime.class));
    }

    @Test
    void racketUpdateSynchronizesFutureStockAndRejectsOverReduction() {
        RacketService service = new RacketService(racketRepository, stockRepository);
        Product product = new Product();
        product.setId(1L);
        Racket existing = validRacket(product, 10);
        existing.setId(2L);
        Racket updates = validRacket(product, 7);

        RacketStockByDate stock = new RacketStockByDate();
        stock.setDate(LocalDate.now());
        stock.setTotalStock(10);
        stock.setAvailableStock(6);
        stock.setReservedStock(3);
        stock.setRentalStock(1);

        when(racketRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(existing));
        when(stockRepository.findFutureByRacketIdForUpdate(2L, LocalDate.now())).thenReturn(List.of(stock));
        when(racketRepository.save(existing)).thenReturn(existing);

        service.updateRacket(2L, updates);

        assertEquals(7, stock.getTotalStock());
        assertEquals(3, stock.getAvailableStock());

        updates.setQuantity(3);
        assertThrows(com.example.quanly.exception.BusinessConflictException.class,
                () -> service.updateRacket(2L, updates));
    }

    @Test
    void deletingUserOnlyDeactivatesAccount() {
        UserService service = new UserService(
                passwordEncoder, userRepository, roleRepository, mock(ProductRepository.class), userMapper);
        User user = new User();
        user.setId(8L);
        user.setActive(true);
        when(userRepository.findById(8L)).thenReturn(Optional.of(user));

        service.deleteAUser(8L);

        assertFalse(user.isActive());
        verify(userRepository).save(user);
        verify(userRepository, never()).deleteById(anyLong());
    }

    private Racket validRacket(Product product, int quantity) {
        Racket racket = new Racket();
        racket.setName("Racket");
        racket.setProduct(product);
        racket.setQuantity(quantity);
        racket.setBookingStockQuantity(2);
        racket.setPrice(100_000);
        racket.setRentalPricePerDay(20_000);
        racket.setRentalPricePerPlay(10_000);
        return racket;
    }
}
