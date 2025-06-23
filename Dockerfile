FROM openjdk:17
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} Task-Manager.jar
ENTRYPOINT ["java", "-jar" ,"/Task-Manager.jar"]