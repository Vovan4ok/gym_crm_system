# gateway-service

The single public entry point (Spring Cloud Gateway, WebFlux). All external
traffic goes through it; gym, workload and auth run only on the internal
network and are never published to the host.

## Routing
Static routes (no service discovery). `/api/login` is split by HTTP method:

| Path | Method | Target |
|---|---|---|
| `/api/trainees/**`, `/api/trainers/**`, `/api/trainings/**`, `/api/training-types/**` | any | gym-service |
| `/api/login` | POST | auth-service (issue token) |
| `/api/login` | PUT | gym-service (change password) |
| `/oauth2/refresh` | POST | auth-service |
| `/api/workload/**` | any | workload-service |

Targets are configured via `GYM_URI` / `WORKLOAD_URI` / `AUTH_URI`.

## Authentication at the edge
The gateway is an OAuth2 resource server: it validates the JWT (JWKS from
auth-service, `AUTH_JWKS_URI`) on every protected route. Registration, login
and token refresh are public.

Downstream services do **not** validate JWTs. Instead the gateway forwards the
authenticated identity in a trusted header:

- `IdentityHeaderFilter` **strips any client-supplied `X-Auth-User`** on every
  request (so it cannot be spoofed), and
- for an authenticated request, sets `X-Auth-User` to the token subject.

gym and workload turn that header into their `SecurityContext`. This is safe
only because the downstream services are unreachable except through the
gateway — hence they are not published to the host.

## CORS
Centralised here via `spring.cloud.gateway.globalcors` (allowed origins,
methods and headers), so the backend services carry no CORS config.
