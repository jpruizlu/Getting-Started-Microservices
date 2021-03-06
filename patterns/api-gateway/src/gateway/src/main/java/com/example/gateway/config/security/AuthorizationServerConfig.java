package com.example.gateway.config.security;

import com.example.gateway.config.bean.AuthenticationProperties;
import com.example.gateway.config.bean.AuthorityProperties;
import com.example.gateway.security.CustomTokenEnhancer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.util.Arrays;
import java.util.Map;

@Slf4j
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    public static final String AUTHORITIES_TRUSTED = "ROLE_TRUSTED_CLIENT";
    public static final String AUTHORITIES_NORMAL = "ROLE_NORMAL_CLIENT";
    public static final String AUTHORITIES_BASIC = "ROLE_BASIC_CLIENT";

    public static final String AUTHORIZATION_CODE = "authorization_code";
    public static final String GRANT_TYPE_PASSWORD = "password";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String IMPLICIT = "implicit";

    public static final String SCOPE_READ = "read";
    public static final String SCOPE_WRITE = "write";
    public static final String SCOPE_TRUST = "trust";

    @Autowired
    private AuthenticationProperties authProperties;

    @Autowired
    private BCryptPasswordEncoder bcryptEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setSigningKey(authProperties.getSymmetricKey());

        /*
        // Add key-pair signing method instead symmetric. See 'resources/README.md'
        KeyStoreKeyFactory keyStoreKeyFactory =
                new KeyStoreKeyFactory(new ClassPathResource("sign-key.jks"), "password".toCharArray());
        converter.setKeyPair(keyStoreKeyFactory.getKeyPair("sign-key"));
         */

        return converter;
    }

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }

    @Bean
    public TokenEnhancer tokenEnhancer() {return new CustomTokenEnhancer(); }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        oauthServer.tokenKeyAccess("isAnonymous() || hasAuthority('"+ AUTHORITIES_TRUSTED +"')")
                .checkTokenAccess("hasAuthority('" + AUTHORITIES_TRUSTED + "')");
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer configure) throws Exception {
        if (authProperties.getAuthorities() == null) return;
        log.debug("Configuring client authorities: {}", authProperties.getAuthorities().keySet());
        Map<AuthenticationProperties.AuthorityType, AuthorityProperties> authorities = authProperties.getAuthorities();
        configure.inMemory()
                .withClient(authorities.get(AuthenticationProperties.AuthorityType.basic).getName())
                    .resourceIds(authProperties.getResourceId())
                    .authorities(AUTHORITIES_BASIC)
                    .secret(bcryptEncoder.encode(authorities.get(AuthenticationProperties.AuthorityType.basic).getSecret()))
                    .authorizedGrantTypes(GRANT_TYPE_PASSWORD, AUTHORIZATION_CODE, IMPLICIT )
                    //.autoApprove(true)
                    .scopes(SCOPE_READ)
                    .accessTokenValiditySeconds(authProperties.getAccessTokenValidity())
                    .refreshTokenValiditySeconds(authProperties.getRefreshTokenValidity())
                .and()
                .withClient(authorities.get(AuthenticationProperties.AuthorityType.normal).getName())
                    .resourceIds(authProperties.getResourceId())
                    .authorities(AUTHORITIES_NORMAL)
                    .secret(bcryptEncoder.encode(authorities.get(AuthenticationProperties.AuthorityType.normal).getSecret()))
                    .authorizedGrantTypes(GRANT_TYPE_PASSWORD, AUTHORIZATION_CODE, IMPLICIT )
                    .scopes(SCOPE_READ, SCOPE_WRITE)
                    .accessTokenValiditySeconds(authProperties.getAccessTokenValidity())
                    .refreshTokenValiditySeconds(authProperties.getRefreshTokenValidity())
                .and()
                .withClient(authorities.get(AuthenticationProperties.AuthorityType.trusted).getName())
                    .resourceIds(authProperties.getResourceId())
                    .authorities(AUTHORITIES_TRUSTED)
                    .secret(bcryptEncoder.encode(authorities.get(AuthenticationProperties.AuthorityType.trusted).getSecret()))
                    .authorizedGrantTypes(GRANT_TYPE_PASSWORD, AUTHORIZATION_CODE, REFRESH_TOKEN, IMPLICIT )
                    .scopes(SCOPE_READ, SCOPE_WRITE, SCOPE_TRUST)
                    .accessTokenValiditySeconds(authProperties.getAccessTokenValidity())
                    .refreshTokenValiditySeconds(authProperties.getRefreshTokenValidity());
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        tokenEnhancerChain.setTokenEnhancers(Arrays.asList(tokenEnhancer(), accessTokenConverter()));
        endpoints.tokenStore(tokenStore())
                .authenticationManager(authenticationManager)
                .tokenEnhancer(tokenEnhancerChain);
    }
}
