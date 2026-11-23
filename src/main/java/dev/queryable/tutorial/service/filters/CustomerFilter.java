package dev.queryable.tutorial.service.filters;

import jakarta.enterprise.context.ApplicationScoped;

import dev.queryable.tutorial.model.Customer;
import io.quarkiverse.queryable.runtime.QueryableFilter;
import io.quarkiverse.queryable.runtime.QueryableFilterContext;

/**
 * Custom filter contributor: adds a Criteria API predicate for the
 * {@code address.city} query parameter, which is not covered by the
 * annotation-driven filters.
 *
 * Since 4.0.1 the entity type is inferred from the
 * {@code QueryableFilter<Customer>} generic, no override needed.
 */
@ApplicationScoped
public class CustomerFilter implements QueryableFilter<Customer> {

    @Override
    public void contribute(QueryableFilterContext<Customer> context) {
        String city = context.param("address.city");
        if (city != null) {
            context.add(context.criteriaBuilder().equal(
                    context.root().get("address").get("city"), city));
        }
    }
}
