# eTutor Task-Administration

This application provides a REST-interface for the eTutor task administration.

## Development

In development environment, the API documentation is available at http://localhost:8080/docs.

See [CONTRIBUTING.md](https://github.com/eTutor-plus-plus/task-administration/blob/main/CONTRIBUTING.md) for details.

## Docker

Start a new instance of the application using Docker:

```bash
docker run -p 8080:8080 \ 
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://postgres:5432/etutor" \
  -e SPRING_DATASOURCE_USERNAME=etutor \
  -e SPRING_DATASOURCE_PASSWORD=myPwd \
  -e SPRING_FLYWAY_USER=etutor_admin \
  -e SPRING_FLYWAY_PASSWORD=adPwd \
  -e SPRING_MAIL_SENDER=etutor@dke.uni-linz.ac.at \
  -e SPRING_MAIL_HOST=smtp.uni-linz.ac.at \
  -e SPRING_MAIL_PORT=587 \
  -e SPRING_MAIL_USERNAME=etutor \
  -e SPRING_MAIL_PASSWORD=myPwd \
  etutorplusplus/task-administration
```

or with Docker Compose:

```yaml
version: '3.8'

services:
    task-administration:
        image: etutorplusplus/task-administration
        restart: unless-stopped
        ports:
            -   target: 8080
                published: 8080
        environment:
            SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/etutor
            SPRING_DATASOURCE_USERNAME: etutor
            SPRING_DATASOURCE_PASSWORD: myPwd
            SPRING_FLYWAY_USER: etutor_admin
            SPRING_FLYWAY_PASSWORD: adPwd
            SPRING_MAIL_SENDER: etutor@dke.uni-linz.ac.at
            SPRING_MAIL_HOST: smtp.uni-linz.ac.at
            SPRING_MAIL_PORT: 587
            SPRING_MAIL_USERNAME: etutor
            SPRING_MAIL_PASSWORD: myPwd
```

### Environment Variables

In production environment, the application requires two database users:

* A database administrator user which has the permission to create the tables.
* A JPA user which has read/write access (`SELECT, INSERT, UPDATE, DELETE, TRUNCATE`) to the database tables (see `./docker/create_user.sh` for example user creation statement).

> In development environment, one user will be used for both.

The users must be configured via environment variables. The clients have to be configured via environment variables as well (`X`/`Y` stands for a 0-based index).

| Variable                     | Description                                        |
|------------------------------|----------------------------------------------------|
| `SERVER_PORT`                | The server port.                                   |
| `SPRING_DATASOURCE_URL`      | JDBC-URL to the database                           |
| `SPRING_DATASOURCE_USERNAME` | The username of the JPA user.                      |
| `SPRING_DATASOURCE_PASSWORD` | The password of the JPA user.                      |
| `SPRING_FLYWAY_USER`         | The username of the database administrator user.   |
| `SPRING_FLYWAY_PASSWORD`     | The password of the database administrator user.   |
| `SPRING_MAIL_SENDER`         | The email address that should be used as "sender". |
| `SPRING_MAIL_HOST`           | The host/ip-address of the SMTP server.            |
| `SPRING_MAIL_PORT`           | The port of the SMTP server.                       |
| `SPRING_MAIL_USERNAME`       | The username of the SMTP server.                   |
| `SPRING_MAIL_PASSWORD`       | The password of the SMTP server.                   |
