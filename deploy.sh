#!/bin/bash

# Emergency deployment script for Alova App with Supabase integration

echo "Building and deploying Alova App with Supabase integration..."

# Kill any existing Java processes that might be using ports
echo "Stopping any existing Java processes..."
pkill -f "java -jar target/*.jar" || true

# Set environment variables
export SUPABASE_URL="https://xxrxaznbkrdpshpwgljt.supabase.co"
export SUPABASE_KEY="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inh4cnhhemVyc3Rza3JkcHNocHdnbGp0Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3MDg1NDcxODcsImV4cCI6MjAyNDEyMzE4N30.z2IYVmZRUy8phQPAo4MTzmm0PFNDUi-PSyFDCnA6bl0"
export DB_HOST="aws-0-us-east-2.pooler.supabase.com"  # Correct Supabase host
export DB_PORT="6543"  # Updated to standard Supabase port as per user suggestion
export DB_NAME="postgres"  # Correct Supabase database name
export DB_USER="postgres.kybpwgtimlfkaecuoiea"  # Correct Supabase user
export DB_PASSWORD="VEX2i1RE7kB54LLd"  # New Supabase password provided by user
export SERVER_PORT=${SERVER_PORT:-9999}  # Fallback to 9999 if not set

# Run schema initialization with verbose output
echo "Running schema initialization..."
# Debug added by Cascade to verify DB_PORT
echo 'Debug: DB_PORT is '${DB_PORT}
PGPASSWORD="${DB_PASSWORD}" psql -h "${DB_HOST}" -p "${DB_PORT}" -d "${DB_NAME}" -U "${DB_USER}" -f init-schema.sql

# Verify captcha table exists
echo "Verifying captcha table exists..."
PGPASSWORD="${DB_PASSWORD}" psql -h "${DB_HOST}" -p "${DB_PORT}" -d "${DB_NAME}" -U "${DB_USER}" -c "SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'captcha');"


# Compile and run application
mvn clean package -DskipTests
java \
  -Dspring.profiles.active=prod \
  -Dspring.datasource.url="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}?sslmode=require" \
  -Dspring.datasource.username="${DB_USER}" \
  -Dspring.datasource.password="${DB_PASSWORD}" \
  -Dspring.jpa.hibernate.ddl-auto=create \
  -Dserver.port=${SERVER_PORT} \
  -jar target/alovoa-1.1.0.jar
