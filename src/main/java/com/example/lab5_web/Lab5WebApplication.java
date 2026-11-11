package com.example.lab5_web;

import com.example.lab5_web.library.JpaUtil;
import com.example.lab5_web.web.FrontController;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;

import java.nio.file.Files;

/** Запускает встроенный Tomcat и регистрирует единственный сервлет-диспетчер на все URL. */
public class Lab5WebApplication {

  public static void main(String[] args) throws Exception {
    int port = Integer.getInteger("server.port", 8080);

    Tomcat tomcat = new Tomcat();
    tomcat.setBaseDir(Files.createTempDirectory("lab5-tomcat").toString());
    tomcat.setPort(port);
    tomcat.getConnector();

    Context context = tomcat.addContext("", Files.createTempDirectory("lab5-docbase").toString());

    // центральный сервлет-диспетчер: принимает ВСЕ запросы приложения ("/")
    Wrapper wrapper = Tomcat.addServlet(context, "frontController", new FrontController());
    wrapper.setLoadOnStartup(1);
    context.addServletMappingDecoded("/", "frontController");

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try {
        tomcat.stop();
        tomcat.destroy();
      } catch (Exception ignored) {
        // остановка приложения
      }
      JpaUtil.close();
    }));

    tomcat.start();
    System.out.println("Приложение запущено: http://localhost:" + port + "/");
    tomcat.getServer().await();
  }
}
