# Task Manager API

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
$ docker-compose up
``` 

- **You can reach the swagger-ui via**  `http://localhost:8080/swagger-ui/index.html`
```sh
  Login with :
'admin@task.com' and 'password1234' 
  for being able to access all endpoints
```
- You can use a GUI tool like [RedisInsight](https://redis.io/download/) to view and manage your cached Redis data.

### Contribute
Feel free to open a Pull Request or an issue !

### ⚠️ Warning
Due to a missed deadline, **the Postman collection may be missing** or **incomplete**.
Please **Be Kind** and use Swagger Docs to interact with the API.