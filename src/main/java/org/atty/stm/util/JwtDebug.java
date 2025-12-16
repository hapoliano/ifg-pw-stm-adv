package org.atty.stm.util;

import org.eclipse.microprofile.jwt.JsonWebToken;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JwtDebug {

    public void logToken(JsonWebToken jwt) {
        if (jwt == null) {
            System.out.println("[JWT DEBUG] Token é nulo!");
            return;
        }

        System.out.println("[JWT DEBUG] Token recebido:");
        System.out.println("  Principal: " + jwt.getName());
        System.out.println("  Raw Claims:");
        jwt.getClaimNames().forEach(name ->
                System.out.println("    " + name + " = " + jwt.getClaim(name))
        );
    }

    public void logComparison(String esperado, String recebido) {
        System.out.println("[JWT DEBUG] Comparando valores:");
        System.out.println("  Esperado: " + esperado);
        System.out.println("  Recebido: " + recebido);
        System.out.println("  Resultado: " + esperado.equals(recebido));
    }

    // NOVO: log genérico
    public void log(String mensagem) {
        System.out.println("[JWT DEBUG] " + mensagem);
    }
}
