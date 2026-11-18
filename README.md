# Library-Servlet-Thymeleaf

A server-rendered web application for managing a book library, built with Java Servlets and Thymeleaf. A single front controller servlet receives every HTTP request and dispatches it to a dedicated command (controller), which works with a JPA-based DAO layer on top of MySQL and renders the result through Thymeleaf templates. The application is packaged as a WAR and runs on Jetty (`mvn jetty:run`).

## About the Project

Library-Servlet-Thymeleaf is the web version of the library application developed in the previous labs. It reuses the DAO layer built with Jakarta Persistence API (EclipseLink, Criteria API and Metamodel API) and puts a web interface on top of it.

The application allows users to:

- find books by author;
- find free copies of a book by its title;
- issue a book copy to a reader;
- write off a book copy;
- list readers who are in debt (a book not returned for more than a month);
- see how many times they have visited the site and when the previous visit was.

## Main Features

### Front Controller

`FrontController` is the only application servlet. It is mapped to `/` in `web.xml`, so it receives all requests except static files (`/static/*`, served by the container's default servlet). For each request it:

1. creates (or restores) the user session and updates the visit cookies;
2. analyzes the URL and builds a route key such as `GET /books` or `POST /issue`;
3. finds the command registered for this key;
4. passes control to the command and receives a `ViewResult`;
5. renders a Thymeleaf template or sends a redirect.

### Commands (Controllers)

Every user operation is a separate class implementing the `Command` interface. A command reads data from the request, calls the DAO and returns a `ViewResult` (a template name with a model, or a redirect).

| Route | Command | Operation |
|---|---|---|
| `GET /` | `HomeCommand` | main page with system information and menu |
| `GET /books` | `FindBooksCommand` | books by author |
| `GET /copies` | `FreeCopiesCommand` | free copies of a book |
| `GET /issue` | `IssueFormCommand` | issue form |
| `POST /issue` | `IssueCommand` | issue a copy to a reader |
| `GET /writeoff` | `WriteOffFormCommand` | write-off form |
| `POST /writeoff` | `WriteOffCommand` | write off a copy |
| `GET /debtors` | `DebtorsCommand` | readers in debt |
| `GET /error` | `ErrorCommand` | error page for container-level errors |

GET commands only read data. POST commands change the database and redirect (Post/Redirect/Get), so refreshing a page never repeats an operation.

### Thymeleaf Templates and CSS

Pages are generated on the server with Thymeleaf from templates in `src/main/resources/templates`. Common parts (head, navigation, messages, footer) are fragments in `fragments.html`. Styles are in a separate file, `src/main/webapp/static/css/style.css` (orange and black palette, Playfair Display and Montserrat fonts).

`index.html` describes the system and contains the menu with links to all operations. `error.html` is the page shown on errors.

### Sessions and Cookies

Every user gets an `HttpSession` that stores the visit number, the previous visit time, the session start and the number of requests. Two cookies live for one year:

- `visitCount` - the number of visits;
- `lastVisit` - the date and time of the last request.

One-time messages ("Book issued", "Copy is not available") are passed through the session (`Flash`) and survive the redirect.

### Error Handling

- DAO classes wrap JPA errors into `LibraryException`. If the cause is a connection problem (database stopped, wrong credentials, unknown database), the code is `DB_UNAVAILABLE`.
- `FrontController` catches every exception, logs it and shows `error.html` with a short message. The user never sees stack traces or SQL text. A database outage gives HTTP 503 and the message "Не удалось подключиться к базе данных. Попробуйте повторить попытку позже."
- Container-level errors (404 from static files, uncaught exceptions) are redirected by `web.xml` to the `/error` route, which renders the same page.
- If the database is down the application still starts, and the failure is logged. After the database comes back, the next request reconnects automatically.

### Logging

Logging uses SLF4J with Log4j2 (`src/main/resources/log4j2.xml`). Messages go to the console and to `logs/library.log` (rolling by day and size). The log records:

- application start and stop (`AppContextListener`);
- every user request (`FrontController`);
- errors and exceptions with stack traces;
- database operations (every DAO method) and SQL statements of EclipseLink, which are routed to Log4j2 through the `jul-to-slf4j` bridge.

### DAO Layer

The DAO layer comes from the previous lab: Criteria API with the static Metamodel, application-managed transactions, pessimistic locking when issuing a copy. `JpaUtil` keeps one `EntityManagerFactory` and gives DAO classes an `EntityManager`. `LookupDao` supplies the lists for the drop-down menus.

## Project Architecture

```
Browser
    ↓
FrontController (servlet, "/")
    ├── VisitTracker (session + cookies)
    ├── Command (one class per operation)
    │       ↓
    │   DAO (BookDao, BookCopyDao, LoanDao, ReaderDao, LookupDao)
    │       ↓
    │   EntityManager / Criteria API → EclipseLink → MySQL
    └── TemplateRenderer (Thymeleaf) → HTML page
```

## Data Model

| Entity | Table | Description |
|---|---|---|
| `Author` | `author` | book author |
| `Book` | `book` | book (title, publisher, publish year) |
| `BookCopy` | `book_copy` | physical copy (inventory number, written-off flag) |
| `Reader` | `reader` | library reader (name, phone, address) |
| `Librarian` | `librarian` | librarian who issues books |
| `Loan` | `loan` | loan (copy, reader, librarian, issue date, return date) |

## Project Structure

```
Lab5_WEB/
├── pom.xml
└── src/main/
    ├── java/com/example/lab5_web/
    │   ├── library/
    │   │   ├── JpaUtil.java
    │   │   ├── dao/        (BookDao, BookCopyDao, LoanDao, ReaderDao, LookupDao)
    │   │   ├── entity/     (Author, Book, BookCopy, Librarian, Loan, Reader)
    │   │   └── exception/  (LibraryException)
    │   └── web/
    │       ├── AppContextListener.java
    │       ├── FrontController.java
    │       ├── TemplateRenderer.java
    │       ├── VisitTracker.java
    │       ├── Flash.java
    │       └── command/    (Command, ViewResult, Params, *Command)
    ├── resources/
    │   ├── META-INF/persistence.xml
    │   ├── log4j2.xml
    │   └── templates/      (fragments, index, books, copies, issue, writeoff, debtors, error)
    └── webapp/
        ├── WEB-INF/web.xml
        └── static/         (css/style.css, library.jpg)
```

## Technology Stack

- Java 17+
- Jakarta Servlet 5.0, Jetty 11 (Jetty Maven Plugin)
- Thymeleaf 3.1
- Jakarta Persistence API 3.2, EclipseLink 5.0.2 (provider and Metamodel Generator)
- MySQL and MySQL Connector/J
- SLF4J and Log4j2
- Maven (WAR packaging)
- HTML5 and CSS3

## Database Setup

Create a MySQL database named `library` with the tables `author`, `book`, `book_author`, `book_copy`, `librarian`, `reader` and `loan`, and a user that has access to it. The schema is not generated by JPA.

## Configuration

Database access is configured in `src/main/resources/META-INF/persistence.xml`:

```xml
<property name="jakarta.persistence.jdbc.url"
          value="jdbc:mysql://localhost:3306/library?characterEncoding=utf8&amp;serverTimezone=UTC"/>
<property name="jakarta.persistence.jdbc.user" value="YOUR_USER"/>
<property name="jakarta.persistence.jdbc.password" value="YOUR_PASSWORD"/>
```

Replace `YOUR_USER` and `YOUR_PASSWORD` with your own credentials and do not commit real passwords.

## Running the Application

No separate servlet container is needed. From the project root:

```bash
mvn jetty:run
```

Open the site at:

```
http://localhost:8080/library/
```

To build a WAR file for deployment to any servlet container:

```bash
mvn clean package
```

The result is `target/library.war`. The application writes its log to `logs/library.log`.

## Technologies Demonstrated

- front controller and command design patterns;
- Java Servlets, `web.xml`, listeners and error pages;
- server-side templating with Thymeleaf and fragments;
- HTTP sessions and cookies;
- Post/Redirect/Get and one-time flash messages;
- JPA, Criteria API and Metamodel API in the DAO layer;
- centralized error handling with a user-friendly error page;
- logging with SLF4J and Log4j2.

## Project Goal

The main goal of the project is to demonstrate how to design a web component with a servlet as the central dispatcher: one entry point that analyzes the request URL, selects a controller for each user operation, gets data through the DAO layer and generates dynamic HTML pages with Thymeleaf, while tracking every user with a session and cookies, handling errors gracefully and logging the work of the application.
