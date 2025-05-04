-- Create databases
CREATE DATABASE devdb;
CREATE DATABASE proddb;

-- Create development user
CREATE USER devuser WITH PASSWORD 'devpassword';
ALTER USER devuser WITH SUPERUSER;
GRANT ALL PRIVILEGES ON DATABASE devdb TO devuser;
GRANT ALL PRIVILEGES ON DATABASE proddb TO devuser;
