package com.example.lab5_web.web;

import com.example.lab5_web.library.JpaUtil;
import com.example.lab5_web.library.dao.BookCopyDao;
import com.example.lab5_web.library.dao.BookDao;
import com.example.lab5_web.library.dao.LoanDao;
import com.example.lab5_web.library.dao.LookupDao;
import com.example.lab5_web.library.dao.ReaderDao;
import com.example.lab5_web.library.exception.LibraryException;
import com.example.lab5_web.web.command.*;
import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;



public class FrontController extends HttpServlet {
  private static final Logger log = LoggerFactory.getLogger(FrontController.class);

  private final Map<String, Command> commands = new HashMap<>();
  private TemplateRenderer renderer;

  @Override
  public void init() throws ServletException {
    EntityManagerFactory emf = JpaUtil.getEntityManagerFactory();

    BookDao bookDao = new BookDao(emf);
    BookCopyDao bookCopyDao = new BookCopyDao(emf);
    LoanDao loanDao = new LoanDao(emf);
    ReaderDao readerDao = new ReaderDao(emf);
    LookupDao lookupDao = new LookupDao(emf);

    commands.put("GET /", new HomeCommand());
    commands.put("GET /books", new FindBooksCommand(bookDao, lookupDao));
    commands.put("GET /copies", new FreeCopiesCommand(bookDao, lookupDao));
    commands.put("GET /issue", new IssueFormCommand(lookupDao));
    commands.put("POST /issue", new IssueCommand(loanDao, lookupDao));
    commands.put("GET /writeoff", new WriteOffFormCommand(lookupDao));
    commands.put("POST /writeoff", new WriteOffCommand(bookCopyDao, lookupDao));
    commands.put("GET /debtors", new DebtorsCommand(readerDao));

    renderer = new TemplateRenderer(getServletContext());
  }

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");

    String path = request.getRequestURI().substring(request.getContextPath().length());

    // статические файлы (картинки) отдаём напрямую, без сессии и счётчиков
    if ("GET".equals(request.getMethod()) && path.startsWith("/static/")) {
      serveStatic(path, response);
      return;
    }

    // каждому пользователю соответствует объект сессии; cookie обновляются до отправки ответа
    HttpSession session = request.getSession(true);
    VisitTracker.track(request, response, session);

    // нормализация пути
    if (path.length() > 1 && path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }
    String key = request.getMethod() + " " + path;
    log.info("Запрос: {}", key);

    Command command = commands.get(key);
    if (command == null) {
      showError(request, response, session, HttpServletResponse.SC_NOT_FOUND,
          "Страница не найдена: " + path);
      return;
    }

    try {
      ViewResult result = command.execute(request);
      if (result.isRedirect()) {
        response.sendRedirect(request.getContextPath() + result.getRedirect());
      } else {
        Map<String, Object> model = new HashMap<>(result.getModel());
        addCommonData(session, model);
        renderer.render(request, response, result.getView(), model);
      }
    } catch (LibraryException e) {
      log.error("Ошибка бизнес-логики [{}]", e.getErrorCode(), e);
      showError(request, response, session, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          e.getMessage() + " [" + e.getErrorCode() + "]");
    } catch (Exception e) {
      log.error("Непредвиденная ошибка", e);
      showError(request, response, session, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Внутренняя ошибка сервера: " + e.getMessage());
    }
  }

  /** Отдаёт файл из папки resources/static (картинки и т.п.). */
  private void serveStatic(String path, HttpServletResponse response) throws IOException {
    String resource = path.substring(1);              // "static/library.jpg"
    if (resource.contains("..")) {                    // защита от выхода за пределы папки
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
      if (in == null) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }
      response.setContentType(contentType(resource));
      response.setHeader("Cache-Control", "public, max-age=86400");
      in.transferTo(response.getOutputStream());
    }
  }

  private String contentType(String name) {
    String n = name.toLowerCase();
    if (n.endsWith(".jpg") || n.endsWith(".jpeg")) return "image/jpeg";
    if (n.endsWith(".png")) return "image/png";
    if (n.endsWith(".webp")) return "image/webp";
    if (n.endsWith(".svg")) return "image/svg+xml";
    if (n.endsWith(".css")) return "text/css";
    return "application/octet-stream";
  }

  private void showError(HttpServletRequest request, HttpServletResponse response,
                         HttpSession session, int status, String message) throws IOException {
    response.setStatus(status);
    Map<String, Object> model = new HashMap<>();
    model.put("status", status);
    model.put("message", message);
    addCommonData(session, model);
    renderer.render(request, response, "error", model);
  }

  /** Данные, нужные на каждой странице: сообщение пользователю и сведения о сессии/посещениях. */
  private void addCommonData(HttpSession session, Map<String, Object> model) {
    Flash.moveToModel(session, model);
    VisitTracker.moveToModel(session, model);
  }

  @Override
  public void destroy() {
    JpaUtil.close();
  }
}
