# Development

This document describes the development setup of the task administration server.

## Dependencies

The following dependencies are required to run the server:

* Java JDK >= 21
* Docker and Docker Compose
* Maven
* Node.js >= 20 (only for creating new releases)
* You can use any Java IDE of your choice.

For more details on style-guides, versioning and release process see [CONTRIBUTING.md](../CONTRIBUTING.md) in the Task-Administration repository. 
For more information on configuration variables, see [README.md](../README.md).

## Setup

To setup the project for local development, follow the steps detailed in the following Listing. 
The procedure assumes you have installed the development dependencies listed above.

```shell
# Clone repository
git clone git@github.com:eTutor-plus-plus/task-administration.git
# or
git clone https://github.com/eTutor-plus-plus/task-administration.git
	
# Enter task-administration directory
cd task-administration
	
# Switch to develop-branch (NEVER make changes directly to the main-branch)
git checkout develop
	
# Optional: Create feature branch
git checkout -b feature/my-new-feature

# Ensure your Docker Engine is running (should not print an error)
docker info

# Open the Project in IDE or manually run application with:
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
After executing above steps, the application is available at http://localhost:8080/. At application startup a default user with username 
"admin" and password "secret" will be created if no users exists. To test the API, the API documentation is available at 
http://localhost:8080/docs. On the API documentation page click _Authorize_ and enter username and password (leave the default values for all other fields).

The application database is automatically created as Docker container. By default, the PostgreSQL database is configured to listen on port 5433. 
Additionally, a Docker container with a fake Email-server gets deployed. The frontend of the mail server is available at http://localhost:8025/ and allows 
to view the Emails sent by the task-administration. By default, the SMTP server listens on port 1025.
