package org.volodymyrzganiaiko.gym.crm.system.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.volodymyrzganiaiko.gym.crm.system.client.AuthTokenClient;
import org.volodymyrzganiaiko.gym.crm.system.dto.ServiceTokenRequest;
import org.volodymyrzganiaiko.gym.crm.system.dto.ServiceTokenResponse;

@Service
public class TokenProviderService {
    private final AuthTokenClient authTokenClient;
    private final String clientId;
    private final String clientSecret;
    private volatile String cachedToken;
    private volatile long expiresAtMs;
    private static final long REFRESH_MARGIN_MS = 60000;

    public TokenProviderService(AuthTokenClient authTokenClient, @Value("${security.service-client.id}") String clientId, @Value("${security.service-client.secret}") String clientSecret) {
        this.authTokenClient = authTokenClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public synchronized String getToken() {
        long now = System.currentTimeMillis();
        if (cachedToken == null || now >= expiresAtMs - REFRESH_MARGIN_MS) {
            ServiceTokenResponse resp = authTokenClient.getToken(new ServiceTokenRequest(clientId, clientSecret));
            cachedToken = resp.token();
            expiresAtMs = now + resp.expiresInSeconds() * 1000;
        }
        return cachedToken;
    }
}
