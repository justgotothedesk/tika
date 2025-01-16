FROM openjdk:17-jdk-slim

WORKDIR /app

RUN echo $(date) > /app/last_build
COPY build/libs/tika-0.0.1-SNAPSHOT.jar app.jar

RUN apt-get update && apt-get install -y \
    tesseract-ocr \
    tesseract-ocr-kor && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

ENTRYPOINT ["java", "-jar", "app.jar"]
