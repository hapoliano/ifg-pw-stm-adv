package org.atty.stm.util;

import io.quarkus.elytron.security.common.BcryptUtil;

public class PasswordUtils {

    /**
     * Gera hash BCrypt usando a implementação oficial do Quarkus.
     */
    public static String hash(String plainPassword) {
        return BcryptUtil.bcryptHash(plainPassword);
    }

    /**
     * Verifica se texto puro bate com hash BCrypt.
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) return false;
        return BcryptUtil.matches(plainPassword, hashedPassword);
    }
}
