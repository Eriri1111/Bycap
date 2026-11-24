package com.bycap.gateway.exception;

/**
 * Exception thrown when attempting to register an employee with an email that already exists.
 */
public class DuplicateEmployeeException extends RuntimeException {

    public DuplicateEmployeeException(String email) {
        super("An employee is already registered with email address: " + email);
    }
}
