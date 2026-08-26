FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN ./mvm clean package -Dskiptests

RUN cp target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]