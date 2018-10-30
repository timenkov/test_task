package org.timenkov.test;

import io.vertx.core.Vertx;
import org.timenkov.test.service.emails.EmailGithubVerticle;
import org.timenkov.test.service.MainVerticle;
import org.timenkov.test.service.SignVerticle;

public class App {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();

    vertx.deployVerticle(new MainVerticle());
    vertx.deployVerticle(new SignVerticle());
    vertx.deployVerticle(new EmailGithubVerticle());
  }
}
