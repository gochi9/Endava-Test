package com.example.carins.service;

import com.example.carins.model.Car;
import com.example.carins.model.InsuranceClaim;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.InsuranceClaimRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.web.dto.CarHistoryEventDto;
import com.example.carins.web.dto.CreateClaimRequest;
import com.example.carins.web.exception.CarNotFoundException;
import com.example.carins.web.exception.InvalidDateException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final InsurancePolicyRepository policyRepository;
    private final InsuranceClaimRepository claimRepository;

    public CarService(CarRepository carRepository, InsurancePolicyRepository policyRepository, InsuranceClaimRepository claimRepository) {
        this.carRepository = carRepository;
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
    }

    public List<Car> listCars() {
        return carRepository.findAll();
    }

    public boolean isInsuranceValid(Long carId, String dateStr) {
        if (!carRepository.existsById(carId))
            throw new CarNotFoundException("Car with ID " + carId + " not found");

        LocalDate date;
        try { date = LocalDate.parse(dateStr); }
        catch (DateTimeParseException e) { throw new InvalidDateException("Invalid date format. Expected YYYY-MM-DD, got: " + dateStr); }

        LocalDate minDate = LocalDate.of(1900, 1, 1);
        LocalDate maxDate = LocalDate.now().plusYears(10);
        if (date.isBefore(minDate) || date.isAfter(maxDate))
            throw new InvalidDateException("Date must be between " + minDate + " and " + maxDate);

        return policyRepository.existsActiveOnDate(carId, date);
    }

    @Transactional
    public InsuranceClaim registerClaim(Long carId, CreateClaimRequest request) {
        Car car = carRepository.findById(carId).orElseThrow(() -> new CarNotFoundException("Car with ID " + carId + " not found"));
        return claimRepository.save(new InsuranceClaim(car, request.claimDate(), request.description(), request.amount()));
    }

    public List<CarHistoryEventDto> getCarHistory(Long carId) {
        if (!carRepository.existsById(carId))
            throw new CarNotFoundException("Car with ID " + carId + " not found");

        List<CarHistoryEventDto> events = new ArrayList<>();

        List<InsurancePolicy> policies = policyRepository.findByCarIdOrderByStartDate(carId);
        for (InsurancePolicy policy : policies) {
            events.add(new CarHistoryEventDto("POLICY_START", policy.getStartDate(), "Insurance policy started with " + policy.getProvider(), policy.getId()));
            events.add(new CarHistoryEventDto("POLICY_END", policy.getEndDate(), "Insurnce policy ended with " + policy.getProvider(), policy.getId()));
        }

        List<InsuranceClaim> claims = claimRepository.findByCarIdOrderByClaimDate(carId);
        for (InsuranceClaim claim : claims)
            events.add(new CarHistoryEventDto("CLAIM", claim.getClaimDate(), "Insurance claim: " + claim.getDescription() + " (Amount: $" + claim.getAmount() + ")", claim.getId()));

        events.sort(Comparator.comparing(CarHistoryEventDto::date));
        return events;
    }
}