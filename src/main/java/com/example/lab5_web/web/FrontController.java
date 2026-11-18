package com.example.lab5_web.web;

import com.example.lab5_web.library.dao.BookCopyDao;
import com.example.lab5_web.library.dao.BookDao;
import com.example.lab5_web.library.dao.LoanDao;
import com.example.lab5_web.library.dao.LookupDao;
import com.example.lab5_web.library.dao.ReaderDao;
import com.example.lab5_web.library.exception.LibraryException;
import com.example.lab5_web.web.command.*;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Центральный сервлет-диспетчер. Принимает все HTTP-запросы приложения
 * (кроме статических файлов /static/*), анализирует URL, определяет нужный контроллер (команду)
 * и передаёт ему управление. Результат отображается через шаблон Thymeleaf или выполняется перенаправление.
 * Критические ошибки (в том числе недоступность БД) приводят к показу страницы error.html.
 */
public class FrontController extends HttpServlet {
  private static final Logger log = LoggerFactory.getLogger(FrontController.class);

  private static final String GENERIC_ERROR = "Произошла внутренняя ошибка. Попробуйте повторить попытку позже.";

  /** Таблица маршрутов: "МЕТОД /путь" -> контроллер. */
  private final Map<String, Command> commands = new HashMap<>();
  private TemplateRenderer renderer;

  @Override
  public void init() throws ServletException {
    BookDao bookDao = new BookDao();
    BookCopyDao bookCopyDao = new BookCopyDao();
    LoanDao loanDao = new LoanDao();
    ReaderDao readerDao = new ReaderDao();
    LookupDao lookupDao = new LookupDao();

    commands.put("GET /", new HomeCommand());
    commands.put("GET /books", new FindBooksCommand(bookDao, lookupDao));
    commands.put("GET /copies", new FreeCopiesCommand(bookDao, lookupDao));
    commands.put("GET /issue", new IssueFormCommand(lookupDao));
    commands.put("POST /issue", new IssueCommand(loanDao, lookupDao));
    commands.put("GET /writeoff", new WriteOffFormCommand(lookupDao));
    commands.put("POST /writeoff", new WriteOffCommand(bookCopyDao, lookupDao));
    commands.put("GET /debtors", new DebtorsCommand(readerDao));
    commands.put("GET /error", new ErrorCommand());

    renderer = new TemplateRenderer(getServletContext());
    log.info("Диспетчер инициализирован, зарегистрировано маршрутов: {}", commands.size());
  }

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");

    boolean errorDispatch = request.getDispatcherType() == DispatcherType.ERROR;
    String path = resolvePath(request);

    // каждому пользователю соответствует объект сессии; cookie обновляются до отправки ответа
    HttpSession session = request.getSession(true);
    if (!errorDispatch) {
      VisitTracker.track(request, response, session);
    }

    String method = errorDispatch ? "GET" : request.getMethod();
    String key = method + " " + path;
    log.info("Запрос: {} от {}", key, request.getRemoteAddr());

    Command command = commands.get(key);
    if (command == null) {
      boolean pathKnown = commands.containsKey("GET " + path) || commands.containsKey("POST " + path);
      log.warn("Маршрут не найден: {}", key);
      showError(request, response, session,
          pathKnown ? HttpServletResponse.SC_METHOD_NOT_ALLOWED : HttpServletResponse.SC_NOT_FOUND,
          pathKnown ? "Эта операция недоступна для данного адреса." : "Страница не найдена.");
      return;
    }

    try {
      ViewResult result = command.execute(request);
      if (result.isRedirect()) {
        log.debug("Перенаправление на {}", result.getRedirect());
        response.sendRedirect(request.getContextPath() + result.getRedirect());
      } else {
        Map<String, Object> model = new HashMap<>(result.getModel());
        addCommonData(session, model);
        renderer.render(request, response, result.getView(), model);
      }
    } catch (LibraryException e) {
      log.error("Ошибка [{}] при обработке {}: {}", e.getErrorCode(), key, e.getMessage(), e);
      int status = e.getErrorCode() == LibraryException.ErrorCode.DB_UNAVAILABLE
          ? HttpServletResponse.SC_SERVICE_UNAVAILABLE
          : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
      fail(request, response, session, status, e.getUserMessage());
    } catch (Exception e) {
      log.error("Непредвиденная ошибка при обработке {}", key, e);
      fail(request, response, session, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, GENERIC_ERROR);
    }
  }

  /** Путь запроса без имени приложения; пустой путь и завершающий "/" нормализуются. */
  private String resolvePath(HttpServletRequest request) {
    String path = request.getServletPath();
    String info = request.getPathInfo();
    if (info != null) {
      path += info;
    }
    if (path.isEmpty()) {
      path = "/";
    }
    if (path.length() > 1 && path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }
    return path;
  }

  /** Показывает страницу ошибки; если страница уже частично отправлена, сбрасывает буфер. */
  private void fail(HttpServletRequest request, HttpServletResponse response,
                    HttpSession session, int status, String message) throws IOException {
    if (response.isCommitted()) {
      log.warn("Ответ уже отправлен, страницу ошибки показать нельзя");
      return;
    }
    response.resetBuffer();
    showError(request, response, session, status, message);
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
    log.info("Диспетчер остановлен");
  }
}
