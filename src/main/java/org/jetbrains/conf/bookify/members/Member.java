package org.jetbrains.conf.bookify.members;

import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * Represents a member of the Bookify service.
 */
@Table("member")
class Member implements Persistable<UUID> {
    @Id
    private UUID id;
    @Column("name")
    @Nullable
    private String name;
    @Column("email")
    @Nullable
    private String email;
    @Column("password")
    @Nullable
    private String password;
    @Column("enabled")
    private boolean enabled;

    @Transient
    private boolean isNew = true;

    Member() {
        this.id = UUID.randomUUID();
        this.enabled = true;
    }

    @PersistenceCreator
    Member(UUID id, String name, String email, String password, boolean enabled) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.enabled = enabled;
        this.isNew = false;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public @Nullable String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public @Nullable String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public @Nullable String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
