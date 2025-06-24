# Task Manager API

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)](https://redis.io/)
[![Java](https://img.shields.io/badge/Java_17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![Swagger](https://img.shields.io/badge/-Swagger-%23Clojure?style=for-the-badge&logo=swagger&logoColor=white)](https://swagger.io/)


## About the Project

This application is designed for  task management processes. Users can log in with different roles to create projects , assign tasks, and track progress.

## Technologies Used

- **Java 17**
- **Spring Boot**
- **Spring Security**
- **Redis**
- **PostgreSQL**
- **Docker**
- **OpenAPI**

## Prerequisites

-  **Java 17, Maven, Redis, PostgreSQL or Docker**

### Docker Run

- Please follow the below directions in order to build and run the application with Docker Compose;

```sh

$ git clone https://github.com/Bino26/task_manager.git
$ cd task_manager
$ make build
``` 

- **You can reach the swagger-ui via**  `http://localhost:8080/swagger-ui/index.html`
```sh
  Login with :
{
  "email": "admin@task.com",
  "password": "password1234"
}
  for being able to access all endpoints
```
- You can use a GUI tool like [RedisInsight](https://redis.io/download/) to view and manage your cached Redis data.

### Commands from Makefile 

| Command         | Description                                 |
|----------------|---------------------------------------------|
| `make build`   | Build app image with `-DskipTests`          |
| `make up`      | Start app and dependencies                  |
| `make down`    | Stop and remove all running containers      |
| `make logs`    | View live logs from containers              |
| `make test`    | Run tests via `Dockerfile.test` + Testcontainers |
| `make clean`   | Clean up all unused Docker resources        |



### 🙏 Kindly Note
Due to time constraints, the Postman collection may be missing or incomplete.  
We kindly invite you to use the integrated **Swagger Documentation** to explore and interact with the API.

Thank you for your understanding and patience.


## 🔧 Environment Configuration

All necessary environment variables are already registered directly in the `docker-compose.yml` file.  
This setup is designed to help reviewers launch the project effortlessly without manual configuration.

### ⚠️ Deployment Note

If you plan to deploy this project in a production environment, **do not use environment variables directly inside `docker-compose.yml`**.  
Instead, follow one of these best practices:

- Use a `.env` file and reference variables using `${VARIABLE_NAME}` syntax.
- Configure environment variables directly in your deployment environment (e.g., CI/CD, cloud provider, or secrets manager).
- Use tools like [Docker secrets](https://docs.docker.com/engine/swarm/secrets/) or Spring Boot config servers to manage sensitive information securely.

**🛡️ Keep credentials and sensitive values out of source control.**

### Contribute
Feel free to open a Pull Request or an issue !