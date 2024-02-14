# Project Structure

The following table gives a high-level overview of the project structure.

| Directory/File       | Description                                                                                              |
|----------------------|----------------------------------------------------------------------------------------------------------|
| _.git-hooks_         | Contains Git-Hooks to ensure commit messages follow the Conventional Commit convention.                  |
| _.github_            | Contains configuration of GitHub Actions and Issue Templates                                             |
| _documentation_      | Contains the documentation of the project                                                                |
| _src/main/java_      | Source code of the application                                                                           |
| _src/main/resources_ | Resources such as configuration files and database migrations scripts                                    |
| _src/test/java_      | Java test classes                                                                                        |
| _src/test/resources_ | Resources for Java test classes                                                                          |
| _Dockerfile_         | Dockerfile used to create a Docker-Image for the application                                             |
| _compose.yml_        | Docker-compose configuration with Docker containers required for development (Database, Fake-Mailserver) |
| _pom.xml_            | Maven project configuration                                                                              |
| _README.md_          | Main project information file                                                                            |
| _CONTRIBUTING.md_    | Information for contributors                                                                             |
| _CHANGELOG.md_       | Automatically generated changelog. DO NOT MODIFY manually.                                               |

The task administration server uses the Spring Boot Framework. The application consists of the packages described in the following table.

| Directory/File | Description                                                                                                                                             |
|----------------|---------------------------------------------------------------------------------------------------------------------------------------------------------|
| _auth_         | Contains classes for User Authentication. A custom OpenID/OAuth2-like authentication mechanism using Json Web Tokens (JWT) is used in this application. |
| _config_       | Contains the configuration classes for the application.                                                                                                 |
| _controllers_  | Contains the RESTful web service controllers.                                                                                                           |
| _data_         | Contains the JPA entities and the repositories for database access.                                                                                     |
| _dto_          | Contains the data transfer objects.                                                                                                                     |
| _services_     | Contains the services encapsulating the business logic. The services are called by the controllers.                                                     |
