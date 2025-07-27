package br.com.gazintech.orderapp.entity;

import br.com.gazintech.orderapp.exception.InsufficientBalanceException;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Created by IntelliJ IDEA.<br/>
 * User: luan-gazin<br/>
 * Date: 25/07/2025<br/>
 * Time: 18:41<br/>
 * To change this template use File | Settings | File Templates.
 */

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "\"Partner\"", indexes = {
        @Index(name = "idx_partner_code", columnList = "code", unique = true)
})
public class Partner {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "credit_limit", nullable = false, precision = 19, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "available_credit", nullable = false, precision = 19, scale = 2)
    private BigDecimal availableCredit;

    @Column(nullable = false)
    private Boolean active;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public boolean hasAvailableCredit(BigDecimal amount) {
        return availableCredit.compareTo(amount) >= 0;
    }

    public void debitCredit(BigDecimal amount) {
        if (!hasAvailableCredit(amount)) {
            throw new InsufficientBalanceException("Insufficient balance");
        }
        this.availableCredit = this.availableCredit.subtract(amount);
    }

    public void creditCredit(BigDecimal amount) {
        this.availableCredit = this.availableCredit.add(amount);
        if (this.availableCredit.compareTo(this.creditLimit) > 0) {
            this.availableCredit = this.creditLimit;
        }
    }
}
