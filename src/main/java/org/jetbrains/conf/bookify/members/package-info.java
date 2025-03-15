/**
 * This package defines the members module of the Bookify application.
 * 
 * <p>The members module is responsible for managing members of the bookify service,
 * including registration, profile management, and authentication.</p>
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Bookify Members",
    allowedDependencies = {"events"}
)
package org.jetbrains.conf.bookify.members;