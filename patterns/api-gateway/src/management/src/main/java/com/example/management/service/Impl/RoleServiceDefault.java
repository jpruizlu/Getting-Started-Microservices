package com.example.management.service.Impl;

import com.example.management.model.Role;
import com.example.management.repository.RoleRepository;
import com.example.management.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class RoleServiceDefault implements RoleService {

    @Autowired
    private RoleRepository repository;

    public List<Role> findAll() { return repository.findAll(); }

    public Optional<Role> findById(String id) { return repository.findById(id); }

    public Role save(Role role){ return repository.save(role); }

    public void delete(String id){ repository.deleteById(id); }
}
