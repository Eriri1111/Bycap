/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Util;

/**
 * Excepción lanzada cuando se intenta registrar un empleado con un correo ya existente.
 */
public class DuplicateEmployeeException extends RuntimeException {
    
    public DuplicateEmployeeException(String email) {
        super("Ya existe un empleado registrado con el correo electrónico: " + email);
    }
}