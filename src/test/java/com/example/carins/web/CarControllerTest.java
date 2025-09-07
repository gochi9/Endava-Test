package com.example.carins.web;

import com.example.carins.service.CarService;
import com.example.carins.web.exception.CarNotFoundException;
import com.example.carins.web.exception.InvalidDateException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CarController.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarService carService;

    @Autowired
    private ObjectMapper objectMapper;

    // missing car http error check
    @Test
    void insuranceValidWithMissingCarReturns404() throws Exception {
        Long carId = 999L;
        String date = "2025-01-01";

        when(carService.isInsuranceValid(carId, date)).thenThrow(new CarNotFoundException("Car with ID " + carId + " not found"));

        mockMvc.perform(get("/api/cars/{carId}/insurance-valid", carId).param("date", date))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Car Not Found"));
    }

    // invalid date http error check
    @Test
    void insuranceValidWithBadDateReturns400() throws Exception {
        Long carId = 1L;
        String invalidDate = "invalid-date";

        when(carService.isInsuranceValid(carId, invalidDate)).thenThrow(new InvalidDateException("Invalid date format. Expected YYYY-MM-DD, got: " + invalidDate));

        mockMvc.perform(get("/api/cars/{carId}/insurance-valid", carId).param("date", invalidDate))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Date"));
    }

    // successful insurance validation check
    @Test
    void insuranceValidWithValidDataReturns200() throws Exception {
        Long carId = 1L;
        String date = "2025-01-01";

        when(carService.isInsuranceValid(carId, date)).thenReturn(true);

        mockMvc.perform(get("/api/cars/{carId}/insurance-valid", carId).param("date", date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.carId").value(carId))
                .andExpect(jsonPath("$.date").value(date))
                .andExpect(jsonPath("$.valid").value(true));
    }

    // claim request validation check
    @Test
    void registerClaimWithBadDataReturns400() throws Exception {
        Long carId = 1L;
        String invalidRequest = "{}";

        mockMvc.perform(post("/api/cars/{carId}/claims", carId).contentType(MediaType.APPLICATION_JSON).content(invalidRequest)).andExpect(status().isBadRequest());
    }

    // car history missing car check
    @Test
    void getHistoryWithMissingCarReturns404() throws Exception {
        Long carId = 999L;

        when(carService.getCarHistory(carId)).thenThrow(new CarNotFoundException("Car with ID " + carId + " not found"));

        mockMvc.perform(get("/api/cars/{carId}/history", carId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Car Not Found"));
    }

    // successful car history retrieval check
    @Test
    void getHistoryWithValidCarReturns200() throws Exception {
        Long carId = 1L;

        when(carService.getCarHistory(carId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/cars/{carId}/history", carId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}