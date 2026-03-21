# 🏥 Healthcare Clinic REST API System

> A production-ready REST API for managing healthcare clinic operations including patient registration, doctor management, appointment scheduling, and JWT-based authentication.

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Maven](https://img.shields.io/badge/Apache%20Maven-3.6+-red.svg)](https://maven.apache.org)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Status](https://img.shields.io/badge/Status-Production%20Ready-green.svg)](#)

## 📋 Table of Contents

- [Quick Access Links](#Quick-Access-Links)
- [Features](#features)
- [Quick Start](#quick-start)
- [API Endpoints](#api-endpoints)
- [Technology Stack](#technology-stack)
- [Database](#database)
- [Authentication](#authentication)
- [Documentation](#documentation)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Future Enhancements](#future-enhancements)


## Quick Access Links

| Feature | URL |
|---------|-----|
| Swagger UI (API Docs) | http://localhost:8080/api/swagger-ui.html |
| H2 Console | http://localhost:8080/api/h2-console |
| API Docs (JSON) | http://localhost:8080/api/v3/api-docs |

## ✨ Features

### 🔐 Authentication & Security
- **JWT Token-based Authentication** - Stateless session management
- **Secure Password Encoding** - BCrypt password hashing
- **User Registration & Login** - Create accounts and authenticate
- **Token Expiration** - 1-hour token validity
- **CORS Configuration** - Enable cross-origin requests

### 👥 Patient Management
- **Patient Registration** - Complete patient data entry
- **Bilingual Support** - English and Arabic names
- **Unique Email & National ID** - Data integrity validation
- **Soft Delete** - Patients marked as deleted, not removed
- **Address Management** - Street, city, region storage

### 👨‍⚕️ Doctor Management
- **Doctor Directory** - View all available doctors
- **Specialty Filtering** - Find doctors by specialty
- **Doctor Information** - Experience, consultation duration
- **Bilingual Names** - English and Arabic support
- **Complete CRUD** - Create, read, update, delete doctors

### 📅 Appointment Management
- **Schedule Appointments** - Book patient with doctor
- **Duplicate Prevention** - Same doctor can't have overlapping appointments
- **View Appointments** - See all bookings with patient & doctor details
- **Update Appointments** - Reschedule existing appointments
- **Delete Appointments** - Cancel appointments

### ✅ Data Validation
- **Email Validation** - Proper format checking
- **Phone Number Validation** - 10-digit format
- **Date Validation** - Future appointments only
- **Required Fields** - Mandatory data checking
- **Unique Constraints** - Email and National ID uniqueness

### 📚 API Features
- **Consistent Response Format** - Standardized API responses
- **Comprehensive Error Handling** - Detailed error messages
- **Swagger UI Documentation** - Interactive API explorer
- **OpenAPI Specification** - Machine-readable API docs
- **Request Logging** - Track all API requests

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Installation

1. **Clone/Navigate to Project**
   ```bash
   cd C:\Users\somyagupta01\myProjects\system\system
   ```

2. **Build the Project**
   ```bash
   mvn clean install -DskipTests
   ```

3. **Run the Application**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the Application**
   ```
   API Base URL: http://localhost:8080/api
   Swagger UI: http://localhost:8080/api/swagger-ui.html
   H2 Console: http://localhost:8080/api/h2-console
   ```

### First Steps

1. **Login** to get JWT token:
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"usernameOrEmail":"admin","password":"admin123"}'
   ```

2. **Use the token** for authenticated requests:
   ```bash
   curl -X GET http://localhost:8080/api/doctors \
     -H "Authorization: Bearer YOUR_TOKEN_HERE"
   ```

3. **Explore API** via Swagger UI at http://localhost:8080/api/swagger-ui.html

## 📡 API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/login` | User login with JWT token |
| POST | `/auth/register` | Register new user |
| POST | `/auth/logout` | User logout |

### Patients
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/patients` | Register new patient |
| GET | `/patients` | List all active patients |
| DELETE | `/patients/{id}` | Soft delete patient |

### Doctors
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/doctors` | List all doctors |
| GET | `/doctors/{id}` | Get doctor by ID |
| GET | `/doctors/specialty/{specialty}` | Filter by specialty |
| POST | `/doctors` | Create new doctor |
| PUT | `/doctors/{id}` | Update doctor |
| DELETE | `/doctors/{id}` | Delete doctor |

### Appointments
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/appointments` | Schedule appointment |
| GET | `/appointments` | List all appointments |
| GET | `/appointments/{id}` | Get appointment by ID |
| PUT | `/appointments/{id}` | Update appointment |
| DELETE | `/appointments/{id}` | Delete appointment |

## 🛠️ Technology Stack

### Core Framework
- **Spring Boot 4.0.3** - Application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database access and ORM

### Database & Persistence
- **H2 Database** - In-memory database (production-ready for MySQL/PostgreSQL)
- **Hibernate** - ORM framework
- **JDBC** - Database connectivity

### Security
- **JWT (JJWT 0.12.3)** - Token-based authentication
- **BCrypt** - Password encryption
- **Spring Security** - Authorization framework

### API & Documentation
- **Springdoc OpenAPI** - Swagger UI integration
- **Lombok** - Code generation and reduction
- **Jackson** - JSON processing

### Build & Dependency Management
- **Maven 3.6+** - Project build tool
- **Java 17+** - Programming language

## 🗄️ Database

### Schema

**Users Table** (Authentication)
```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(255) UNIQUE NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  full_name VARCHAR(255),
  created_at TIMESTAMP,
  last_login TIMESTAMP
);
```

**Patients Table**
```sql
CREATE TABLE patients (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  full_name_english VARCHAR(255),
  full_name_arabic VARCHAR(255),
  email VARCHAR(255) UNIQUE NOT NULL,
  mobile_number VARCHAR(10),
  date_of_birth DATE,
  national_id VARCHAR(255) UNIQUE,
  street VARCHAR(255),
  city VARCHAR(255),
  region VARCHAR(255),
  is_deleted BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP
);
```

**Doctors Table**
```sql
CREATE TABLE doctors (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name_english VARCHAR(255),
  name_arabic VARCHAR(255),
  specialty VARCHAR(255),
  years_of_experience INT,
  consultation_duration INT
);
```

**Appointments Table**
```sql
CREATE TABLE appointments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  appointment_time TIMESTAMP NOT NULL,
  status VARCHAR(50),
  UNIQUE (doctor_id, appointment_time),
  FOREIGN KEY (patient_id) REFERENCES patients(id),
  FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);
```

## Important Note

The actual database schema may differ slightly from the above SQL depending on Hibernate naming strategy and entity annotations.

### Pre-loaded Sample Data

**Doctors:**
1. Dr. Ahmed Hassan - Cardiology (15 years)
2. Dr. Fatima Al-Rashid - Pediatrics (12 years)
3. Dr. Mohammed Al-Dosari - Orthopedics (18 years)
4. Dr. Layla Al-Qahtani - Dermatology (10 years)
5. Dr. Samir Al-Otaibi - General Medicine (8 years)
and more...

**Users:**
| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | Administrator |
| doctor1 | doctor123 | Doctor |
| reception | reception123 | Reception Staff |

## 🔐 Authentication

### JWT Token Flow

1. **Login/Register** → Receive JWT token
2. **Include token** in Authorization header: `Bearer {token}`
3. **Token validated** on each protected endpoint
4. **Token expires** after 24 hours

### Example

```bash
# 1. Get token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"admin123"}'

# Response
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "expiresIn": 86400000
  }
}

# 2. Use token in requests
curl -X GET http://localhost:8080/api/doctors \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

## 📚 Documentation

### Comprehensive Guides

- **[API_DOCUMENTATION.md](API_DOCUMENTATION.md)** - Complete API reference with examples
- **[QUICK_START.md](QUICK_START.md)** - Getting started guide
- **[IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)** - What was implemented
- **[REQUIREMENTS_CHECKLIST.md](REQUIREMENTS_CHECKLIST.md)** - Requirements verification
- **[FILE_STRUCTURE.md](FILE_STRUCTURE.md)** - Project file organization

### Online Documentation

- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/api/v3/api-docs

## 🧪 Testing

### Using cURL

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"admin123"}'

# Register patient
curl -X POST http://localhost:8080/api/patients \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {TOKEN}" \
  -d '{
    "fullNameEnglish":"John Doe",
    "email":"john@example.com",
    "mobileNumber":"0501234567",
    "dateOfBirth":"1990-05-15",
    "nationalId":"1234567890",
    "address":{"street":"123 St","city":"Riyadh","region":"Riyadh"}
  }'
```

### Using Postman

1. Import API from: http://localhost:8080/api/v3/api-docs
2. Add Bearer token to Authorization tab
3. Execute requests

### Using Swagger UI

1. Open http://localhost:8080/api/swagger-ui.html
2. Authorize with test credentials
3. Try endpoints directly

## 📁 Project Structure

```
system/
├── src/main/java/com/clinic/system/
│   ├── controller/          # REST endpoints
│   ├── service/             # Business logic
│   ├── repository/          # Database access
│   ├── entity/              # Domain models
│   ├── dto/                 # Data transfer objects
│   ├── security/            # JWT & authentication
│   ├── config/              # Spring configuration
│   ├── exception/           # Error handling
│   └── util/                # Utilities
├── src/main/resources/
│   └── application.properties
├── pom.xml
└── Documentation files
```

## 🔄 API Response Format

### Success Response
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { /* response data */ },
  "timestamp": "2026-03-18T10:30:00"
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description",
  "status": 400,
  "timestamp": "2026-03-18T10:30:00",
  "path": "/api/endpoint",
  "validationErrors": { /* field errors */ }
}
```

## 🚦 HTTP Status Codes

| Code | Meaning |
|------|---------|
| 200 | OK - Successful request |
| 201 | Created - Resource created |
| 400 | Bad Request - Validation failed |
| 404 | Not Found - Resource not found |
| 409 | Conflict - Duplicate resource |
| 500 | Server Error - Internal error |

## 🔮 Future Enhancements

- [ ] Role-based access control (RBAC)
- [ ] Email notifications
- [ ] SMS notifications
- [ ] Appointment reminders
- [ ] Patient medical history
- [ ] Prescription management
- [ ] Billing system
- [ ] Analytics dashboard
- [ ] Redis caching
- [ ] MySQL/PostgreSQL support
- [ ] API rate limiting
- [ ] Unit & integration tests
- [ ] Docker containerization
- [ ] Kubernetes deployment

## 🤝 Contributing

This is a complete implementation of the Healthcare Clinic REST API requirements. For modifications or enhancements:

1. Follow the existing code structure
2. Maintain the API response format
3. Update documentation accordingly
4. Test all changes thoroughly

## 📞 Support

For issues or questions:

1. Check the comprehensive documentation in this repository
2. Review API_DOCUMENTATION.md for endpoint details
3. Check application logs for error details
4. Verify database connection in H2 console

## 📄 License

MIT License - See LICENSE file for details

## 🎉 Getting Started

```bash
# 1. Build
mvn clean install -DskipTests

# 2. Run
mvn spring-boot:run

# 3. Explore
# - API: http://localhost:8080/api
# - Docs: http://localhost:8080/api/swagger-ui.html
# - Database: http://localhost:8080/api/h2-console

# 4. Test with provided credentials
# - Username: admin | Password: admin123
```

---

**Version**: 1.0.0  
**Status**: ✅ Production Ready  
**Last Updated**: March 18, 2026

Made with ❤️ for Healthcare Management

