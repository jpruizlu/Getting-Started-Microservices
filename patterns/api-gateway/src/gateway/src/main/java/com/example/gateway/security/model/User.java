package com.example.gateway.security.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String id;
    private String username;
    private String password;
    private String email;
    private boolean active;
    private List<String> resources;
    private List<String> roles;

}
