package com.example.carins;

import com.example.carins.service.CarService;
import com.example.carins.web.exception.CarNotFoundException;
import com.example.carins.web.exception.InvalidDateException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CarInsuranceApplicationTests {

    @Autowired
    CarService service;

    @Test
    void insuranceValidityBasic() {
        assertTrue(service.isInsuranceValid(1L, "2024-06-01"));
        assertTrue(service.isInsuranceValid(1L, "2025-06-01"));
        assertFalse(service.isInsuranceValid(2L, "2025-02-01"));
    }

    @Test
    void insuranceValidityWithNonExistentCar() {
        assertThrows(CarNotFoundException.class, () -> service.isInsuranceValid(999L, "2024-06-01"));
    }

    @Test
    void insuranceValidityWithInvalidDateFormat() {
        assertThrows(InvalidDateException.class, () -> service.isInsuranceValid(1L, "invalid-date"));

        assertThrows(InvalidDateException.class, () -> service.isInsuranceValid(1L, "2024/06/01"));
    }

    @Test
    void insuranceValidityWithImpossibleDates() {
        assertThrows(InvalidDateException.class, () -> service.isInsuranceValid(1L, "1800-01-01"));

        assertThrows(InvalidDateException.class, () -> service.isInsuranceValid(1L, "2050-01-01"));
    }

    @Test
    void validDatesAccepted() {
        assertDoesNotThrow(() -> service.isInsuranceValid(1L, "2024-01-01"));
        assertDoesNotThrow(() -> service.isInsuranceValid(1L, LocalDate.now().toString()));
    }
}