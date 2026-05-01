# spring-chess-backend
Springboot chess backend.

## Run as Containers

Build and run Spring Boot server with PostgreSQL, Redis, and Stockfish:

```bash
docker compose up --build
```

Stop containers:

```bash
docker compose down
```

The API server is available at:

- `http://localhost:8081`

Container ports:

- PostgreSQL: `localhost:5432`
- Redis: `localhost:6379`
- Stockfish TCP (UCI bridge): `localhost:8088`

Stockfish is available to the Spring container via:

- `STOCKFISH_HOST=stockfish`
- `STOCKFISH_PORT=8080`

## Encrypted Registration API Envelope

The registration API uses an encrypted envelope for both request and response:

```json
{
  "data": "Base64(...)",
  "token": "Base64(...)",
  "digiSign": "Base64(...)"
}
```

- `data`: AES-256-GCM encrypted payload bytes in the format `Base64(IV || ciphertext+tag)`.
  - IV size is 12 bytes.
  - GCM tag size is 128 bits.
- `token`: RSA-encrypted AES key (AES key is a dynamic 32-char alphanumeric string), Base64-encoded.
- `digiSign`: RSA-encrypted SHA-256 digest of the plaintext JSON payload, Base64-encoded.

All three fields are required and must be valid Base64 in incoming requests.

## Non-Prod Crypto Utility Endpoints

These endpoints exist only in non-production profiles (`@Profile("!prod")`) to help test
client-side encryption/decryption.

- `POST /api/v1/registrations/crypto-utils/encrypt-request`
  - Input: plain JSON body
  - Output: encrypted envelope (`data`, `token`, `digiSign`)

- `POST /api/v1/registrations/crypto-utils/decrypt-response`
  - Input: encrypted envelope (`data`, `token`, `digiSign`)
  - Output: plain decrypted JSON as `text/plain`

### Sample curl

Encrypt plain request into envelope:

```bash
curl -X POST "http://localhost:8081/api/v1/registrations/crypto-utils/encrypt-request" \
  -H "Content-Type: application/json" \
  -d '{"registrationNo":"sf4t4e354dff20","rollNo":"2342423535"}'
```

Decrypt envelope back to plain JSON:

```bash
curl -X POST "http://localhost:8081/api/v1/registrations/crypto-utils/decrypt-response" \
  -H "Content-Type: application/json" \
  -d '{"data":"<base64>","token":"<base64>","digiSign":"<base64>"}'
```

### Command To Run Unit Tests

```bash
mvn test -DfailIfNoTests=false
```
