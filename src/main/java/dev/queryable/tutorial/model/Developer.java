package dev.queryable.tutorial.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.SqlFragmentAlias;
import org.hibernate.annotations.UuidGenerator;

import io.quarkiverse.queryable.runtime.Q;
import io.quarkiverse.queryable.runtime.QFilter;
import io.quarkiverse.queryable.runtime.QLike;
import io.quarkiverse.queryable.runtime.QList;
import io.quarkiverse.queryable.runtime.QLogicalDelete;
import io.quarkiverse.queryable.runtime.QNil;
import io.quarkiverse.queryable.runtime.QNotNil;
import io.quarkiverse.queryable.runtime.QOrderBy;
import io.quarkiverse.queryable.runtime.QRs;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
@Table(name = "developers")
@QRs("/api/developers")
@QOrderBy("surname asc")
// Hibernate filter with a SQL subquery on the Project membership table:
// Hibernate owns the SQL condition, @QFilter connects the REST query
// parameter to the filter parameter. GET /api/developers?obj.project_uuid=<uuid>
// returns only the developers assigned to that project.
@FilterDef(name = "Developer.project", parameters = @ParamDef(name = "project_uuid", type = String.class))
@Filter(name = "Developer.project", condition = "exists (select 1 from project_developers pd "
        + "where pd.developer_uuid = {developer}.uuid "
        + "and pd.project_uuid = :project_uuid)", deduceAliasInjectionPoints = false, aliases = @SqlFragmentAlias(alias = "developer", table = "developers"))
@QFilter(name = "Developer.project", queryParam = "obj.project_uuid", filterParam = "project_uuid")
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

    @Q
    public LocalDate hiredOn;

    @QNil
    @QNotNil(prefix = "notNil")
    public String notes;

    @QLogicalDelete
    public boolean active = true;

    public LocalDateTime creationDate;
    public String creationUser;
    public LocalDateTime updateDate;
    public String updateUser;
}
