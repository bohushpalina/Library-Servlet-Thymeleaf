package com.example.lab5_web.web;

import com.example.lab5_web.library.JpaUtil;
import com.example.lab5_web.library.exception.LibraryException;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/** Запуск и остановка приложения: настройка логирования, проверка БД, закрытие JPA. */
public class AppContextListener implements ServletContextListener {
  private static final Logger log = LoggerFactory.getLogger(AppContextListener.class);

  @Override
  public void contextInitialized(ServletContextEvent event) {
    // логи (java.util.logging) направляются в SLF4J -> Log4j2
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();

    ServletContext context = event.getServletContext();
    log.info("Запуск приложения '{}' (контекст '{}', контейнер: {})",
        context.getServletContextName(), context.getContextPath(), context.getServerInfo());

    // проверка подключения к БД; при ошибке приложение всё равно запускается,
    // а пользователь увидит страницу error.html при обращении к данным
    EntityManager em = null;
    try {
      em = JpaUtil.createEntityManager();
      log.info("Подключение к базе данных установлено");
    } catch (LibraryException e) {
      log.error("База данных недоступна при запуске приложения: {}", e.getMessage());
    } finally {
      if (em != null) {
        em.close();
      }
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent event) {
    log.info("Остановка приложения");
    JpaUtil.close();
    SLF4JBridgeHandler.uninstall();
  }
}
