package com.example.demo.security.jwt;

import com.example.demo.model.Location;
import com.example.demo.security.CustomUserDetails;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    private static final Key SIGNING_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256); // 256 bits key
    private static final Integer EXPIRATION_TIME = 1000 * 60 * 60;

    // Generate a JWT token
    public String generateToken(CustomUserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("firstName", userDetails.getFirstName());
        claims.put("lastName", userDetails.getLastName());
        claims.put("email", userDetails.getUsername());
        claims.put("phoneNumber", userDetails.getPhoneNumber());
        claims.put("crops", userDetails.getCrops());
        claims.put("roles", userDetails.getAppUserRole().name());

        // Add the location information to the claims (latitude and longitude)
        if (!userDetails.getLocations().isEmpty()) {
            Location location = userDetails.getLocations().get(0); // Assuming only one location
            Map<String, Double> value = new HashMap<>();
            value.put("latitude", location.getLatitude());
            value.put("longitude", location.getLongitude());
            claims.put("location", value);
        }

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS256) // Decode Base64 key
                .compact();
    }

    // Validate JWT token (you can implement more checks here if needed)
    public boolean validateToken(String token, org.springframework.security.core.userdetails.UserDetails userDetails) {
        String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // Extract the username from the token
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    // Check if the token has expired
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Extract the expiration date from the token
    private Date extractExpiration(String token) {
        return getClaims(token).getExpiration();
    }

    // Extract claims from the token
    private Claims getClaims(String token) {
        JwtParser parser = Jwts.parserBuilder()
                .setSigningKey(SIGNING_KEY) // Set the signing key
                .build();
        return parser.parseClaimsJws(token).getBody(); // Parse the JWT and get the claims
    }
}
