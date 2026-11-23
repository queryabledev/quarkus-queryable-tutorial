package dev.queryable.tutorial.service.listeners;

import java.time.LocalDateTime;
import java.util.Locale;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import dev.queryable.tutorial.model.Customer;
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
 * REST lifecycle callbacks for Customer.
 *
 * Pre-* callbacks run inside the write transaction (auditing fields,
 * normalization). Post-* callbacks are registered as JTA synchronizations
 * and run only after a successful commit (external side effects).
 */
@ApplicationScoped
public class CustomerListener implements QueryableRsListener<Customer> {

    @Inject
    AuditSinkService auditSink;

    @Inject
    SecurityIdentity securityIdentity;

    @Override
    public Class<Customer> entityType() {
        return Customer.class;
    }

    @PrePersist
    void normalize(Customer customer) {
        if (customer.externalCode != null) {
            customer.externalCode = customer.externalCode.trim().toUpperCase(Locale.ROOT);
        }
        customer.creationDate = LocalDateTime.now();
        customer.creationUser = securityIdentity.getPrincipal().getName();
    }

    @PreUpdate
    void preUpdate(Customer customer) {
        Customer persisted = Customer.findById(customer.id);
        if (persisted != null) {
            customer.creationDate = persisted.creationDate;
            customer.creationUser = persisted.creationUser;
        }
        customer.updateDate = LocalDateTime.now();
        customer.updateUser = securityIdentity.getPrincipal().getName();
    }

    @PostPersist
    void auditPersist(Customer customer) {
        auditSink.add(QueryableOperation.CREATE, customer.id);
    }

    @PostUpdate
    void auditUpdate(Customer customer) {
        auditSink.add(QueryableOperation.UPDATE, customer.id);
    }

    @PostDelete
    void auditDelete(Long id) {
        auditSink.add(QueryableOperation.DELETE, id);
    }
}
