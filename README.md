# spring-chess-backend
Springboot chess backend.

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

## registration-store Test Matrix

| Layer | Test Class | Covered Scenarios |
| --- | --- | --- |
| Controller | `RegistrationControllerTest` | Success (201), validation error (400), duplicate key (409), system/app error (500), suspicious input error (400), unexpected exception (500) |
| Controller | `RegistrationCryptoUtilityControllerTest` | Encrypt success (200), decrypt success (200), encrypt bad request via domain exception (400), encrypt unexpected exception (500), decrypt bad request via domain exception (400), decrypt unexpected exception (500) |
| Service | `RegistrationServiceImplTest` | Success response + DAO insert verification, serialization failure mapping, persistence exception propagation, bean validation failure, SQL-injection guard failure |
| DAO | `RegistrationDaoImplTest` | PostgreSQL SQL selection, Oracle SQL selection, duplicate key rethrow, `DataAccessException` to `SystemException` mapping |

### Command To Run Unit Tests

```bash
mvn -pl registration-store -am test -DfailIfNoTests=false
```
