package org.jetbrains.conf.bookify.employee;

import org.springframework.data.repository.PagingAndSortingRepository;

interface EmployeeRepository extends PagingAndSortingRepository<Employee, EmployeeId> {

}
