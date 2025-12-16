package org.atty.stm.controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.Location;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/privacidade")
public class PrivacidadeController {

    @Inject
    @Location("privacidade.html")
    Template privacidade;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        return privacidade.instance();
    }
}