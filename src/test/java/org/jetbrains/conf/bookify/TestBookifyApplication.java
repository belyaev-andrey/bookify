package org.jetbrains.conf.bookify;

import org.springframework.boot.SpringApplication;

public class TestBookifyApplication {

    public static void main(String[] args) {
        SpringApplication.from(BookifyApplication::main).with(DbConfiguration.class).run(args);
    }

}
