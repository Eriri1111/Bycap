package com.bycap.gateway.model;

import java.util.UUID;

/**
 * Entidad principal: Empleado del Comercio.
 *
 * Principio: Information Expert (GRASP) — la entidad valida sus propias
 * invariantes de negocio (formato de email, rol válido) en el constructor,
 * garantizando que nunca exista un Employee en estado inválido.
 */
public class Employee {

    private final UUID id;
    private final String fullName;
    private final String email;
    private final UUID merchantId;
    private final Role role;
    private boolean active;

    /** Roles funcionales del sistema Bycap (RF-04, RF-05). */
    public enum Role {
        SUPER_ADMIN,
        MERCHANT_ADMIN,
        CASHIER
    }

    public Employee(UUID id, String fullName, String email, UUID merchantId, Role role) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("El nombre completo del empleado no puede estar vacío.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("El correo electrónico no tiene un formato válido.");
        }
        if (merchantId == null) {
            throw new IllegalArgumentException("El ID del comercio no puede ser nulo.");
        }
        if (role == null) {
            throw new IllegalArgumentException("El rol del empleado no puede ser nulo.");
        }

        this.id         = id != null ? id : UUID.randomUUID();
        this.fullName   = fullName.trim();
        this.email      = email.toLowerCase().trim();
        this.merchantId = merchantId;
        this.role       = role;
        this.active     = true;
    }

    public void deactivate() { this.active = false; }
    public void activate()   { this.active = true; }

    public UUID getId()         { return id; }
    public String getFullName() { return fullName; }
    public String getEmail()    { return email; }
    public UUID getMerchantId() { return merchantId; }
    public Role getRole()       { return role; }
    public boolean isActive()   { return active; }

    @Override
    public String toString() {
        return String.format("Employee{id=%s, fullName='%s', email='%s', role=%s, active=%b}",
                id, fullName, email, role, active);
    }
}
