package org.atty.stm.controller;

import io.smallrye.jwt.build.Jwt;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;

@Path("/jwt")
public class JWTController {

    @GET
    @Path("/generate")
    public Response generateJWT() {

        String token = Jwt.issuer("http://localhost/issuer")
                .upn("master@stmadv.com.br")
                .groups("Admin")  // A role que vai liberar as rotas
                .claim("Nome", "Master")
                .claim("Data", LocalDate.now().toString())
                .sign();

        return Response.ok("Token criado")
                .header("token", token)
                .build();
    }


    @GET
    @Path("/public")
    @PermitAll
    public Response publico() {
        return Response.ok("Público OK").build();
    }


    @GET
    @Path("/admin")
    @RolesAllowed("Admin")
    public Response admin() {
        return Response.ok("ADMIN OK").build();
    }

}
