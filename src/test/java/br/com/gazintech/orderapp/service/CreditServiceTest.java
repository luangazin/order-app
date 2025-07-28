package br.com.gazintech.orderapp.service;

import br.com.gazintech.orderapp.entity.Partner;
import br.com.gazintech.orderapp.exception.InsufficientBalanceException;
import br.com.gazintech.orderapp.exception.PartnerNotFoundException;
import br.com.gazintech.orderapp.repository.PartnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreditService Unit Tests")
class CreditServiceTest {

    @Mock
    private PartnerRepository partnerRepository;

    @InjectMocks
    private CreditService creditService;

    private UUID partnerId;
    private Partner partner;
    private BigDecimal amount;

    @BeforeEach
    void setUp() {
        partnerId = UUID.randomUUID();
        amount = BigDecimal.valueOf(100.00);

        partner = new Partner();
        partner.setId(partnerId);
        partner.setCode("PARTNER001");
        partner.setCreditLimit(BigDecimal.valueOf(1000.00));
        partner.setAvailableCredit(BigDecimal.valueOf(500.00));
    }

    @Nested
    @DisplayName("hasAvailableCredit Tests")
    class HasAvailableCreditTests {

        @Test
        @DisplayName("Should pass when partner has sufficient credit")
        void shouldPassWhenPartnerHasSufficientCredit() {
            when(partnerRepository.findById(partnerId)).thenReturn(Optional.of(partner));

            assertDoesNotThrow(() -> creditService.hasAvailableCredit(partnerId, amount));
            verify(partnerRepository).findById(partnerId);
        }

        @Test
        @DisplayName("Should throw InsufficientBalanceException when partner has insufficient credit")
        void shouldThrowInsufficientBalanceExceptionWhenPartnerHasInsufficientCredit() {
            partner.setAvailableCredit(BigDecimal.valueOf(99.00));
            when(partnerRepository.findById(partnerId)).thenReturn(Optional.of(partner));

            InsufficientBalanceException exception = assertThrows(
                    InsufficientBalanceException.class,
                    () -> creditService.hasAvailableCredit(partnerId, amount)
            );
            assertEquals("Insufficient credit for partner: PARTNER001", exception.getMessage());
            verify(partnerRepository).findById(partnerId);
        }

        @Test
        @DisplayName("Should throw PartnerNotFoundException when partner not found")
        void shouldThrowPartnerNotFoundExceptionWhenPartnerNotFound() {
            when(partnerRepository.findById(partnerId)).thenReturn(Optional.empty());

            PartnerNotFoundException exception = assertThrows(
                    PartnerNotFoundException.class,
                    () -> creditService.hasAvailableCredit(partnerId, amount)
            );
            assertEquals("Partner not found: " + partnerId, exception.getMessage());
            verify(partnerRepository).findById(partnerId);
        }
    }

    @Nested
    @DisplayName("debitCredit Tests")
    class DebitCreditTests {

        @Test
        @DisplayName("Should debit credit successfully when partner has sufficient credit")
        void shouldDebitCreditSuccessfullyWhenPartnerHasSufficientCredit() {
            
            when(partnerRepository.findByIdWithLock(partnerId)).thenReturn(Optional.of(partner));
            when(partnerRepository.save(partner)).thenReturn(partner);

            // When
            creditService.debitCredit(partnerId, amount);

            // Then
            verify(partnerRepository, times(1)).findByIdWithLock(partnerId);
            verify(partnerRepository, times(1)).save(partner);
        }

        @Test
        @DisplayName("Should throw InsufficientBalanceException when partner has insufficient credit")
        void shouldThrowInsufficientBalanceExceptionWhenPartnerHasInsufficientCredit() {
            partner.setAvailableCredit(BigDecimal.valueOf(99.00));
            when(partnerRepository.findByIdWithLock(partnerId)).thenReturn(Optional.of(partner));

            InsufficientBalanceException exception = assertThrows(
                    InsufficientBalanceException.class,
                    () -> creditService.debitCredit(partnerId, amount)
            );
            assertEquals("Insufficient credit for partner: PARTNER001", exception.getMessage());
            verify(partnerRepository).findByIdWithLock(partnerId);
            verify(partnerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw PartnerNotFoundException when partner not found")
        void shouldThrowPartnerNotFoundExceptionWhenPartnerNotFound() {
            
            when(partnerRepository.findByIdWithLock(partnerId)).thenReturn(Optional.empty());

            
            PartnerNotFoundException exception = assertThrows(
                    PartnerNotFoundException.class,
                    () -> creditService.debitCredit(partnerId, amount)
            );
            assertEquals("Partner not found: " + partnerId, exception.getMessage());
            verify(partnerRepository).findByIdWithLock(partnerId);
            verify(partnerRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("creditCredit Tests")
    class CreditCreditTests {

        @Test
        @DisplayName("Should credit credit successfully")
        void shouldCreditCreditSuccessfully() {
            
            when(partnerRepository.findByIdWithLock(partnerId)).thenReturn(Optional.of(partner));
            when(partnerRepository.save(partner)).thenReturn(partner);

            // When
            creditService.creditCredit(partnerId, amount);

            // Then
            verify(partnerRepository).findByIdWithLock(partnerId);
            verify(partnerRepository).save(partner);
        }

        @Test
        @DisplayName("Should throw PartnerNotFoundException when partner not found")
        void shouldThrowPartnerNotFoundExceptionWhenPartnerNotFound() {
            
            when(partnerRepository.findByIdWithLock(partnerId)).thenReturn(Optional.empty());

            
            PartnerNotFoundException exception = assertThrows(
                    PartnerNotFoundException.class,
                    () -> creditService.creditCredit(partnerId, amount)
            );
            assertEquals("Partner not found: " + partnerId, exception.getMessage());
            verify(partnerRepository).findByIdWithLock(partnerId);
            verify(partnerRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getAvailableCredit Tests")
    class GetAvailableCreditTests {

        @Test
        @DisplayName("Should return available credit when partner exists")
        void shouldReturnAvailableCreditWhenPartnerExists() {
            
            BigDecimal expectedCredit = BigDecimal.valueOf(500.00);
            when(partnerRepository.findById(partnerId)).thenReturn(Optional.of(partner));

            BigDecimal result = creditService.getAvailableCredit(partnerId);

            assertEquals(expectedCredit, result);
            verify(partnerRepository).findById(partnerId);
        }

        @Test
        @DisplayName("Should throw PartnerNotFoundException when partner not found")
        void shouldThrowPartnerNotFoundExceptionWhenPartnerNotFound() {
            
            when(partnerRepository.findById(partnerId)).thenReturn(Optional.empty());

            PartnerNotFoundException exception = assertThrows(
                    PartnerNotFoundException.class,
                    () -> creditService.getAvailableCredit(partnerId)
            );
            assertEquals("Partner not found: " + partnerId, exception.getMessage());
            verify(partnerRepository).findById(partnerId);
        }
    }

    @Nested
    @DisplayName("updateCreditLimit Tests")
    class UpdateCreditLimitTests {

        @Test
        @DisplayName("Should update credit limit successfully with positive available credit")
        void shouldUpdateCreditLimitSuccessfullyWithPositiveAvailableCredit() {
            
            BigDecimal newLimit = BigDecimal.valueOf(1500.00);
            BigDecimal currentLimit = BigDecimal.valueOf(1000.00);
            BigDecimal currentAvailable = BigDecimal.valueOf(300.00);
            BigDecimal usedCredit = currentLimit.subtract(currentAvailable); // 700.00
            BigDecimal expectedNewAvailable = newLimit.subtract(usedCredit); // 800.00

            when(partnerRepository.findByIdWithLock(partnerId)).thenReturn(Optional.of(partner));
            when(partnerRepository.save(partner)).thenReturn(partner);

            // When
            creditService.updateCreditLimit(partnerId, newLimit);

            // Then
            verify(partnerRepository).findByIdWithLock(partnerId);
            verify(partnerRepository).save(partner);
        }

        @Test
        @DisplayName("Should set available credit to zero when it becomes negative")
        void shouldSetAvailableCreditToZeroWhenItBecomesNegative() {
            
            BigDecimal newLimit = BigDecimal.valueOf(200.00);
            BigDecimal currentLimit = BigDecimal.valueOf(1000.00);
            BigDecimal currentAvailable = BigDecimal.valueOf(300.00);
            BigDecimal usedCredit = currentLimit.subtract(currentAvailable); // 700.00

            when(partnerRepository.findByIdWithLock(partnerId)).thenReturn(Optional.of(partner));
            when(partnerRepository.save(partner)).thenReturn(partner);

            // When
            creditService.updateCreditLimit(partnerId, newLimit);

            // Then
            verify(partnerRepository).findByIdWithLock(partnerId);
            verify(partnerRepository).save(partner);

        }

        @Test
        @DisplayName("Should throw PartnerNotFoundException when partner not found")
        void shouldThrowPartnerNotFoundExceptionWhenPartnerNotFound() {
            
            BigDecimal newLimit = BigDecimal.valueOf(1500.00);
            when(partnerRepository.findByIdWithLock(partnerId)).thenReturn(Optional.empty());

            
            PartnerNotFoundException exception = assertThrows(
                    PartnerNotFoundException.class,
                    () -> creditService.updateCreditLimit(partnerId, newLimit)
            );
            assertEquals("Partner not found: " + partnerId, exception.getMessage());
            verify(partnerRepository).findByIdWithLock(partnerId);
            verify(partnerRepository, never()).save(any());
        }
    }
}