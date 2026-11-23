package dev.queryable.tutorial.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.UuidGenerator;

import io.quarkiverse.queryable.runtime.QLike;
import io.quarkiverse.queryable.runtime.QList;
import io.quarkiverse.queryable.runtime.QLogicalDelete;
import io.quarkiverse.queryable.runtime.QOrderBy;
import io.quarkiverse.queryable.runtime.QRs;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
@Table(name = "developers")
@QRs("/api/developers")
@QOrderBy("surname asc")
public class Developer extends PanacheEntityBase {

    @UuidGenerator
    @Id
    @QList
    public String uuid;

    @QLike
    public String name;

    @QLike
    public String surname;

    @QList
    public String team_uuid;

    @QLogicalDelete
    public boolean active = true;
}
