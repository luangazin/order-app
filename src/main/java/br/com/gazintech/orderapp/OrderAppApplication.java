package br.com.gazintech.orderapp;

import br.com.gazintech.orderapp.entity.Partner;
import br.com.gazintech.orderapp.repository.PartnerRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.math.BigDecimal;
import java.util.concurrent.Executor;

@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableTransactionManagement
public class OrderAppApplication {

    @Autowired
    private PartnerRepository partnerRepository;

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("order-app-");
        executor.initialize();
        return executor;
    }

    @PostConstruct
    public void init() {
        String code = "1ffe19fd-cb50-4afe-b4cf-07aa691631df";
        if (!partnerRepository.existsByCode(code)) {
            partnerRepository.save(Partner.builder()
                    .active(true)
                    .code(code)
                    .name("Partener 001")
                    .email("email")
                    .availableCredit(BigDecimal.valueOf(10000))
                    .creditLimit(BigDecimal.valueOf(1000))
                    .build());
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(OrderAppApplication.class, args);
    }
}
