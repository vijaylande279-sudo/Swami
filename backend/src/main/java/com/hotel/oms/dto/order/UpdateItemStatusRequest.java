package com.hotel.oms.dto.order;

import jakarta.validation.constraints.NotBlank;

public record UpdateItemStatusRequest(
    @NotBlank String status
) {}
