# TrustWipe

TrustWipe is a secure data wiping system based on NIST 800-88 standards.

## Features
- Full and Partial wipe
- File/folder selection
- Real-time progress tracking
- PDF Certificate of Destruction
- MongoDB integration

## Tech Stack
- Backend: Spring Boot
- Frontend: React (Vite + Tailwind)
- Database: MongoDB

## Setup

### Backend
cd trustwipe-backend
mvn spring-boot:run

### Frontend
cd trustwipe-frontend
npm install
npm run dev

### MongoDB
Make sure MongoDB is running locally on:
mongodb://localhost:27017