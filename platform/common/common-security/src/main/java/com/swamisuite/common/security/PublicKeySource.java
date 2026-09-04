package com.swamisuite.common.security;

import java.security.interfaces.RSAPublicKey;

/**
 * Supplies the RSA public key currently used to verify identity-service-issued
 * JWTs. Implementations decide how/when to fetch and cache it (see
 * {@link CachingJwksPublicKeySource}); nothing here ever touches a private key.
 */
public interface PublicKeySource {

    RSAPublicKey getCurrentKey();

    /**
     * Forces a re-fetch on the next {@link #getCurrentKey()} call - used when a
     * verification failure looks like it could be a stale/rotated key.
     */
    void invalidate();
}
