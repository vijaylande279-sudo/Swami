package com.hotel.oms.dto.menu;

import jakarta.validation.constraints.NotNull;

public record UpdateAvailabilityRequest(
    @NotNull Boolean available
) {}
