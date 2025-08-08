FROM openjdk:17-jdk-slim

WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -DskipTests

# Create directories for documents and certificates
RUN mkdir -p /app/documents /app/certificates

# Expose port
EXPOSE 8443

# Run application
CMD ["java", "-jar", "target/electronic-signature-service-1.0.0.jar"]
