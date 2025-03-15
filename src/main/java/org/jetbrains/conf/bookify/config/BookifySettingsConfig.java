package org.jetbrains.conf.bookify.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Name;

@ConfigurationProperties(prefix = "bookify")
public class BookifySettingsConfig {

    @Name("maximum.books.borrowed")
    private int maximumBooksBorrowed = 0;

    @Name("overdue.days")
    private int overdueDays = 0;

    public int getMaximumBooksBorrowed() {
        return maximumBooksBorrowed;
    }

    public int getOverdueDays() {
        return overdueDays;
    }

    public void setMaximumBooksBorrowed(int maximumBooksBorrowed) {
        this.maximumBooksBorrowed = maximumBooksBorrowed;
    }

    public void setOverdueDays(int overdueDays) {
        this.overdueDays = overdueDays;
    }
}
