package com.bycap.gateway.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Core Domain Entity: Merchant Employee.
 *
 * GRASP Information Expert: Validates its own invariants (valid email, non-blank name, valid role)
 * upon instantiation while mapping cleanly to a relational database table via JPA.
 */
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private Role role;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum Role {
        SUPER_ADMIN,
        MERCHANT_ADMIN,
        CASHIER
    }

    protected Employee() {
        // Required by JPA
    }

    public Employee(UUID id, String fullName, String email, UUID merchantId, Role role) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Employee full name cannot be empty.");
        }
        if (email == null || !email.contains("@") || email.isBlank()) {
            throw new IllegalArgumentException("Email address does not have a valid format.");
        }
        if (merchantId == null) {
            throw new IllegalArgumentException("Merchant ID cannot be null.");
        }
        if (role == null) {
            throw new IllegalArgumentException("Employee role cannot be null.");
        }

        this.id         = id != null ? id : UUID.randomUUID();
        this.fullName   = fullName.trim();
        this.email      = email.toLowerCase().trim();
        this.merchantId = merchantId;
        this.role       = role;
        this.active     = true;
        this.createdAt  = LocalDateTime.now();
    }

    public void deactivate() { this.active = false; }
    public void activate()   { this.active = true; }
    public void toggleStatus() { this.active = !this.active; }

    public UUID getId()                 { return id; }
    public String getFullName()         { return fullName; }
    public String getEmail()            { return email; }
    public UUID getMerchantId()         { return merchantId; }
    public Role getRole()               { return role; }
    public boolean isActive()           { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return String.format("Employee{id=%s, fullName='%s', email='%s', role=%s, active=%b}",
                id, fullName, email, role, active);
    }
}
