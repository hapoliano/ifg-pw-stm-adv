package org.atty.stm.controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.Location;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/contato")
public class ContatoController {

    @Inject
    @Location("contato.html")
    Template contato;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        return contato.instance();
    }
}