# Build stage for Java application
FROM maven:3.8.6-eclipse-temurin-17 AS build
WORKDIR /build
COPY . .
RUN mvn clean install -DskipTests

# Runtime stage with Python and Java
FROM eclipse-temurin:17-jdk-jammy AS runtime

# Install Python and system dependencies for Whisper
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    python3 \
    python3-pip \
    python3-dev \
    ffmpeg \
    libsm6 \
    libxext6 \
    git && \
    rm -rf /var/lib/apt/lists/*

	RUN ln -s /usr/bin/python3 /usr/bin/python

# Install Whisper with pip
RUN pip3 install --no-cache-dir --upgrade pip && \
    pip3 install --no-cache-dir torch torchaudio && \
    pip3 install --no-cache-dir openai-whisper

WORKDIR /app
COPY --from=build /build/target/audiotranscriber-0.0.1-SNAPSHOT.jar app.jar
COPY transcribe.py .

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
