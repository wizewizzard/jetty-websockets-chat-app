package com.wu.chatserver.jwtauth;

import io.jsonwebtoken.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Objects;

public class JwtManager {
    @Getter
    @Setter
    private String issuer;
    @Setter
    @Getter
    private byte[] secret;

    public JwtManager(String issuer, byte[] secret) {
        Objects.requireNonNull(issuer);
        Objects.requireNonNull(secret);
        this.issuer = issuer;
        this.secret = secret;
        jwtParser = Jwts.parserBuilder()
                .setSigningKey(getSecret())
                .requireIssuer(getIssuer())
                .build();
    }

    private JwtParser jwtParser;

    public Jws<Claims> parse(String token){
        return jwtParser.parseClaimsJws(token);
    }

    public String generate(Map<String, Object> claims){
        return Jwts.builder()
                .addClaims(claims)
                .setIssuer(issuer)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }
}
