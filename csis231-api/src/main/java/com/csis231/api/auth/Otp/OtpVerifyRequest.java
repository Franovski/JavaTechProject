package com.csis231.api.auth.Otp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor           // <-- needed for Jackson
@AllArgsConstructor          // (handy for tests)
public class OtpVerifyRequest {
    @NotBlank
    private String username;

    @NotBlank
    @Size(min = 6, max = 6)  // code must be 6 digits
    private String code;
}
