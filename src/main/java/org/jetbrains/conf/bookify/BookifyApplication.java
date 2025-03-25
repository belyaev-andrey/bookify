package org.jetbrains.conf.bookify;

import org.jetbrains.conf.bookify.config.BookifySettingsConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.modulith.Modulithic;

@Modulithic
@SpringBootApplication
@EnableConfigurationProperties({BookifySettingsConfig.class})
public class BookifyApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookifyApplication.class, args);
    }

}
