package com.swamisuite.common.security;

import java.security.interfaces.RSAPublicKey;

/** Wraps an already-known key - used by identity-service to verify its own tokens without an HTTP round-trip. */
public class StaticPublicKeySource implements PublicKeySource {

    private final RSAPublicKey key;

    public StaticPublicKeySource(RSAPublicKey key) {
        this.key = key;
    }

    @Override
    public RSAPublicKey getCurrentKey() {
        return key;
    }

    @Override
    public void invalidate() {
        // Nothing to refresh - the key is fixed for this process's lifetime.
    }
}
