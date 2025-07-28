package br.com.gazintech.orderapp.repository.specification;

import br.com.gazintech.orderapp.dto.OrderSearchDTO;
import br.com.gazintech.orderapp.entity.Order;
import jakarta.persistence.criteria.Predicate;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Specification class for filtering orders based on various criteria.
 * This class provides a method to create a Specification for querying orders
 * based on the fields present in the OrderSearchDTO.
 */
@NoArgsConstructor
public class OrderSpecifications {

    /**
     * Creates a Specification for filtering orders based on the provided OrderSearchDTO.
     *
     * @param dto The OrderSearchDTO containing the search criteria.
     * @return A Specification that can be used to filter orders.
     */
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