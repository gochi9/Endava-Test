package com.example.carins.service;

import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.InsurancePolicyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PolicyExpirationService {

    private static final Logger logger = LoggerFactory.getLogger(PolicyExpirationService.class);
    private final InsurancePolicyRepository policyRepository;

    public PolicyExpirationService(InsurancePolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void checkExpiredPolicies() {
        LocalDate today = LocalDate.now();
        List<InsurancePolicy> expiredPolicies = policyRepository.findExpiredPoliciesNotNotified(today);

        for (InsurancePolicy policy : expiredPolicies) {
            logger.warn("Policy {} for car {} expired on {}", policy.getId(), policy.getCar().getId(), policy.getEndDate());
            policy.setExpirationNotified(true);
            policyRepository.save(policy);
        }

        if (!expiredPolicies.isEmpty())
            logger.info("Processed {} expired policy notifications", expiredPolicies.size());
    }
}