package dev.queryable.tutorial.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.SqlFragmentAlias;

import io.quarkiverse.queryable.runtime.Q;
import io.quarkiverse.queryable.runtime.QFilter;
import io.quarkiverse.queryable.runtime.QLike;
import io.quarkiverse.queryable.runtime.QList;
import io.quarkiverse.queryable.runtime.QLogicalDelete;
import io.quarkiverse.queryable.runtime.QNil;
import io.quarkiverse.queryable.runtime.QNotNil;
import io.quarkiverse.queryable.runtime.QRs;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
@QRs("/api/v1/customers")
// Hibernate filter with a SQL subquery: Hibernate owns the SQL condition,
// @QFilter connects REST query parameters to the filter parameters (4.0.1).
@FilterDef(name = "Customer.coordinator", parameters = {
        @ParamDef(name = "coordinator_id", type = BigDecimal.class),
        @ParamDef(name = "country", type = String.class)
})
@Filter(name = "Customer.coordinator", condition = "exists (select 1 from Customer coordinator where coordinator.id = {customer}.id "
        + "and coordinator.id = :coordinator_id and coordinator.country = :country)", deduceAliasInjectionPoints = false, aliases = @SqlFragmentAlias(alias = "customer", table = "Customer"))
@QFilter(name = "Customer.coordinator", queryParam = "obj.coordinator_id", filterParam = "coordinator_id")
@QFilter(name = "Customer.coordinator", queryParam = "obj.coordinator_country", filterParam = "country")
public class Customer extends PanacheEntity {

    @QLike
    public String name;

    @Q(name = "externalCode")
    public String externalCode;

    @QList(name = "countries")
    public String country;

    @Q
    public LocalDate createdOn;

    @QNil
    @QNotNil(prefix = "notNil")
    public String notes;

    @Q
    public LocalDate retiredOn;

    @QLogicalDelete
    public boolean active = true;

    @Embedded
    public Address address = new Address();

    public LocalDateTime creationDate;
    public String creationUser;
    public LocalDateTime updateDate;
    public String updateUser;
}
