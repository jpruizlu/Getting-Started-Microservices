package com.example.auth.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document("users")
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id private String id;
    @Indexed(unique = true) private String username;
    private String password;
    private String email;
    private boolean active;
    private List<String> resources;
    private List<String> roles;
}
