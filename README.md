# Library-Servlet-Thymeleaf

A server-rendered web application for managing a book library, built with Java Servlets and Thymeleaf. A single front controller servlet receives every HTTP request and dispatches it to a dedicated command (controller), which works with a JPA-based DAO layer on top of MySQL and renders the result through Thymeleaf templates.

## About the Project

Library-Servlet-Thymeleaf is a web version of the library application developed in the previous labs. It reuses the DAO layer built with Jakarta Persistence API (EclipseLink, Criteria API and Metamodel API) and puts a web interface on top of it.

The application allows users to:

- find books by author;
- find free copies of a book by its title;
- issue a book copy to a reader;
- write off a book copy;
- list readers who are in debt (a book not returned for more than a month);
- see how many times they have visited the site and when the previous visit was.

The project uses an embedded Apache Tomcat and does not depend on Spring: the servlet dispatcher, the controllers, the session handling and the template rendering are written by hand.

## Main Features

### Front Controller

`FrontController` is the only servlet of the application. It is mapped to `/`, so it receives all HTTP requests. For each request it:

1. creates (or restores) the user session and updates visit cookies;
2. analyzes the URL and builds a route key such as `GET /books` or `POST /issue`;
3. finds the command registered for this key;
4. passes control to the command and receives a `ViewResult`;
5. either renders a Thymeleaf template or sends a redirect.

Unknown URLs produce a 404 page, unexpected exceptions produce a 500 page.

### Commands (Controllers)

Every user operation is a separate class that implements the `Command` interface. A command reads data from the request, calls the DAO and returns a `ViewResult` with a template name and a model (or a redirect).

| Route | Command | Operation |
|---|---|---|
| `GET /` | `HomeCommand` | main page |
| `GET /books` | `FindBooksCommand` | books by author |
| `GET /copies` | `FreeCopiesCommand` | free copies of a book |
| `GET /issue` | `IssueFormCommand` | issue form with lists of copies, readers, librarians |
| `POST /issue` | `IssueCommand` | issue a copy to a reader |
| `GET /writeoff` | `WriteOffFormCommand` | write-off form |
| `POST /writeoff` | `WriteOffCommand` | write off a copy |
| `GET /debtors` | `DebtorsCommand` | readers in debt |

GET commands only read data and show a page. POST commands change the database and then redirect (Post/Redirect/Get), so refreshing the page never repeats an operation.

### Thymeleaf Templates

Pages are generated on the server with Thymeleaf (without Spring) from templates in `resources/templates`. Common parts (head, navigation, messages, footer with session info) are extracted into fragments and reused with `th:replace`. The styles are kept in one fragment, `styles.html`. The design uses an orange and black palette with Playfair Display and Montserrat fonts.

### Sessions and Cookies

Every user gets an `HttpSession`. It stores the visit number, the time of the previous visit, the session start time and the number of requests in the session.

Two cookies are stored in the browser for one year:

- `visitCount` - the number of visits to the site;
- `lastVisit` - the date and time of the last request.

A new visit starts when a new session is created. The footer of every page shows the visit counter, the previous visit and the session start.

One-time messages such as "Book issued" or "Copy is not available" are also passed through the session (`Flash`), so they survive the redirect and are shown exactly once.

### DAO Layer

The DAO layer is taken from the previous lab and works with `EntityManager`:

- queries are built with the Criteria API and the static Metamodel (`Book_`, `Loan_`, ...);
- write operations run inside an application-managed `EntityTransaction`;
- a copy is issued with a pessimistic lock (`PESSIMISTIC_WRITE`), so it cannot be given to two readers at once;
- JPA errors are wrapped in `LibraryException` with an error code.

`LookupDao` additionally supplies the lists with IDs for the drop-down menus on the pages.

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

Relations:

```
Author     *——*  Book       (table book_author)
Book       1——*  BookCopy
BookCopy   1——*  Loan
Reader     1——*  Loan
Librarian  1——*  Loan
```

## Project Structure

```
Lab5_WEB/
├── src/
│   └── main/
│       ├── java/com/example/lab5_web/
│       │   ├── Lab5WebApplication.java
│       │   ├── library/
│       │   │   ├── JpaUtil.java
│       │   │   ├── dao/        (BookDao, BookCopyDao, LoanDao, ReaderDao, LookupDao)
│       │   │   ├── entity/     (Author, Book, BookCopy, Librarian, Loan, Reader)
│       │   │   └── exception/  (LibraryException)
│       │   └── web/
│       │       ├── FrontController.java
│       │       ├── TemplateRenderer.java
│       │       ├── VisitTracker.java
│       │       ├── Flash.java
│       │       └── command/    (Command, ViewResult, Params, *Command)
│       └── resources/
│           ├── META-INF/persistence.xml
│           ├── simplelogger.properties
│           ├── static/         (library.jpg)
│           └── templates/      (fragments, styles, index, books, copies,
│                                issue, writeoff, debtors, error)
├── pom.xml
└── README.md
```

## Technology Stack

- Java 21
- Java Servlet API (Jakarta Servlet 6.1)
- Apache Tomcat 11 (embedded)
- Thymeleaf 3.1
- Jakarta Persistence API 3.2
- EclipseLink 5.0.2 (JPA provider and Metamodel Generator)
- MySQL and MySQL Connector/J
- Maven
- SLF4J
- HTML5 and CSS3

## Database Setup

Create a MySQL database named `library` with the tables `author`, `book`, `book_author`, `book_copy`, `librarian`, `reader` and `loan`, and a user that has access to it. The schema is not generated by JPA: the `ddl-generation` property is not set, so EclipseLink works with the existing tables.

## Configuration

Database access is configured in `src/main/resources/META-INF/persistence.xml`:

```xml
<property name="jakarta.persistence.jdbc.url"
          value="jdbc:mysql://localhost:3306/library?characterEncoding=utf8&amp;serverTimezone=UTC"/>
<property name="jakarta.persistence.jdbc.user" value="YOUR_USER"/>
<property name="jakarta.persistence.jdbc.password" value="YOUR_PASSWORD"/>
<property name="jakarta.persistence.jdbc.driver" value="com.mysql.cj.jdbc.Driver"/>
```

Replace `YOUR_USER` and `YOUR_PASSWORD` with your own credentials. Do not commit real passwords to the repository.

SQL logging is controlled with `eclipselink.logging.level.sql` (`FINE` prints every statement, `OFF` disables it).

## Metamodel Generation

The static metamodel classes (`Book_`, `Loan_`, etc.) are generated at compile time by the annotation processor configured in `pom.xml`. If the IDE does not see them, enable annotation processing and rebuild the project.

## Running the Application

Build the project:

```bash
mvn clean package
```

Run the application:

```bash
mvn exec:java
```

Or run `Lab5WebApplication` from the IDE. Open the site at:

```
http://localhost:8080/
```

The port can be changed with a system property, for example `-Dserver.port=9090`.

## Technologies Demonstrated

- front controller and command design patterns;
- Java Servlets (a single dispatcher servlet, request routing by method and path);
- server-side templating with Thymeleaf and template fragments;
- HTTP sessions and cookies;
- Post/Redirect/Get and one-time flash messages;
- JPA, Criteria API and Metamodel API in the DAO layer;
- embedded Tomcat without a framework.
