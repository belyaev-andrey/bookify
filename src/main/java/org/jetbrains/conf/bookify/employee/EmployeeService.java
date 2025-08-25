package org.jetbrains.conf.bookify.employee;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
class EmployeeService {

    private final EmployeeRepository employeeRepository;

    EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public List<Employee> findAll(String sortBy) {
        List<Employee> employees = new ArrayList<>();
        employeeRepository.findAll(Sort.by(sortBy)).forEach(employees::add);
        return employees;
    }
}
