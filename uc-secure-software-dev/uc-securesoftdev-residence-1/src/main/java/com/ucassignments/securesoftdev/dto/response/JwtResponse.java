package com.ucassignments.securesoftdev.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class JwtResponse {
    private String accessToken;
    private String refreshToken;
    private String username;
    private List<String> roles;

    public JwtResponse(String accessToken, String refreshToken, Long id, String username, List<String> roles) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.roles = roles;
    }
}

