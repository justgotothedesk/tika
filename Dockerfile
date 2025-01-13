FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build/libs/*.jar app.jar

RUN apt-get update && apt-get install -y \
    tesseract-ocr \
    tesseract-ocr-kor && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

ENTRYPOINT ["java", "-jar", "app.jar"]
