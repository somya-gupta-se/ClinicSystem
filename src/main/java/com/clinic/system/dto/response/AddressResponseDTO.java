package com.clinic.system.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressResponseDTO {
    private String street;
    private String city;
    private String region;
}