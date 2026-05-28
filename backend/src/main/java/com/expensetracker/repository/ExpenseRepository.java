package com.expensetracker.repository;

import com.expensetracker.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ExpenseRepository - Data access layer for Expense entity.
 *
 * Spring Data JPA automatically generates queries from method names:
 * findByUserId() → SELECT * FROM expenses WHERE user_id = ?
 */
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    /**
     * Get all expenses for a specific user, ordered by date descending (newest first).
     */
    List<Expense> findByUserIdOrderByDateDesc(Long userId);

    /**
     * Find a specific expense by its ID and user ID.
     * This prevents users from accessing other users' expenses (security check).
     */
    Optional<Expense> findByIdAndUserId(Long id, Long userId);

    /**
     * Custom JPQL query to get category-wise expense totals for AI analysis.
     *
     * JPQL (Java Persistence Query Language) uses entity/field names, not table/column names.
     * e = alias for Expense entity
     * e.user.id = access user relationship
     *
     * Returns List of Object[] where:
     * [0] = category (String)
     * [1] = total amount (BigDecimal)
     */
    @Query("SELECT e.category, SUM(e.amount) FROM Expense e WHERE e.user.id = :userId GROUP BY e.category ORDER BY SUM(e.amount) DESC")
    List<Object[]> findCategoryWiseTotalByUserId(@Param("userId") Long userId);
}
