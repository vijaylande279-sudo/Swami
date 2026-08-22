package com.hotel.oms.dto.auth;

public record LoginResponse(
    String token,
    String role,
    String name
) {}
