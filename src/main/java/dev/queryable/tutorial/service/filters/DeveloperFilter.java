package dev.queryable.tutorial.service.filters;

import java.util.Locale;

import jakarta.enterprise.context.ApplicationScoped;

import dev.queryable.tutorial.model.Developer;
import io.quarkiverse.queryable.runtime.QueryableFilter;
import io.quarkiverse.queryable.runtime.QueryableFilterContext;

/**
 * Custom filter contributor: the {@code search} query parameter matches
 * name OR surname (case-insensitive), something the annotation-driven
 * filters cannot express. The entity type is inferred from the
 * {@code QueryableFilter<Developer>} generic (4.0.1).
 */
@ApplicationScoped
public class DeveloperFilter implements QueryableFilter<Developer> {

    @Override
    public void contribute(QueryableFilterContext<Developer> context) {
        String search = context.param("search");
        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.toLowerCase(Locale.ROOT) + "%";
            context.add(context.criteriaBuilder().or(
                    context.criteriaBuilder().like(
                            context.criteriaBuilder().lower(context.root().get("name")), pattern),
                    context.criteriaBuilder().like(
                            context.criteriaBuilder().lower(context.root().get("surname")), pattern)));
        }
    }
}
