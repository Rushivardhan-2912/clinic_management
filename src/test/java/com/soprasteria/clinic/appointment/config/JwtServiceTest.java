package com.soprasteria.clinic.appointment.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService();
        user = new User("john", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));

        // Load keys
        PrivateKey privateKey = loadPrivateKey("/keys/private.pem");
        PublicKey publicKey = loadPublicKey("/keys/public.pem");

        // Inject fields
        ReflectionTestUtils.setField(jwtService, "privateKey", privateKey);
        ReflectionTestUtils.setField(jwtService, "publicKey", publicKey);
        ReflectionTestUtils.setField(jwtService, "expiration", 3600000L); // 1 hour
    }

    @Test
    void testGenerateTokenAndValidate() {
        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertEquals("john", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, user));
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void testExtractRoles() {
        String token = jwtService.generateToken(user);
        List<String> roles = jwtService.extractRoles(token);
        assertNotNull(roles);
        assertTrue(roles.contains("ROLE_USER"));
    }

    @Test
    void testExpiredToken() throws Exception {
        // Create expired token manually using private key
        PrivateKey privateKey = (PrivateKey) ReflectionTestUtils.getField(jwtService, "privateKey");

        String expiredToken = Jwts.builder()
                .setSubject("john")
                .claim("roles", List.of("ROLE_USER"))
                .setIssuedAt(new Date(System.currentTimeMillis() - 3600000)) // 1 hour ago
                .setExpiration(new Date(System.currentTimeMillis() - 1000)) // 1 second ago
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();

        assertTrue(jwtService.isTokenExpired(expiredToken));
    }

    private PrivateKey loadPrivateKey(String path) throws Exception {
        try (InputStream in = getClass().getResourceAsStream(path)) {
            String key = new String(in.readAllBytes())
                    .replaceAll("-----\\w+ PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] keyBytes = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePrivate(spec);
        }
    }

    private PublicKey loadPublicKey(String path) throws Exception {
        try (InputStream in = getClass().getResourceAsStream(path)) {
            String key = new String(in.readAllBytes())
                    .replaceAll("-----\\w+ PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] keyBytes = Base64.getDecoder().decode(key);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        }
    }
}
