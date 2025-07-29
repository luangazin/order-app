package br.com.gazintech.orderapp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Profile({"local", "docker"})
@Configuration
public class InitLocalDatabase {

    @PersistenceContext
    private EntityManager entityManager;

    @EventListener(ContextRefreshedEvent.class)
    @Transactional
    public void init() {
        UUID partnerId = UUID.fromString("1ffe19fd-cb50-4afe-b4cf-07aa691631df");

        // Check if partner exists
        Long count = (Long) entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM public.\"Partner\" WHERE id = ?1")
                .setParameter(1, partnerId)
                .getSingleResult();

        if (count == 0) {
            entityManager.createNativeQuery("""
                            INSERT INTO public."Partner"
                            (id, active, available_credit, code, created_at, credit_limit, email, "name", updated_at, "version")
                            VALUES(?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10)
                            """)
                    .setParameter(1, partnerId)
                    .setParameter(2, true)
                    .setParameter(3, new BigDecimal("10000"))
                    .setParameter(4, "Partener 001")
                    .setParameter(5, LocalDateTime.now())
                    .setParameter(6, new BigDecimal("1000"))
                    .setParameter(7, "email")
                    .setParameter(8, "Partener 001")
                    .setParameter(9, LocalDateTime.now())
                    .setParameter(10, 0)
                    .executeUpdate();
        }
    }
}
