# Library-Management-System
A Java Swing-based Library Management System integrated with MySQL using JDBC. Features role-based login (Admin/User), book management, issue/return functionality, and a clean GUI interface.


## Features
* User login authentication
* Add, update, view, and delete books
* MySQL database connectivity via JDBC
* Simple and interactive Swing GUI


## 🛠 Tech Stack

* **Java (JDK 8+)**
* **Java Swing**
* **MySQL**
* **JDBC**


## Project Structure

Library-Management-System/
├── DBConnection.java
├── LoginFrame.java
├── LibraryManagement.java
├── Main.java
├── mysql-connector-j-8.3.0.jar
└── README.md


## Setup
1. Install **Java JDK** and **MySQL**
2. Create a MySQL database and required tables (`users`, `books`)
3. Update DB credentials in `DBConnection.java`
4. Add MySQL connector JAR to classpath
5. Run `Main.java`


## Future Scop
* Password encryption
* Search & filter
* Book issue/return module
