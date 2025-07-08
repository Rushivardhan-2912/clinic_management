package com.soprasteria.clinic.appointment.config;

import com.soprasteria.clinic.appointment.exception.ClinicExceptionHandler;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.*;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @Value("${jwt.expiration.ms}")
    private long expiration;

    @PostConstruct
    public void loadKeys() {
        try (InputStream privateKeyStream = getClass().getResourceAsStream("/keys/private.pem");
             InputStream publicKeyStream = getClass().getResourceAsStream("/keys/public.pem")) {

            if (privateKeyStream == null || publicKeyStream == null) {
                throw new ClinicExceptionHandler.KeyLoadingException("Private or public key file not found in resources.");
            }

            // Load private key
            String privateKeyContent = new String(privateKeyStream.readAllBytes())
                    .replaceAll("-----\\w+ PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyContent);
            PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            privateKey = keyFactory.generatePrivate(privateSpec);

            // Load public key
            String publicKeyContent = new String(publicKeyStream.readAllBytes())
                    .replaceAll("-----\\w+ PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyContent);
            X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicKeyBytes);
            publicKey = keyFactory.generatePublic(publicSpec);

        } catch (Exception e) {
            throw new ClinicExceptionHandler.KeyLoadingException("Failed to load RSA keys", e);
        }
    }

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities().stream()
                        .map(auth -> auth.getAuthority())
                        .toList())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .setAllowedClockSkewSeconds(5)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public List<String> extractRoles(String token) {
        return extractClaims(token).get("roles", List.class);
    }
}