# QR Code Login with Keycloak Integration

## Overview

This project implements a QR Code login system using Java, Maven, Keycloak for access management, Docker for containerization, and utilizes both socket communication and API endpoints.

## Prerequisites

Before you begin, ensure you have the following tools and dependencies installed:

- Java Development Kit (JDK)
- Apache Maven
- Docker
- Keycloak (for setting up authentication and authorization)
- Your preferred IDE (IntelliJ, Eclipse, etc.)

## Setup

1. **Clone the repository:**

   ```bash
   git clone https://github.com/msmsadegh/SPI-Keycloak-QR-Example
   cd SPI-Keycloak-QR-Example
   ```

2. **Configure Keycloak:**

    - Set up a Keycloak realm and client for your application.
    - Update the `application.properties` file with your Keycloak configuration.

3. **Build the project:**

   ```bash
   mvn clean install
   ```

4. **Run the Docker container:**

   ```bash
   docker build -t your-image-name .
   docker run -p 8080:8080 your-image-name
   ```

   Replace `your-image-name` with a suitable name for your Docker image.

5. **Access the application:**

   Open your web browser and navigate to `http://localhost:8080` to interact with the QR Code login system.

## Project Structure

- **`src/main/java/com.example.qrcodelogin`**: Contains the main Java source code.
- **`src/main/resources`**: Configuration files, including `application.properties` for Keycloak settings.
- **`Dockerfile`**: Instructions for Docker to build the container.

## Usage

Provide instructions on how users can use your application, including how to generate and scan QR codes for login.

## API Endpoints

Document the API endpoints used in your project.

## Contributing

If you'd like to contribute to this project, please follow these guidelines [CONTRIBUTING.md](link-to-contributing-guidelines).

## License

This project is licensed under the [MIT License](LICENSE.md).

## Acknowledgments

- Give credit to the libraries, tools, or individuals that helped or inspired your project.
- Gratitude to [Dr. Ali Haghighat](#) for valuable insights and guidance throughout the development.
- Special thanks to [Mr. Ghobad Emadi](#) for the original idea and inspiration behind this project.
- Thanks to [Mr. Mohammad Ali Mirzaei](#) for his support and contributions to the project.
- Shoutout to [Mr. Alireza Rahati](#) for his assistance and collaboration.
