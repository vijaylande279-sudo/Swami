package com.swamisuite.identity.service;

/**
 * Delivers a password-reset link to the user. Swapping to real email once
 * notification-service exists (later phase) is a one-bean change - nothing calling
 * this interface needs to know how delivery happens.
 */
public interface ResetLinkDeliverer {
    void deliver(String email, String resetLink);
}
