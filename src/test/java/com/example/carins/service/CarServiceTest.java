package com.example.carins.service;

import com.example.carins.model.Car;
import com.example.carins.model.InsuranceClaim;
import com.example.carins.model.Owner;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.InsuranceClaimRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.web.dto.CreateClaimRequest;
import com.example.carins.web.exception.CarNotFoundException;
import com.example.carins.web.exception.InvalidDateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private InsurancePolicyRepository policyRepository;

    @Mock
    private InsuranceClaimRepository claimRepository;

    @InjectMocks
    private CarService carService;

    private Car testCar;
    private Owner testOwner;

    @BeforeEach
    void setUp() {
        testOwner = new Owner("Test Owner", "test@example.com");
        testCar = new Car("VIN12345", "Toyota", "Camry", 2020, testOwner);
    }

    // missing car validation check
    @Test
    void insuranceValidWithMissingCar() {
        Long carId = 999L;
        when(carRepository.existsById(carId)).thenReturn(false);

        assertThrows(CarNotFoundException.class, () -> carService.isInsuranceValid(carId, "2025-01-01"));
    }

    // date format validation check
    @Test
    void insuranceValidWithBadFormat() {
        Long carId = 1L;
        when(carRepository.existsById(carId)).thenReturn(true);

        assertThrows(InvalidDateException.class, () -> carService.isInsuranceValid(carId, "invalid-date"));
    }

    // historical date boundary check
    @Test
    void insuranceValidWithOldDate() {
        Long carId = 1L;
        when(carRepository.existsById(carId)).thenReturn(true);

        assertThrows(InvalidDateException.class, () -> carService.isInsuranceValid(carId, "1800-01-01"));
    }

    // future date boundary check
    @Test
    void insuranceValidWithFutureDate() {
        Long carId = 1L;
        when(carRepository.existsById(carId)).thenReturn(true);

        assertThrows(InvalidDateException.class, () -> carService.isInsuranceValid(carId, "2040-01-01"));
    }

    // active policy coverage check
    @Test
    void insuranceValidWithActivePolicy() {
        Long carId = 1L;
        String dateStr = "2025-06-01";
        LocalDate date = LocalDate.parse(dateStr);

        when(carRepository.existsById(carId)).thenReturn(true);
        when(policyRepository.existsActiveOnDate(carId, date)).thenReturn(true);

        assertTrue(carService.isInsuranceValid(carId, dateStr));
    }

    // inactive policy coverage check
    @Test
    void insuranceValidWithInactivePolicy() {
        Long carId = 1L;
        String dateStr = "2025-06-01";
        LocalDate date = LocalDate.parse(dateStr);

        when(carRepository.existsById(carId)).thenReturn(true);
        when(policyRepository.existsActiveOnDate(carId, date)).thenReturn(false);

        assertFalse(carService.isInsuranceValid(carId, dateStr));
    }

    // claim registration car validation check
    @Test
    void registerClaimWithMissingCar() {
        Long carId = 999L;
        CreateClaimRequest request = new CreateClaimRequest(LocalDate.now(), "Test claim", new BigDecimal("500.00"));

        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        assertThrows(CarNotFoundException.class, () -> carService.registerClaim(carId, request));
    }

    // successful claim creation check
    @Test
    void registerClaimWithValidData() {
        Long carId = 1L;
        CreateClaimRequest request = new CreateClaimRequest(
                LocalDate.now(), "Test claim", new BigDecimal("500.00"));

        when(carRepository.findById(carId)).thenReturn(Optional.of(testCar));
        when(claimRepository.save(any(InsuranceClaim.class))).thenAnswer(invocation -> {
            InsuranceClaim claim = invocation.getArgument(0);
            return claim;
        });

        InsuranceClaim result = carService.registerClaim(carId, request);

        assertNotNull(result);
        assertEquals(testCar, result.getCar());
        assertEquals(request.claimDate(), result.getClaimDate());
        assertEquals(request.description(), result.getDescription());
        assertEquals(request.amount(), result.getAmount());
    }

    // car history retrieval validation check
    @Test
    void getHistoryWithMissingCar() {
        Long carId = 999L;
        when(carRepository.existsById(carId)).thenReturn(false);

        assertThrows(CarNotFoundException.class, () -> carService.getCarHistory(carId));
    }
}