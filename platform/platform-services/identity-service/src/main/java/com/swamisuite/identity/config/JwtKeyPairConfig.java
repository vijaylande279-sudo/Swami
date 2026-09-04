package com.swamisuite.identity.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Loads identity-service's RS256 key pair from PEM files if configured and present,
 * otherwise generates a fresh RSA-2048 pair and (if a path is configured) persists it
 * so restarts don't invalidate every outstanding token. With no path configured at
 * all, the pair is purely in-memory - fine for tests, but every restart invalidates
 * refresh tokens, which is logged loudly so nobody mistakes it for a bug.
 */
@Configuration
@EnableConfigurationProperties(JwtKeyProperties.class)
public class JwtKeyPairConfig {

    private static final Logger log = LoggerFactory.getLogger(JwtKeyPairConfig.class);

    @Bean
    public KeyPair identityServiceKeyPair(JwtKeyProperties properties) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String privatePath = properties.privateKeyPath();
        String publicPath = properties.publicKeyPath();

        if (privatePath != null && publicPath != null) {
            Path privateFile = Path.of(privatePath);
            Path publicFile = Path.of(publicPath);
            if (Files.exists(privateFile) && Files.exists(publicFile)) {
                return loadKeyPair(privateFile, publicFile);
            }
            KeyPair generated = generateKeyPair();
            Files.createDirectories(privateFile.toAbsolutePath().getParent());
            Files.writeString(privateFile, pem("PRIVATE KEY", generated.getPrivate().getEncoded()));
            Files.writeString(publicFile, pem("PUBLIC KEY", generated.getPublic().getEncoded()));
            log.info("Generated a new RS256 key pair and persisted it to {} / {}", privateFile, publicFile);
            return generated;
        }

        log.warn("No swamisuite.jwt.keys.* path configured - generating an EPHEMERAL RS256 key pair. "
                + "All tokens become invalid on the next restart. Fine for tests; set a path for real use.");
        return generateKeyPair();
    }

    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private KeyPair loadKeyPair(Path privateFile, Path publicFile) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        byte[] privateBytes = Base64.getDecoder().decode(stripPem(Files.readString(privateFile)));
        RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateBytes));

        byte[] publicBytes = Base64.getDecoder().decode(stripPem(Files.readString(publicFile)));
        RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(publicBytes));

        return new KeyPair(publicKey, privateKey);
    }

    private String stripPem(String pem) {
        return pem.replaceAll("-----(BEGIN|END) [A-Z ]+-----", "").replaceAll("\\s", "");
    }

    private String pem(String label, byte[] der) {
        String base64 = Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(der);
        return "-----BEGIN " + label + "-----\n" + base64 + "\n-----END " + label + "-----\n";
    }
}
