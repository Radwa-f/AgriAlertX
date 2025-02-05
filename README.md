# 🌱🌦️ AgriAlertX: Agricultural Disaster Prevention and Alert System                                                            

<div align="center">
  <picture>
    <source srcset="https://github.com/user-attachments/assets/911c92e3-e9f8-4aaa-85ee-3f0a548ea628" media="(prefers-color-scheme: dark)">
    <img src="https://github.com/user-attachments/assets/c2c68b06-0e01-4f55-b577-07cb76639166" width="300" alt="AgriAlertX Logo">
  </picture>
</div>

This platform enables real-time agricultural risk monitoring based on weather forecasts, providing farmers with timely alerts and actionable recommendations to protect their crops from adverse weather conditions. Using open-source weather APIs and crop-specific threshold analysis, X helps prevent agricultural losses through proactive notifications.

## Table of Contents

- [Software Architecture](#software-architecture)
- [Docker Image](#docker-image)
- [Frontend](#frontend)
- [Backend](#backend)
- [Getting Started](#getting-started)
- [Video Demonstration](#video-demonstration)
- [Contributing](#contributing)
- [Improvements](#improvements)

## Software architecture
![AgriAlertX Architecture](https://github.com/user-attachments/assets/b74be9fa-6277-4346-bedc-f4f791ec4467)                    
  
The application architecture uses Next.js for the web frontend, Kotlin/Swift for mobile clients, Spring Boot for the backend, and Flask for the chatbot service, with communication via RESTful APIs.

## Docker Image
```sh
services:
  mysql:
    image: mysql:8.0
    container_name: mysql-container
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: agrialert
    ports:
      - "3307:3306"
    networks:
      - agrialert-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  flask-api:
    build:
      context: ./flask_backend
      dockerfile: Dockerfile
    container_name: agrialert-flask-api
    ports:
      - "8086:8086"
    depends_on:
      mysql:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://localhost:3306/agrialert
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: password
    networks:
      - agrialert-network

  spring-boot-api:
    build:
      context: ./springboot_backend
      dockerfile: Dockerfile
    container_name: agrialert-spring-boot-api
    ports:
      - "8087:8087"
    depends_on:
      mysql:
        condition: service_healthy
    networks:
      - agrialert-network

networks:
  agrialert-network:
    driver: bridge
```

## Frontend

### Technologies Used
- Next.js (Web)
- Kotlin (Android)
- Swift (iOS)
- Tailwind CSS
- TypeScript

## Backend

### Technologies Used
- Spring Boot
- MySQL
- Flask (Chatbot)

## Backend Project Structure

The backend code follows a modular and organized structure, leveraging the power of Spring Boot for building a robust and scalable application.

### 1. com.example.demo
- *Main Application Class:* DemoApplication.java serves as the entry point for the Spring Boot application. It includes the main method to bootstrap and start the application.

### 2. com.example.demo.controller
- *Controller Layer:* This package contains classes responsible for handling incoming HTTP requests. Each controller defines RESTful endpoints for specific features or entities and delegates the request processing to the service layer.

### 3. com.example.demo.model
- *Entity Layer:* The model package includes classes representing data entities in the application. These classes use JPA annotations to define the structure of the corresponding database tables, ensuring seamless ORM mapping.

### 4. com.example.demo.repository
- *Repository Layer:* This package contains interfaces extending Spring Data JPA repository interfaces. These provide built-in methods for CRUD operations and enable interaction with the database without requiring boilerplate code.

### 5. com.example.demo.security
- *Security Configuration:* The security package includes classes for configuring authentication and authorization mechanisms. This might involve defining roles, managing user credentials, and securing endpoints based on roles or permissions.

### 6. com.example.demo.service
- *Service Layer:* This package contains business logic for the application. Services interact with the repository layer to fetch or modify data and provide processed information to the controller layer.

### 7. com.example.demo.utils
- *Utility Classes:* The utils package includes helper or utility classes that provide commonly used functionality across the application. These might include functions for validation, formatting, or other reusable logic.

### Additional Files and Directories:
- **resources Folder:** This contains application configuration files like application.properties or application.yml, along with other static resources.
- **Dockerfile:** Defines the steps to containerize the Spring Boot application for deployment.
- **pom.xml:** The Maven configuration file, managing dependencies and build configurations for the project.
- **.gitignore:** Specifies files and directories to be excluded from version control.

This structured organization ensures the application is scalable, maintainable, and adheres to the separation of concerns principle.                                        
  
  

### Dependencies

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <scope>runtime</scope>
</dependency>
// Other dependencies are in the pom.xml
```

## Getting Started
Here are step-by-step instructions to set up and run AgriAlertX locally:

### Prerequisites:

1. **Git:**
   - Ensure you have Git installed. If not, download and install it from [git-scm.com](https://git-scm.com/).

2. **MySQL:**
   - Install MySQL Server.
   - Create a database named `agrialert`.
   - Ensure MySQL is running on port 3306.

3. **Node.js:**
   - Install Node.js (LTS version) from [nodejs.org](https://nodejs.org/).


### Backend Setup:

1. **Clone the Project:**
   - Clone the repository by running the following command:
     ```bash
     git clone https://github.com/Radwa-f/AgriAlertX.git
     cd AgriAlertX/Springboot_Backend
     ```

2. **Install Backend Dependencies:**
   - Open a terminal in the backend project folder.
   - Run the following command to install dependencies:
     ```bash
     mvn clean install
     ```

3. **Configure Application Properties:**
   - Update the `application.properties` file with your MySQL credentials.
   - Configure the OpenMeteo API key if required.

4. **Run Backend:**
   - Start your XAMPP Apache and MySQL servers.
   - Run the Spring Boot application. The database and entities will be created automatically.
   - Verify that the backend is running at [http://localhost:8087](http://localhost:8087).
       

### Web Frontend Setup:

1. **Install Dependencies:**
   - Navigate to the `Agrialert_Web` directory:
     ```bash
     cd web-client
     ```
   - Install the necessary dependencies by running:
     ```bash
     npm install
     ```

2. **Run Frontend:**
   - Start the development server by running:
     ```bash
     npm run dev
     ```

3. **Access the Web Interface:**
   - Open your browser and navigate to [http://localhost:3000](http://localhost:3000) to access the web interface.


### Mobile Frontend Setup:

1. **Android:**
   - Open the Android project in **Android Studio**.
   - Update the API endpoint in the configuration files.
   - Build and run the application on your device or emulator.

2. **iOS:**
   - Open the iOS project in **Xcode**.
   - Update the API endpoint in the configuration files.
   - Build and run the application on your device or simulator.



### Chatbot Setup:

1. **Install Python Dependencies:**
   - Navigate to the `Flask_Backend` directory:
     ```bash
     cd chatbot
     ```
   - Install the required Python dependencies by running:
     ```bash
     pip install -r requirements.txt
     ```

2. **Run Chatbot Server:**
   - Start the chatbot server by running:
     ```bash
     python chat.py
     ```

3. **Access Chatbot Service:**
   - The chatbot service will be available at [http://localhost:5000](http://localhost:5000).
                                       

## Video Demonstration
Here are the illustrative videos of our apps, android mobile app, ios mobile app and the web app:

<div align="center">

[See The IOSVideo](https://github.com/user-attachments/assets/57b1bfc7-6ed4-4a97-8701-c1206a48b931)</br>
[See The Android Video](https://github.com/user-attachments/assets/9bf06c0f-d11e-4906-8102-ea8581ed7989)</br>
[See The WEB Video](https://github.com/user-attachments/assets/2c856a28-3a5f-4f9f-bc76-6da66e5381ac)


</div>

# Contributing
 
We welcome contributions from everyone, and we appreciate your help to make this project even better! If you would like to contribute, please follow these guidelines:

## Contributors
- Fattouhi Radwa ([GitHub](https://github.com/Radwa-f))
- Douidy Sifeddine ([GitHub](https://github.com/SaifeddineDouidy))                    
- Mohamed Lachgar ([Researchgate](https://www.researchgate.net/profile/Mohamed-Lachgar)) 


### Improvements:

- Ensure clean, maintainable, and scalable code through automated reviews.
- Code Quality Maintenance.
- Integrate IoT sensors for more accurate data.
- AI-Based Enhancements.
- Enhance architecture for larger datasets and dynamic alerting systems.
