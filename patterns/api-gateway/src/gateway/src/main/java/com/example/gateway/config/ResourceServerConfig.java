package com.example.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(ResourceServerSecurityConfigurer config) {
        config.resourceId(AuthorizationServerConfig.RESOURCE_ID);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            //.anonymous().disable() // Allows access to swagger
            .authorizeRequests()
                 .antMatchers("/**/v2/api-docs", "/**/configuration/ui", "/**/swagger-resources/**",
                        "/**/configuration/**", "/**/swagger-ui.html", "/**/webjars/**").permitAll()
                .antMatchers("/api/**").access("hasRole('ADMIN') or hasRole('USER')")
            .and()
            .exceptionHandling()
                 .accessDeniedHandler(new OAuth2AccessDeniedHandler());
    }
}
