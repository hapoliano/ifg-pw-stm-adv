package org.atty.stm.util;

import io.smallrye.jwt.build.Jwt;
import java.util.Set;

/**
 * Utility para criação de tokens JWT usando a API SmallRye JWT Build.
 */
public class TokenUtils {

    public static String generateJwt(String email, String userId, String nome, Set<String> roles, String issuer, long expiresInSeconds) throws Exception {
        long currentTimeInSecs = (System.currentTimeMillis() / 1000);

        return Jwt.issuer(issuer)
                .subject(email) // O 'sub' (subject)
                .upn(userId) // O 'upn' (Universal Principal Name)
                .groups(roles) // Os 'groups' (roles/perfis)
                .claim("name", nome)
                .issuedAt(currentTimeInSecs)
                .expiresIn(expiresInSeconds)
                .sign();
    }
}