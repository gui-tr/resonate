# resonate

A Quarkus-based microservice for managing artist profiles, fan profiles, music releases, and tracks. This project uses Supabase for authentication and Backblaze for storage.

**Security Notes:**  
• A new JWT-based security mechanism is now in place with a dedicated filter for token validation.  
• Logging has been switched to SLF4J, and all relevant endpoints now use this logging framework.  
• Configuration for Supabase, Backblaze, and JWT secrets is expected to be provided externally (via config maps or environment variables).

## Building and Running

Use Maven to build:
```bash
./mvnw clean package
