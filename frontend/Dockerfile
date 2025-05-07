# Use Node.js as base image
FROM node:18-alpine

# Set working directory
WORKDIR /app

# Copy package.json and package-lock.json
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy project files
COPY . .

# Expose port 4200 (Angular default dev server port)
EXPOSE 4200

# Start the Angular development server, binding to all network interfaces
CMD ["npm", "start", "--", "--host", "0.0.0.0", "--disable-host-check"]
