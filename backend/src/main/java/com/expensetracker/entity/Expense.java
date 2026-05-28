package com.expensetracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Expense Entity - Maps to the 'expenses' table in MySQL.
 *
 * Relationships:
 * - Many Expenses belong to One User (ManyToOne)
 */
@Entity
@Table(name = "expenses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount; // BigDecimal is best for money (avoids floating point issues)

    @Column(nullable = false, length = 100)
    private String category; // e.g., Food, Travel, Shopping, Bills, etc.

    @Column(nullable = false)
    private LocalDate date;

    // Many expenses belong to one user
    // @JoinColumn = the foreign key column in 'expenses' table
    // FetchType.LAZY = don't load user data when loading expense (performance)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
