package org.atty.stm.filter;

import org.jboss.resteasy.reactive.server.ServerResponseFilter;
import jakarta.ws.rs.container.ContainerResponseContext;

public class JWTResponseFilter {

    @ServerResponseFilter
    public void process(ContainerResponseContext response) {

        String token = response.getHeaderString("token");

        if (token != null && !token.isBlank()) {
            response.getHeaders().add(
                    "Set-Cookie",
                    // configura o cookie com nome 'token' conforme seu application.properties
                    "token=" + token + "; Path=/; Max-Age=3600; HttpOnly; SameSite=Strict"
            );
        }
    }
}
