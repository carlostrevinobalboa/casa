# Casa

Aplicacion de gestion del hogar multiusuario (hogar compartido) con modulos de despensa, recetas, compra, mascotas, actividad, calendario y notificaciones.

## Stack
- Backend: Spring Boot, Spring Security, JPA/Hibernate, PostgreSQL, Redis, WebSocket, JWT, OAuth2 Google
- Frontend: Vue 3 (Composition API), Pinia, Vue Router, Tailwind, PWA
- Integraciones previstas: Google Maps Platform, Firebase Cloud Messaging

## Estado actual (Fase 2)
- Registro/login con JWT
- Login con Google OAuth2 (si configuras credenciales)
- Hogares compartidos con codigo de invitacion
- Despensa CRUD por hogar
- Alertas automaticas de stock minimo y caducidad
- Notificaciones por usuario y hogar

## Estructura
- `backend/`: API y logica de negocio
- `frontend/`: app web/PWA
- `docs/`: modelo funcional, automatizaciones y roadmap
- `docker-compose.yml`: entorno local completo

## Arranque rapido (Docker)
1. Copia variables:
   - `copy .env.example .env`
2. Levanta servicios:
   - `docker compose up --build`
3. Salud API:
   - `http://localhost:8080/api/health`
4. Frontend:
   - `http://localhost:5173`

## Variables clave
- `JWT_SECRET`: minimo 32 caracteres.
- `APP_FRONTEND_URL`: URL del frontend para redirecciones OAuth2.
- `GOOGLE_CLIENT_ID` y `GOOGLE_CLIENT_SECRET`: habilitan login Google.

## Google OAuth2 (registro/login)
1. Crea credenciales OAuth2 Web en Google Cloud Console.
2. Define redirect URI autorizada:
   - `http://localhost:8080/login/oauth2/code/google`
3. Coloca `GOOGLE_CLIENT_ID` y `GOOGLE_CLIENT_SECRET` en `.env`.
4. Inicia backend/frontend y usa el boton `Entrar con Google` en `/login`.

## Endpoints principales Fase 2
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `GET /api/households`
- `POST /api/households`
- `POST /api/households/join`
- `GET /api/households/{householdId}/pantry-items`
- `POST /api/households/{householdId}/pantry-items`
- `PUT /api/households/{householdId}/pantry-items/{itemId}`
- `DELETE /api/households/{householdId}/pantry-items/{itemId}`
- `GET /api/households/{householdId}/notifications`
- `GET /api/households/{householdId}/notifications/unread-count`
- `POST /api/households/{householdId}/notifications/{notificationId}/read`

## Nota de Java
Spring Boot 3.x requiere Java 17+ (ideal Java 21). Tu entorno local actual muestra Java 8, asi que para backend usa Docker o actualiza Java.
