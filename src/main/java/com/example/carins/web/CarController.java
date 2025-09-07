package com.example.carins.web;

import com.example.carins.model.Car;
import com.example.carins.model.InsuranceClaim;
import com.example.carins.model.Owner;
import com.example.carins.service.CarService;
import com.example.carins.web.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CarController {

    private final CarService service;

    public CarController(CarService service) {
        this.service = service;
    }

    @GetMapping("/cars")
    public List<CarDto> getCars() {
        return service.listCars().stream().map(this::toDto).toList();
    }

    @GetMapping("/cars/{carId}/insurance-valid")
    public ResponseEntity<InsuranceValidityResponse> isInsuranceValid(@PathVariable("carId") Long carId, @RequestParam("date") String date) {
        return ResponseEntity.ok(new InsuranceValidityResponse(carId, date, service.isInsuranceValid(carId, date)));
    }

    @PostMapping("/cars/{carId}/claims")
    public ResponseEntity<ClaimDto> registerClaim(@PathVariable("carId") Long carId, @Valid @RequestBody CreateClaimRequest request) {
        InsuranceClaim claim = service.registerClaim(carId, request);
        ClaimDto claimDto = toClaimDto(claim);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(claim.getId())
                .toUri();

        return ResponseEntity.created(location).body(claimDto);
    }

    @GetMapping("/cars/{carId}/history")
    public ResponseEntity<List<CarHistoryEventDto>> getCarHistory(@PathVariable("carId") Long carId) {
        return ResponseEntity.ok(service.getCarHistory(carId));
    }

    private CarDto toDto(Car c) {
        Owner o = c.getOwner();
        return new CarDto(c.getId(), c.getVin(), c.getMake(), c.getModel(), c.getYearOfManufacture(), o != null ? o.getId() : null, o != null ? o.getName() : null, o != null ? o.getEmail() : null);
    }

    private ClaimDto toClaimDto(InsuranceClaim claim) {
        return new ClaimDto(claim.getId(), claim.getCar().getId(), claim.getClaimDate(), claim.getDescription(), claim.getAmount(), claim.getCreatedAt());
    }

    public record InsuranceValidityResponse(Long carId, String date, boolean valid) {}
}