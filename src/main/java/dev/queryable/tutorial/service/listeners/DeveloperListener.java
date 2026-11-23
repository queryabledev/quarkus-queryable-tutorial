package dev.queryable.tutorial.service.listeners;

import java.time.LocalDateTime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;

import dev.queryable.tutorial.model.Developer;
import dev.queryable.tutorial.service.AuditSinkService;
import io.quarkiverse.queryable.runtime.PostDelete;
import io.quarkiverse.queryable.runtime.PostPersist;
import io.quarkiverse.queryable.runtime.PostUpdate;
import io.quarkiverse.queryable.runtime.PrePersist;
import io.quarkiverse.queryable.runtime.PreUpdate;
import io.quarkiverse.queryable.runtime.QueryableOperation;
import io.quarkiverse.queryable.runtime.QueryableRsListener;
import io.quarkus.security.identity.SecurityIdentity;

/**
 * REST lifecycle callbacks for Developer.
 *
 * Pre-* callbacks run inside the write transaction (validation, auditing
 * fields). Post-* callbacks are registered as JTA synchronizations and run
 * only after a successful commit (external side effects).
 *
 * Since 4.0.1 the entity type is inferred from the
 * {@code QueryableRsListener<Developer>} generic, no override needed.
 */
@ApplicationScoped
public class DeveloperListener implements QueryableRsListener<Developer> {

    @Inject
    AuditSinkService auditSink;

    @Inject
    SecurityIdentity securityIdentity;

    // Validation (4.0.1): throw a Jakarta REST exception to reject the
    // request before the write; an invalid POST/PUT returns HTTP 400.
    @PrePersist
    @PreUpdate
    void validate(Developer developer) {
        if (developer.name == null || developer.name.isBlank()) {
            throw new BadRequestException("name is required");
        }
        if (developer.surname == null || developer.surname.isBlank()) {
            throw new BadRequestException("surname is required");
        }
    }

    @PrePersist
    void onCreate(Developer developer) {
        developer.creationDate = LocalDateTime.now();
        developer.creationUser = securityIdentity.getPrincipal().getName();
    }

    @PreUpdate
    void onUpdate(Developer developer) {
        Developer persisted = Developer.findById(developer.uuid);
        if (persisted != null) {
            developer.creationDate = persisted.creationDate;
            developer.creationUser = persisted.creationUser;
        }
        developer.updateDate = LocalDateTime.now();
        developer.updateUser = securityIdentity.getPrincipal().getName();
    }

    @PostPersist
    void auditPersist(Developer developer) {
        auditSink.add(QueryableOperation.CREATE, developer.uuid);
    }

    @PostUpdate
    void auditUpdate(Developer developer) {
        auditSink.add(QueryableOperation.UPDATE, developer.uuid);
    }

    @PostDelete
    void auditDelete(String uuid) {
        auditSink.add(QueryableOperation.DELETE, uuid);
    }
}
