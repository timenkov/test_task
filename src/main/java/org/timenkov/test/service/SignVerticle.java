package org.timenkov.test.service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.timenkov.test.Constants;
import org.timenkov.test.model.Response;
import org.timenkov.test.model.User;
import org.timenkov.test.storage.UserStore;

public class SignVerticle extends AbstractVerticle {

  private UserStore userStore = new UserStore();

  @Override
  public void start() throws Exception {

    vertx.eventBus().<JsonObject>consumer(Constants.SIGNIN_ADDRESS, message -> {
      User user = tryParseUser(message);
      if (user == null) {
        return;
      }
      User found = userStore.findUser(user.getEmail());
      if (found == null) {
        message.reply(Response.ko(400, "user with such email not registered"));
      } else if (found.getPassword().equals(user.getPassword())) {// check password match
        User signin = new User();
        signin.setEmail(found.getEmail());
        signin.setAvatar(found.getAvatar());
        message.reply(Response.ok(JsonObject.mapFrom(signin)));
      } else {
        message.reply(Response.ko(400, "email or password incorrect"));
      }
    });

    vertx.eventBus().<JsonObject>consumer(Constants.SIGNUP_ADDRESS, message -> {
      User user = tryParseUser(message);
      if (user == null) {
        return;
      }
      // check email unique on registration
      if (userStore.findUser(user.getEmail()) != null) {
        message.reply(Response.ko(400, "user with such email exists"));
      } else {
        userStore.saveUser(user);
        message.reply(Response.ok(new JsonObject().put("avatar", user.getAvatar())));
      }
    });
  }

  private User tryParseUser(Message<JsonObject> message) {
    try {
      return message.body().mapTo(User.class);
    } catch (IllegalArgumentException e) {
      System.out.println("Incorrect Json for User.");
      message.reply(Response.ko(400, e.getMessage()));
    }
    return null;
  }
}
