package br.com.gazintech.orderapp.repository.specification;

import br.com.gazintech.orderapp.dto.OrderSearchDTO;
import br.com.gazintech.orderapp.entity.Order;
import jakarta.persistence.criteria.Predicate;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class OrderSpecifications {

    public static Specification<Order> bySearchDto(OrderSearchDTO dto) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (dto.getOrderId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("id"), dto.getOrderId()));
            }
            if (dto.getPartnerId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("partner").get("id"), dto.getPartnerId()));
            }
            if (dto.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), dto.getStartDate()));
            }
            if (dto.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), dto.getEndDate()));
            }
            if (dto.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), dto.getStatus()));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}