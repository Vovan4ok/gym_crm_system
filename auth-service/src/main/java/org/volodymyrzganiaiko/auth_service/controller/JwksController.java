package org.volodymyrzganiaiko.auth_service.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "JWKS")
public class JwksController {
    private final RSAKey rsaKey;

    public JwksController(RSAKey rsaKey) {
        this.rsaKey = rsaKey;
    }

    @GetMapping("/oauth2/jwks")
    @Operation(summary = "Get the JSON Web Key Set", description = "Returns the public RSA key set used by resource servers to validate access tokens.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "The public JWK set"))
    public Map<String, Object> getKeySet() {
        return new JWKSet(rsaKey.toPublicJWK()).toJSONObject();
    }
}
