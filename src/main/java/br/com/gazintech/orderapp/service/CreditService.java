package br.com.gazintech.orderapp.service;

import br.com.gazintech.orderapp.entity.Partner;
import br.com.gazintech.orderapp.exception.InsufficientBalanceException;
import br.com.gazintech.orderapp.exception.PartnerNotFoundException;
import br.com.gazintech.orderapp.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.kv;

/**
 * Service for managing partner credit operations.
 * Provides methods to check, debit, credit, and update credit limits for partners.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreditService {

    private final PartnerRepository partnerRepository;

    /**
     * Checks if the partner has available credit for the specified amount.
     *
     * @param partnerId the ID of the partner
     * @param amount    the amount to check
     * @throws PartnerNotFoundException if the partner does not exist
     * @throws InsufficientBalanceException if the partner does not have enough credit
     */
    @Cacheable(value = "partner-credit", key = "#partnerId")
    public void hasAvailableCredit(UUID partnerId, BigDecimal amount) throws PartnerNotFoundException, InsufficientBalanceException {
        log.info("Checking available Partner credit", kv("partnerId", partnerId), kv("amount", amount));

        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new PartnerNotFoundException("Partner not found %s".formatted(partnerId)));

        boolean hasCredit = partner.hasAvailableCredit(amount);
        log.info("Partner {} has available credit: {}", partnerId, hasCredit);
        if (!hasCredit) {
            throw new InsufficientBalanceException("Insufficient credit for partner: %s".formatted(partner.getCode()));
        }
    }

    /**
     * Debits the specified amount from the partner's credit.
     *
     * @param partnerId the ID of the partner
     * @param amount    the amount to debit
     * @throws PartnerNotFoundException if the partner does not exist
     * @throws InsufficientBalanceException if the partner does not have enough credit
     */
    @Transactional
    @CacheEvict(value = "partner-credit", key = "#partnerId")
    public void debitCredit(UUID partnerId, BigDecimal amount) throws PartnerNotFoundException, InsufficientBalanceException {
        log.info("Debiting credit for partner {} amount {}", partnerId, amount);

        Partner partner = partnerRepository.findByIdWithLock(partnerId)
                .orElseThrow(() -> new PartnerNotFoundException("Partner not found: " + partnerId));

        if (!partner.hasAvailableCredit(amount)) {
            throw new InsufficientBalanceException("Insufficient credit for partner: " + partner.getCode());
        }

        partner.debitCredit(amount);
        partnerRepository.save(partner);

        log.info("Credit debited successfully for partner {} new available credit: {}",
                partnerId, partner.getAvailableCredit());
    }

    /**
     * Credits the specified amount to the partner's credit.
     *
     * @param partnerId the ID of the partner
     * @param amount    the amount to credit
     * @throws PartnerNotFoundException if the partner does not exist
     */
    @Transactional
    @CacheEvict(value = "partner-credit", key = "#partnerId")
    public void creditCredit(UUID partnerId, BigDecimal amount) throws PartnerNotFoundException {
        log.info("Crediting credit for partner {} amount {}", partnerId, amount);

        Partner partner = partnerRepository.findByIdWithLock(partnerId)
                .orElseThrow(() -> new PartnerNotFoundException("Partner not found: " + partnerId));

        partner.creditCredit(amount);
        partnerRepository.save(partner);

        log.info("Credit credited successfully for partner {} new available credit: {}",
                partnerId, partner.getAvailableCredit());
    }

    /**
     * Retrieves the available credit for the specified partner.
     *
     * @param partnerId the ID of the partner
     * @return the available credit amount
     * @throws PartnerNotFoundException if the partner does not exist
     */
    @Cacheable(value = "partner-credit", key = "#partnerId + '-info'")
    public BigDecimal getAvailableCredit(UUID partnerId) throws PartnerNotFoundException {
        log.info("Getting available credit for partner {}", partnerId);

        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new PartnerNotFoundException("Partner not found: " + partnerId));

        return partner.getAvailableCredit();
    }

    /**
     * Updates the credit limit for the specified partner.
     *
     * @param partnerId the ID of the partner
     * @param newLimit  the new credit limit
     * @throws PartnerNotFoundException if the partner does not exist
     */
    @Transactional
    @CacheEvict(value = "partner-credit", key = "#partnerId")
    public void updateCreditLimit(UUID partnerId, BigDecimal newLimit) throws PartnerNotFoundException {
        log.info("Updating credit limit for partner {} to {}", partnerId, newLimit);

        Partner partner = partnerRepository.findByIdWithLock(partnerId)
                .orElseThrow(() -> new PartnerNotFoundException("Partner not found: " + partnerId));

        BigDecimal usedCredit = partner.getCreditLimit().subtract(partner.getAvailableCredit());
        partner.setCreditLimit(newLimit);
        partner.setAvailableCredit(newLimit.subtract(usedCredit));

        if (partner.getAvailableCredit().compareTo(BigDecimal.ZERO) < 0) {
            partner.setAvailableCredit(BigDecimal.ZERO);
        }

        partnerRepository.save(partner);

        log.info("Credit limit updated for partner {} new limit: {} new available: {}",
                partnerId, newLimit, partner.getAvailableCredit());
    }
}
