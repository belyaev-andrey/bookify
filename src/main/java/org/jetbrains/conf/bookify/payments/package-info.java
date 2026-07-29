/**
 * This package defines the payments module of the Bookify application.
 *
 * <p>The payments module is responsible for managing fines for overdue books,
 * including fine rate configuration and fine processing.</p>
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Bookify Payments",
        allowedDependencies = {"events"}
)
@NullMarked
package org.jetbrains.conf.bookify.payments;

import org.jspecify.annotations.NullMarked;
