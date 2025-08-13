package org.jetbrains.conf.bookify.employee;

public record EmployeeId(
        Organization organization,
        Long employeeNumber) {
}
