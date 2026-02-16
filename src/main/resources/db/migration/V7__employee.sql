create table employee
(
    organization    varchar(20),
    employee_number int,
    name            varchar(100),
    birth_date      timestamp,
    email           varchar(255),
    constraint employee_pk primary key (employee_number, organization)
);

-- Add employee reference columns to borrowing table
ALTER TABLE borrowing
    ADD COLUMN employee_organization VARCHAR(20),
    ADD COLUMN employee_number       INT;

-- Add foreign key constraint to reference employee table
ALTER TABLE borrowing
    ADD CONSTRAINT fk_borrowing_employee
        FOREIGN KEY (employee_number, employee_organization)
            REFERENCES employee (employee_number, organization);
