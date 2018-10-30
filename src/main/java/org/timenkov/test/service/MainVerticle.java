package org.timenkov.test.service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.jwt.JWTOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import org.timenkov.test.Constants;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start() {

    HttpServer server = vertx.createHttpServer();

    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());

    JWTAuthOptions config = new JWTAuthOptions()
        .setKeyStore(new KeyStoreOptions()
            .setPath("keystore.jceks")
            .setPassword("secret"));

    JWTAuth jwtAuth = JWTAuth.create(vertx, config);

    router.route().handler(JWTAuthHandler.create(jwtAuth, "/api/sign"));

    router.post("/api/signup").handler(rc -> {
      handleAuth(Constants.SIGNUP_ADDRESS, rc, jwtAuth);
    });

    router.post("/api/signin").handler(rc -> {
      handleAuth(Constants.SIGNIN_ADDRESS, rc, jwtAuth);
    });

    router.post("/api/emails/github").handler(rc -> {
      handle(Constants.EMAILS_GITHUB_ADDRESS, rc);
    });

    //
    server.requestHandler(router::accept).listen(8080);
  }

  private void handle(String eventBus, RoutingContext rc) {
    JsonObject object = rc.getBodyAsJson();
    vertx.eventBus().<JsonObject>send(eventBus, object, reply -> {
      HttpServerResponse response = rc.response();
      response.putHeader("content-type", "application/json");
      if (reply.succeeded()) {
        response.end(reply.result().body().encodePrettily());
      } else {
        response.setStatusCode(500);
        response.end(reply.cause().getMessage());
      }
    });
  }

  private void handleAuth(String eventBus, RoutingContext rc, JWTAuth jwtAuth) {
    JsonObject object = rc.getBodyAsJson();
    vertx.eventBus().<JsonObject>send(eventBus, object, reply -> {
      HttpServerResponse response = rc.response();
      response.putHeader("content-type", "application/json");
      if (reply.succeeded()) {
        JsonObject result = reply.result().body();
        if (result.getInteger("statusCode") == 200) {
          String token = jwtAuth.generateToken(
              new JsonObject()
                  .put("username", result.getJsonObject("body").getString("email")),
              new JWTOptions()
                  .setSubject("test_task API")
                  .setIssuer("Vert.x"));
          result.put("token", token);
        }
        response.end(result.encodePrettily());
      } else {
        response.setStatusCode(500);
        response.end(reply.cause().getMessage());
      }
    });
  }
}
