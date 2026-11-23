package dev.queryable.tutorial.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;

import io.quarkiverse.queryable.runtime.Q;
import io.quarkiverse.queryable.runtime.QLike;
import io.quarkiverse.queryable.runtime.QList;
import io.quarkiverse.queryable.runtime.QLogicalDelete;
import io.quarkiverse.queryable.runtime.QNil;
import io.quarkiverse.queryable.runtime.QNotNil;
import io.quarkiverse.queryable.runtime.QRs;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
@QRs("/api/v1/customers")
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
