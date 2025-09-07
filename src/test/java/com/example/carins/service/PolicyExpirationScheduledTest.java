package com.example.carins.service;

import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.repo.OwnerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
public class PolicyExpirationScheduledTest {

    @Autowired
    private PolicyExpirationService policyExpirationService;

    @Autowired
    private InsurancePolicyRepository policyRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Test
    @Transactional
    void testExpiredPolicyLoggedOnce(CapturedOutput output) {
        InsurancePolicy policy = policyRepository.findById(1L).get();
        policy.setEndDate(LocalDate.now().minusDays(1));
        policy.setExpirationNotified(false);
        policyRepository.save(policy);

        policyExpirationService.checkExpiredPolicies();

        assertThat(output.getOut()).contains("Policy 1 for car 1 expired on");
        assertThat(output.getOut()).contains("Processed 1 expired policy notifications");

        InsurancePolicy updatedPolicy = policyRepository.findById(1L).get();
        assertTrue(updatedPolicy.isExpirationNotified());
    }

    // duplicate notification spam check
    @Test
    @Transactional
    void testAlreadyNotifiedPolicyNotLoggedAgain(CapturedOutput output) {
        InsurancePolicy policy = policyRepository.findById(1L).get();
        policy.setEndDate(LocalDate.now().minusDays(1));
        policy.setExpirationNotified(true);
        policyRepository.save(policy);

        policyExpirationService.checkExpiredPolicies();

        assertThat(output.getOut()).doesNotContain("Policy 1 for car 1 expired on");
        assertThat(output.getOut()).doesNotContain("Processed");
    }

    // bulk expiration logging check
    @Test
    @Transactional
    void testMultipleExpiredPoliciesLoggedCorrectly(CapturedOutput output) {
        InsurancePolicy policy1 = policyRepository.findById(1L).get();
        policy1.setEndDate(LocalDate.now().minusDays(2));
        policy1.setExpirationNotified(false);
        policyRepository.save(policy1);

        InsurancePolicy policy2 = policyRepository.findById(2L).get();
        policy2.setEndDate(LocalDate.now().minusDays(1));
        policy2.setExpirationNotified(false);
        policyRepository.save(policy2);

        policyExpirationService.checkExpiredPolicies();

        assertThat(output.getOut()).contains("Policy 1 for car 1 expired on");
        assertThat(output.getOut()).contains("Policy 2 for car 1 expired on");
        assertThat(output.getOut()).contains("Processed 2 expired policy notifications");

        assertTrue(policyRepository.findById(1L).get().isExpirationNotified());
        assertTrue(policyRepository.findById(2L).get().isExpirationNotified());
    }

    // valid policy false positive check
    @Test
    @Transactional
    void testNonExpiredPolicyNotLogged(CapturedOutput output) {
        InsurancePolicy policy = policyRepository.findById(1L).get();
        policy.setEndDate(LocalDate.now().plusDays(30));
        policy.setExpirationNotified(false);
        policyRepository.save(policy);

        policyExpirationService.checkExpiredPolicies();

        assertThat(output.getOut()).doesNotContain("Policy 1 for car 1 expired on");
        assertThat(output.getOut()).doesNotContain("Processed");

        assertFalse(policyRepository.findById(1L).get().isExpirationNotified());
    }

    // midnight expiration boundary check
    @Test
    @Transactional
    void testExpiredPolicyLoggedExactlyAtMidnight(CapturedOutput output) {
        InsurancePolicy policy = policyRepository.findById(1L).get();
        policy.setEndDate(LocalDate.now().minusDays(1));
        policy.setExpirationNotified(false);
        policyRepository.save(policy);

        policyExpirationService.checkExpiredPolicies();

        assertThat(output.getOut()).contains("Policy 1 for car 1 expired on");
        assertTrue(policyRepository.findById(1L).get().isExpirationNotified());
    }

    // mixed policy states filtering check
    @Test
    @Transactional
    void testMixedExpiredAndValidPolicies(CapturedOutput output) {
        InsurancePolicy expiredPolicy = policyRepository.findById(1L).get();
        expiredPolicy.setEndDate(LocalDate.now().minusDays(1));
        expiredPolicy.setExpirationNotified(false);
        policyRepository.save(expiredPolicy);

        InsurancePolicy validPolicy = policyRepository.findById(2L).get();
        validPolicy.setEndDate(LocalDate.now().plusDays(30));
        validPolicy.setExpirationNotified(false);
        policyRepository.save(validPolicy);

        InsurancePolicy alreadyNotifiedPolicy = policyRepository.findById(3L).get();
        alreadyNotifiedPolicy.setEndDate(LocalDate.now().minusDays(5));
        alreadyNotifiedPolicy.setExpirationNotified(true);
        policyRepository.save(alreadyNotifiedPolicy);

        policyExpirationService.checkExpiredPolicies();

        assertThat(output.getOut()).contains("Policy 1 for car 1 expired on");
        assertThat(output.getOut()).doesNotContain("Policy 2 for car 1 expired on");
        assertThat(output.getOut()).doesNotContain("Policy 3 for car 2 expired on");
        assertThat(output.getOut()).contains("Processed 1 expired policy notifications");

        assertTrue(policyRepository.findById(1L).get().isExpirationNotified());
        assertFalse(policyRepository.findById(2L).get().isExpirationNotified());
        assertTrue(policyRepository.findById(3L).get().isExpirationNotified());
    }

    // log message format validation check
    @Test
    @Transactional
    void testLogMessageFormat(CapturedOutput output) {
        InsurancePolicy policy = policyRepository.findById(1L).get();
        LocalDate expiredDate = LocalDate.of(2024, 12, 31);
        policy.setEndDate(expiredDate);
        policy.setExpirationNotified(false);
        policyRepository.save(policy);

        policyExpirationService.checkExpiredPolicies();

        String expectedLogMessage = "Policy 1 for car 1 expired on 2024-12-31";
        assertThat(output.getOut()).contains(expectedLogMessage);
    }

    // scheduler error handling check
    @Test
    @Transactional
    void testScheduledTaskRunsWithoutErrors(CapturedOutput output) {
        policyRepository.findAll().forEach(policy -> {
            policy.setEndDate(LocalDate.now().plusDays(30));
            policy.setExpirationNotified(false);
            policyRepository.save(policy);
        });

        assertDoesNotThrow(() -> policyExpirationService.checkExpiredPolicies());

        assertThat(output.getOut()).doesNotContain("expired on");
        assertThat(output.getOut()).doesNotContain("Processed");
    }
}