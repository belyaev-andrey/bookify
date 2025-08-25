package org.jetbrains.conf.bookify.employee;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EmployeeMapper {

    @Mapping(
            target = "organization",
            source = "id.organization"
    )
    EmployeeSearchResponse toEmployeeSearchResponce(Employee employee);

}
