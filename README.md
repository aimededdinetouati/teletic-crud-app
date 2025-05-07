# Teletic CRUD Task Management System - Documentation

## Table of Contents
- [Overview](#overview)
- [System Architecture](#system-architecture)
- [Installation and Setup](#installation-and-setup)
  - [Prerequisites](#prerequisites)
  - [Setting Up the Development Environment](#setting-up-the-development-environment)
  - [Running in Production](#running-in-production)
- [Backend API](#backend-api)
  - [Authentication Endpoints](#authentication-endpoints)
  - [User Management Endpoints](#user-management-endpoints)
  - [Task Management Endpoints](#task-management-endpoints)
- [Frontend Application](#frontend-application)
  - [Authentication Module](#authentication-module)
  - [Admin Module](#admin-module)
  - [User Module](#user-module)
  - [Shared Components](#shared-components)
- [Database Structure](#database-structure)
- [Security Features](#security-features)
- [Development Guidelines](#development-guidelines)
- [Troubleshooting](#troubleshooting)

## Overview

The Teletic CRUD Task Management System is a web-based application that allows organizations to manage tasks and users. The system implements role-based access control with two types of users: administrators and regular users.

### Key Features:

- **Role-Based Access Control**: Two user roles - Admin and User
- **User Management**: Admins can create, view, update, and delete users
- **Task Management**: Admins can create, assign, and manage tasks
- **Task Assignment**: Tasks can be assigned to specific users
- **Task Status Tracking**: Users can update the status of their assigned tasks
- **Responsive UI**: Modern, responsive user interface for all screens

## System Architecture

The application is built using a modern tech stack with separate frontend and backend components:

### Backend:
- **Framework**: Spring Boot 3.3.11
- **Language**: Kotlin 1.9.25
- **Database**: PostgreSQL 15
- **Security**: JWT-based authentication
- **API Documentation**: OpenAPI/Swagger
- **Build Tool**: Gradle 8.13

### Frontend:
- **Framework**: Angular 19.2.10
- **Language**: TypeScript
- **UI Libraries**: Bootstrap 5.3.6 and FontAwesome 6.7.2
- **HTTP Client**: Angular's HttpClient with OpenAPI-generated services
- **Build Tool**: Node.js and npm

### Containerization:
- Docker containers for all components
- Docker Compose for orchestration

## Installation and Setup

### Prerequisites

- Docker and Docker Compose
- JDK 17 (for local development)
- Node.js 18+ and npm (for local development)
- PostgreSQL (optional for local development)

### Setting Up the Development Environment

#### Clone the Repository

```bash
git clone https://github.com/yourusername/teletic-crud-app.git
cd teletic-crud-app
```

#### Running with Docker Compose

The easiest way to run the entire application is using Docker Compose:

```bash
docker-compose up -d
```

This will:
1. Start a PostgreSQL database container
2. Build and start the Spring Boot backend container
3. Build and start the Angular frontend container
4. Configure networking between all services

The application will be available at:
- Frontend: http://localhost:4200
- Backend API: http://localhost:8080/api
- API Documentation: http://localhost:8080/api/swagger-ui.html

#### Docker Configuration Details

The application uses the following Docker setup:

**Database (PostgreSQL):**
- Container name: postgres-teletic
- Port mapping: 5432:5432
- Environment variables:
  - POSTGRES_USER: devuser
  - POSTGRES_PASSWORD: devpassword
  - POSTGRES_DB: devdb
- Volumes: 
  - Persistent data volume for database files
  - Initialization SQL script mounted from ./postgres/init.sql

**Backend (Spring Boot):**
- Built from custom Dockerfile in ./backend
- Container name: backend
- Port mapping: 8080:8080
- Environment variables:
  - SPRING_PROFILES_ACTIVE: dev
  - SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-db:5432/devdb
  - SPRING_DATASOURCE_USERNAME: devuser
  - SPRING_DATASOURCE_PASSWORD: devpassword
- Dependencies: postgres-db service

**Frontend (Angular):**
- Built from custom Dockerfile in ./frontend
- Container name: frontend
- Port mapping: 4200:4200
- Volumes: 
  - ./frontend/src mounted to /app/src for live development
- Dependencies: backend service

#### Running Each Component Separately

**Backend:**

```bash
cd backend
./gradlew bootRun
```

**Frontend:**

```bash
cd frontend
npm install
npm start
```

Note: The frontend is configured with a proxy to automatically forward API requests to the backend during development.

### Running in Production

For production deployment, build optimized versions and use appropriate environment configurations:

**Backend:**

```bash
cd backend
./gradlew build -Pprod
```

**Frontend:**

```bash
cd frontend
npm run build -- --configuration=production
```

## Backend API

The backend provides a RESTful API with the following key endpoints:

### Authentication Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | /api/auth/register | Register a new user | Public |
| POST | /api/auth/login | Authenticate and get JWT token | Public |

### User Management Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | /api/users | Get all users (with pagination and filtering) | Admin |
| GET | /api/users/{userId} | Get a specific user | Admin |
| POST | /api/users | Create a new user | Admin |
| PUT | /api/users/{userId} | Update an existing user | Admin |
| DELETE | /api/users/{userId} | Delete a user | Admin |

### Task Management Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | /api/tasks | Get all tasks (with pagination and filtering) | Admin |
| GET | /api/tasks/{id} | Get a specific task | Admin, Assigned User |
| POST | /api/tasks | Create a new task | Admin |
| PUT | /api/tasks/{id} | Update an existing task | Admin |
| DELETE | /api/tasks/{id} | Delete a task | Admin |
| PATCH | /api/tasks/{id}/assign | Assign a task to a user | Admin |
| PATCH | /api/tasks/{id}/status | Update task status | Admin, Assigned User |
| GET | /api/tasks/my | Get current user's tasks | User |

### API Documentation

The API is documented using OpenAPI (Swagger), which is available at `/api/swagger-ui.html` when the application is running.

## Frontend Application

The frontend application is built with Angular and organized into feature modules:

### Authentication Module

- **Login Component**: User authentication with email and password
- **Register Component**: New user registration form
- **Auth Guards**: Prevent unauthorized access to protected routes
- **Auth Interceptor**: Automatically adds JWT tokens to API requests

### Admin Module

- **Dashboard**: Overview of system statistics
- **User List**: View, search, and manage users
- **User Form**: Create or edit user details
- **Task List**: View, search, and manage tasks
- **Task Form**: Create or edit task details
- **Task Assignment**: Assign tasks to users

### User Module

- **Dashboard**: Overview of assigned tasks and statistics
- **Task List**: View all tasks assigned to the current user
- **Task Detail**: View and update the status of an assigned task

### Shared Components

- **Main Layout**: Common layout with header and sidebar
- **Loading Spinner**: Animated loading indicator
- **Alert Component**: Display success/error messages
- **Confirm Dialog**: Confirmation popup for critical actions

## Database Structure

The application uses a PostgreSQL database with the following main tables:

### Users Table

Stores information about system users:

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary key, auto-increment |
| full_name | VARCHAR(100) | User's full name |
| email | VARCHAR(50) | User's email address (unique) |
| password | VARCHAR(100) | Encrypted password |
| locked | BOOLEAN | Whether the account is locked |
| enabled | BOOLEAN | Whether the account is active |
| created_date | TIMESTAMP | When the account was created |
| last_modified_date | TIMESTAMP | When the account was last modified |

### Roles Table

Stores user roles:

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary key, auto-increment |
| name | VARCHAR(50) | Role name (e.g., ROLE_ADMIN, ROLE_USER) |
| created_date | TIMESTAMP | When the role was created |
| last_modified_date | TIMESTAMP | When the role was last modified |

### Users_Roles Table

Junction table for the many-to-many relationship between users and roles:

| Column | Type | Description |
|--------|------|-------------|
| users_id | BIGINT | Foreign key to Users table |
| roles_id | BIGINT | Foreign key to Roles table |

### Tasks Table

Stores task information:

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary key, auto-increment |
| title | VARCHAR(100) | Task title |
| description | TEXT | Task description |
| status | VARCHAR(20) | Task status (TO_DO, IN_PROGRESS, COMPLETED, CANCELLED) |
| due_date | TIMESTAMP | Task due date |
| assignee_id | BIGINT | Foreign key to Users table |
| created_date | TIMESTAMP | When the task was created |
| last_modified_date | TIMESTAMP | When the task was last modified |

## Security Features

The application implements several security features:

### Authentication

- JWT (JSON Web Token) based authentication
- Password encryption using BCrypt
- Token validation on every protected API request

### Authorization

- Role-based access control
- Method-level security using Spring Security annotations
- Frontend route guards to prevent unauthorized access

### Secure Practices

- CORS configuration to restrict cross-origin requests
- Input validation on both frontend and backend
- Protection against common web vulnerabilities

## Development Guidelines

### Backend Coding Standards

- Use Kotlin coding conventions
- Follow RESTful API design principles
- Write unit tests for all business logic
- Document API endpoints with OpenAPI annotations

### Frontend Coding Standards

- Follow Angular style guide
- Use reactive patterns with RxJS
- Implement proper error handling in services
- Make components modular and reusable

### Version Control Guidelines

- Use feature branches for new features
- Write descriptive commit messages
- Perform code reviews before merging
- Tag releases with semantic versioning

## Troubleshooting

### Common Issues and Solutions

#### Database Connection Issues

**Symptom**: Backend fails to start with database connection errors.

**Solution**:
- Check PostgreSQL container status: `docker ps | grep postgres`
- Verify database credentials in application configuration
- Ensure PostgreSQL port (5432) is not being used by another service

#### Authentication Problems

**Symptom**: Unable to log in or getting 401 Unauthorized responses.

**Solution**:
- Verify correct credentials are being used
- Check if JWT token is properly included in the request header
- Ensure the token has not expired
- Verify you have proper permissions for the requested resource

#### Frontend Build Issues

**Symptom**: Angular build fails with errors.

**Solution**:
- Clear npm cache: `npm cache clean --force`
- Delete node_modules and reinstall: `rm -rf node_modules && npm install`
- Update Angular CLI: `npm update @angular/cli`
- Check for TypeScript errors in your code

#### Docker Compose Issues

**Symptom**: Docker Compose fails to start the services.

**Solution**:
- Stop all containers: `docker-compose down`
- Remove existing containers: `docker-compose rm -f`
- Rebuild all containers: `docker-compose build --no-cache`
- Start services again: `docker-compose up -d`

#### Frontend API Connection Issues

**Symptom**: Frontend cannot connect to backend API.

**Solution**:
- Verify the proxy configuration in frontend/proxy.conf.json
- Check that the backend service is running and accessible
- Look for CORS-related errors in browser console
- Make sure the API URL in ApiConfiguration is set correctly

### Logging

- Backend logs can be accessed using: `docker logs backend`
- Frontend logs can be accessed using: `docker logs frontend`
- Database logs can be accessed using: `docker logs postgres-teletic`

### Getting Help

If you encounter issues not covered in this documentation, please:
1. Check the issue tracker on GitHub
2. Search for similar problems in the documentation
3. Contact the development team at support@teletic.com
