Bank Emulation
This application is a REST API service with a web interface for managing bank accounts, transactions, and users.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-green)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue)
![Security](https://img.shields.io/badge/Security-JWT%20%2B%20OAuth2-red)

## 📋 Features

* **Authorization and Authentication:**
    * Registration and Login via Email/Password.
    * Social Login via **Google OAuth2**.
    * Route protection using **JWT (JSON Web Token)**.
    * Role-based access control (`CLIENT`, `ADMIN`).
* **Banking Operations:**
    * Account creation and viewing.
    * Transfers between accounts.
    * Transaction history.
* **Notifications:** Email notifications (via Mailtrap/SMTP).
* **Documentation:** Built-in Swagger UI.

## 🛠 Tech Stack

* **Core:** Java 17, Spring Boot 3.4.1
* **Database:** PostgresSQL, Hibernate (JPA)
* **Security:** Spring Security 6, OAuth2 Client, JWT 
* **Frontend:** Thymeleaf, HTML5, CSS3, JavaScript
* **Tools:** Docker, Maven
* **Documentation:** SpringDoc OpenAPI (Swagger)
* **Unit Tests:** Mockito, JUnit5
* **Integrations Tests:** TestContainers

  👤 Author: Postoienko Oleksandr