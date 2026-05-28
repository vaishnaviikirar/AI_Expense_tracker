package com.expensetracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * User Entity - Maps to the 'users' table in MySQL.
 *
 * Relationships:
 * - One User can have Many Expenses (OneToMany)
 */
@Entity
@Table(name = "users")
@Data                   // Lombok: generates getters, setters, toString, equals, hashCode
@NoArgsConstructor      // Lombok: generates no-arg constructor
@AllArgsConstructor     // Lombok: generates all-arg constructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment primary key
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private String password; // BCrypt encrypted password

    // One user can have many expenses
    // mappedBy = "user" means the 'user' field in Expense entity owns the relationship
    // cascade = ALL means if user is deleted, all their expenses are deleted too
    // fetch = LAZY means expenses are NOT loaded until explicitly accessed (performance)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Expense> expenses;
}
