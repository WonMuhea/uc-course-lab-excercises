package com.ucassignments.securesoftdev.dto.request;

import lombok.Data;
import java.util.Set;

@Data
public class SignupRequest {
    private String username;
    private String password;
    private Set<String> role;
}
