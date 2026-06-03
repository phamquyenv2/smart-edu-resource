/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paq.utils;

import java.util.Date;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.springframework.stereotype.Component;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

/**
 *
 * @author paq-vi
 */
@Component
public class JwtUtils {

    private static final long EXPIRATION_MS = 86400000; // 1 ngày

    public static String generateToken(String username) throws Exception {
        JWSSigner signer = new MACSigner(getJwtSecret());

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(username)
                .expirationTime(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .issueTime(new Date())
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader(JWSAlgorithm.HS256),
                claimsSet
        );

        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    public static String validateTokenAndGetUsername(String token) throws Exception {
        SignedJWT signedJWT = SignedJWT.parse(token);
        JWSVerifier verifier = new MACVerifier(getJwtSecret());

        if (signedJWT.verify(verifier)) {
            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expiration.after(new Date())) {
                return signedJWT.getJWTClaimsSet().getSubject();
            }
        }
        return null;
    }

    private static String getJwtSecret() {
        String secret = System.getenv("JWT_SECRET");
        if (secret != null && !secret.isBlank()) {
            return secret;
        }

        try {
            secret = ResourceBundle.getBundle("configs").getString("jwt.secret");
            if (secret != null && !secret.isBlank()) {
                return secret;
            }
        } catch (MissingResourceException ex) {
        }

        throw new IllegalStateException("Missing JWT secret. Set JWT_SECRET or configs.properties jwt.secret.");
    }
}
