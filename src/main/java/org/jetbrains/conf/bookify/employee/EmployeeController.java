package org.jetbrains.conf.bookify.employee;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
class EmployeeController {

    private final EmployeeService employeeService;
    private final EmployeeMapper employeeMapper;

    EmployeeController(EmployeeService employeeService, EmployeeMapper employeeMapper) {
        this.employeeService = employeeService;
        this.employeeMapper = employeeMapper;
    }

    @GetMapping("")
    public ResponseEntity<List<EmployeeSearchResponse>> findAll(@RequestParam(value = "sortBy", defaultValue = "name") String sortBy) {
        List<EmployeeSearchResponse> employees = employeeService.findAll(sortBy).stream().map(employeeMapper::toEmployeeSearchResponce).toList();
        return employees.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(employees);
    }
}
