package dev.queryable.tutorial.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.UuidGenerator;

import io.quarkiverse.queryable.runtime.Q;
import io.quarkiverse.queryable.runtime.QLike;
import io.quarkiverse.queryable.runtime.QList;
import io.quarkiverse.queryable.runtime.QOrderBy;
import io.quarkiverse.queryable.runtime.QRs;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
@Table(name = "projects")
@QRs("/api/projects")
@QOrderBy("name asc")
public class Project extends PanacheEntityBase {

    @UuidGenerator
    @Id
    @QList
    public String uuid;

    @QLike
    public String name;

    @Q
    public BigDecimal budget;

    @ElementCollection
    public List<String> developers_uuid = new ArrayList<>();
}
