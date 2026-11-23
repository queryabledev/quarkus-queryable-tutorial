package dev.queryable.tutorial.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.UuidGenerator;

import io.quarkiverse.queryable.runtime.QLike;
import io.quarkiverse.queryable.runtime.QLikeList;
import io.quarkiverse.queryable.runtime.QList;
import io.quarkiverse.queryable.runtime.QOrderBy;
import io.quarkiverse.queryable.runtime.QRs;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
@Table(name = "teams")
@QRs("/api/teams")
@QOrderBy("name asc")
public class Team extends PanacheEntityBase {

    @UuidGenerator
    @Id
    @QList
    public String uuid;

    @QLike
    public String name;

    @QLikeList
    public String tags;
}
