package dev.queryable.tutorial.service.rs;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import dev.queryable.tutorial.service.AuditSinkService;

@Path("/test/audit")
@Produces(MediaType.APPLICATION_JSON)
public class AuditServiceRs {

    @Inject
    AuditSinkService auditSink;

    @GET
    public List<String> events() {
        return auditSink.events();
    }

    @DELETE
    public void clear() {
        auditSink.clear();
    }
}
