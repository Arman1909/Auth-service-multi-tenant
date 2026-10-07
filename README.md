# PBM Auth POC
Needs Java 21, Maven, Docker (for Postgres), openssl.
```
./gen-keys.sh && docker compose up -d && mvn spring-boot:run
mvn test          # H2 + test-only keys, no Docker needed
```
Seed users (password `password`): `superadmin`, `tenantadmin1` (Tenant A+B), `usera1`, `userb1`.
```
T=$(curl -s -XPOST localhost:8080/api/v1/auth/login -H 'Content-Type: application/json' -d '{"username":"superadmin","password":"password"}' | jq -r .accessToken)
curl -s localhost:8080/api/v1/tenants -H "Authorization: Bearer $T"          # get tenant ids
curl -s -XPOST localhost:8080/api/v1/auth/login -H 'Content-Type: application/json' -d '{"username":"tenantadmin1","password":"password","tenantId":"<A-id>"}'
curl -s -XPOST localhost:8080/api/v1/auth/switch-tenant -H "Authorization: Bearer <jwt>" -H 'Content-Type: application/json' -d '{"tenantId":"<B-id>"}'
```
Prod env vars: `DB_URL DB_USER DB_PASSWORD JWT_PUBLIC_KEY JWT_PRIVATE_KEY` (e.g. `file:/secrets/private.pem`).
