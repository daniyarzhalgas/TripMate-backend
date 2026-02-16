package kz.sdu.repository.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import kz.sdu.entity.TripRequest;
import kz.sdu.entity.User;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TripRequestSpecification {

    public static Specification<TripRequest> withFilters(
            String gender,
            Integer minAge,
            Integer maxAge,
            BigDecimal minBudget,
            String city,
            String country,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Фильтр по городу
            if (city != null && !city.isBlank()) {
                predicates.add(cb.equal(
                        cb.lower(root.get("destCity")),
                        city.toLowerCase()
                ));
            }

            // Фильтр по стране
            if (country != null && !country.isBlank()) {
                predicates.add(cb.equal(
                        cb.lower(root.get("destCountry")),
                        country.toLowerCase()
                ));
            }

            // Фильтр по минимальному бюджету
            if (minBudget != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("budgetAmount"),
                        minBudget
                ));
            }

            // Фильтр по дате начала
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("startDate"),
                        startDate
                ));
            }

            // Фильтр по дате окончания
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("endDate"),
                        endDate
                ));
            }

            // Фильтры по пользователю (gender и age) требуют JOIN с таблицей users
            if ((gender != null && !gender.isBlank()) || minAge != null || maxAge != null) {
                Join<TripRequest, User> userJoin = root.join("user", JoinType.INNER);
                
                // Фильтр по полу
                if (gender != null && !gender.isBlank()) {
                    // Gender хранится как ENUM в БД, конвертируем в строку для сравнения
                    predicates.add(cb.equal(
                            cb.lower(cb.function("CAST", String.class, userJoin.get("gender"))),
                            gender.toLowerCase()
                    ));
                }

                // Фильтр по возрасту (вычисляем из date_of_birth)
                if (minAge != null || maxAge != null) {
                    if (minAge != null) {
                        LocalDate maxBirthDate = LocalDate.now().minusYears(minAge);
                        predicates.add(cb.lessThanOrEqualTo(
                                userJoin.get("dateOfBirth"),
                                maxBirthDate
                        ));
                    }
                    if (maxAge != null) {
                        LocalDate minBirthDate = LocalDate.now().minusYears(maxAge + 1);
                        predicates.add(cb.greaterThan(
                                userJoin.get("dateOfBirth"),
                                minBirthDate
                        ));
                    }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
