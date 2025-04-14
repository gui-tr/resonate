# Resonate - Music Streaming Platform Backend

## Introduction

### Solution Overview
Resonate is a music streaming application similar to Bandcamp, allowing musicians to upload music and fans to stream it. This repository contains the backend API built with Quarkus, providing the core functionality for user authentication, music uploads, and streaming.

### Project Aim & Objectives
- **Main Goal**: Create a secure, scalable backend API for a music streaming platform
- **Key Objectives**:
  - Implement secure authentication and authorization
  - Create APIs for artist and fan profiles management
  - Enable music releases and tracks management
  - Provide secure audio file storage and streaming
  - Build a robust permission system for content access

## Enterprise Considerations

### Performance
- Utilizing Quarkus for its optimized startup time and low memory footprint
- Database query optimization via Panache repositories
- Efficient file handling with streaming capabilities for audio files

### Scalability
- Domain-driven design architecture for easy extension
- Modular component design with clear separation of concerns
- Stateless API design enabling horizontal scaling

### Robustness
- Comprehensive error handling with custom exception classes and mappers
- Over 80% test coverage using JaCoCo
- Transactional operations for data integrity

### Security
- JWT-based authentication with Supabase
- Row-Level Security (RLS) in PostgreSQL
- Role-based access control for resources
- Secure file storage with Backblaze B2
- Environment-based secret management

### Deployment
- CI/CD pipeline with GitHub Actions
- Heroku for application hosting
- Development and production environments
- SonarQube analysis for code quality (development environment)

## Installation & Usage Instructions

### Prerequisites
- Java 21
- Maven
- PostgreSQL database (or Supabase account)
- Backblaze B2 account (for file storage)

### Setup Steps
1. Clone the repository:
   ```
   git clone https://github.com/gui-tr/resonate-backend.git
   cd resonate-backend
   ```

2. Configure environment variables:
   - Create a `.env` file with the following variables:
     ```
     SUPABASE_URL=your_supabase_url
     SUPABASE_API_KEY=your_supabase_key
     SUPABASE_DB_URL=your_supabase_db_url
     SUPABASE_USERNAME=your_supabase_username
     SUPABASE_PASSWORD=your_supabase_password
     JWT_SECRET=your_jwt_secret
     BACKBLAZE_KEY_ID=your_backblaze_key_id
     BACKBLAZE_APPLICATION_KEY=your_backblaze_app_key
     BACKBLAZE_BUCKET_NAME=your_backblaze_bucket_name
     ```

3. Run database migrations:
   - Flyway migrations are configured to run automatically at startup

### Running the Application
```
./mvnw quarkus:dev
```

Access the API at http://localhost:8080
Swagger UI available at http://localhost:8080/swagger-ui/

## Feature Overview

### Authentication System
- **Purpose**: Secure user registration, login, and session management
- **Location**: `src/main/java/com/resonate/auth` and `src/main/java/com/resonate/api/AuthResource.java`
- **Key Endpoints**:
  - `POST /api/auth/register` - Register new users
  - `POST /api/auth/login` - Authenticate users
  - `POST /api/auth/logout` - End user session

### Profile Management
- **Purpose**: Store and manage artist and fan profiles
- **Location**: `src/main/java/com/resonate/domain/model` and `src/main/java/com/resonate/api`
- **Key Components**:
  - `ArtistProfileResource` - API for artist profiles
  - `FanProfileResource` - API for fan profiles

### Music Release Management
- **Purpose**: Allow artists to create and manage music releases
- **Location**: `src/main/java/com/resonate/api/ReleaseResource.java`
- **Key Endpoints**:
  - `POST /api/releases` - Create a new release
  - `GET /api/releases/public` - Public release catalog
  - `GET /api/releases/public/{id}` - Release details

### Track Management
- **Purpose**: Upload and manage individual tracks within releases
- **Location**: `src/main/java/com/resonate/api/TrackResource.java`
- **Key Endpoints**:
  - `POST /api/tracks` - Create a new track
  - `GET /api/tracks/{id}` - Get track details

### File Storage
- **Purpose**: Securely store and retrieve audio files
- **Location**: `src/main/java/com/resonate/storage/BackblazeStorageService.java`
- **Key Features**:
  - Secure upload URL generation
  - Temporary streaming URL generation

## Known Issues & Future Enhancements

### Current Limitations
- Authentication issues with JWT validation
- CORS configuration incomplete
- Front-end not yet deployed to production

### Planned Enhancements
- Fix authentication bug for proper JWT validation
- Complete CORS configuration
- Implement music player component
- Improve UI aesthetics and UX
- Expand test coverage
- Add subscription and payment features

## References
- [Quarkus Framework](https://quarkus.io/)
- [Supabase](https://supabase.io/)
- [Backblaze B2](https://www.backblaze.com/b2)
- [Heroku](https://www.heroku.com/)
- [GitHub Actions](https://github.com/features/actions)
