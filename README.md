# saltynote-service-kotlin

[![Gradle Test](https://github.com/SaltyNote/saltynote-service-kotlin/actions/workflows/gradle.yml/badge.svg)](https://github.com/SaltyNote/saltynote-service-kotlin/actions/workflows/gradle.yml)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/c8d2d08e89f54d85a852586dd927dc1a)](https://app.codacy.com/gh/SaltyNote/saltynote-service-kotlin/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
![Website](https://img.shields.io/website?label=service&url=https%3A%2F%2Fapi.saltynote.com)

Bring SaltyNote Service into Kotlin

## Overview

This is the backend service for [saltynote](https://saltynote.com). It
uses [JWT](https://auth0.com/docs/tokens/json-web-tokens) for authentication(`access token` & `refresh token`). As high-level, this service provides
APIs for:

1. User (signup, login, token refresh, token cleanup, password reset, account delete)
2. Note (create, update, fetch and delete)

![overview](https://raw.githubusercontent.com/SaltyNote/saltynote-service/master/docs/images/overview.png)


## Get Started

This is a standard spring boot project with Maven, so you can use generic maven command to run it. While the simplest &
quickest way is to run [`./start.sh`](./start.sh).

Swagger UI will be available at http://localhost:8888/swagger-ui.html (Screenshot for User APIs)
![swagger-ui](https://raw.githubusercontent.com/SaltyNote/saltynote-service/master/docs/images/swagger-ui.jpg)

### Prerequisite

1. Java 17 (due to Spring Boot v3)
2. Docker (docker compose) for setting up development dependencies, e.g. database, redis, etc.
3. IDE ([Eclipse](https://www.eclipse.org/) or [Intellij](https://www.jetbrains.com/idea/))

### Configuration

1. The service relies on database to store `user` and `note` information. In development env, you can run `docker compose up`
   to start `mongo(storage)` and `redis(cache)` locally(*add `-d` if you want start it as “detached” mode*).
2. This service also need smtp service to send email(*Note: this is optional now, if not setup, the email payload will
   be logged([code](src/main/java/com/saltynote/service/event/EmailEventListener.java#L50-L55)).*).

## License

saltynote service is licensed under MIT - [LICENSE](./LICENSE)
