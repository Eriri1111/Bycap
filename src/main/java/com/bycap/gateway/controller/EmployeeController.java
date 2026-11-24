package com.bycap.gateway.controller;

import com.bycap.gateway.dto.RegisterEmployeeDTO;
import com.bycap.gateway.model.Employee;
import com.bycap.gateway.service.RegisterEmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")
public class EmployeeController {

    private final RegisterEmployeeService registerEmployeeService;

    public EmployeeController(RegisterEmployeeService registerEmployeeService) {
        this.registerEmployeeService = registerEmployeeService;
    }

    @GetMapping
    public ResponseEntity<List<Employee>> listEmployees(
            @RequestParam(value = "merchantId", required = false) UUID merchantId) {
        return ResponseEntity.ok(registerEmployeeService.getEmployeesByMerchant(merchantId));
    }

    @PostMapping
    public ResponseEntity<Employee> registerEmployee(@RequestBody RegisterEmployeeDTO dto) {
        Employee created = registerEmployeeService.execute(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<Employee> toggleEmployeeStatus(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(registerEmployeeService.toggleEmployeeStatus(id));
    }
}
