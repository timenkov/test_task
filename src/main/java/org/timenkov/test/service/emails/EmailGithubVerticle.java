package org.timenkov.test.service.emails;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import org.timenkov.test.Constants;
import org.timenkov.test.model.EmailGithub;
import org.timenkov.test.model.Response;

import java.util.HashSet;
import java.util.Set;

public class EmailGithubVerticle extends AbstractVerticle {

  private Emailer emailer = new Emailer();
  private WebClient client;

  @Override
  public void start() throws Exception {
    // init http client
    client = WebClient.create(vertx);

    vertx.eventBus().<JsonObject>consumer(Constants.EMAILS_GITHUB_ADDRESS, message -> {
      EmailGithub githubEmail = tryParseEmailGithub(message);
      if (githubEmail == null) {
        return;
      }

      vertx.<JsonObject>executeBlocking(future -> {
            Set<EmailCandidate> emailCandidates = new HashSet<>();

            for (String username : githubEmail.getUsernames()) {
              // Send a GET request
              client.get("api.github.com", "/users/" + username)
                  .send(ar -> {
                    if (ar.succeeded()) {
                      // Obtain response
                      JsonObject githubUserJson = ar.result().bodyAsJsonObject();
                      if (githubUserJson.getBoolean("email") != null) {
                        EmailCandidate emailCandidate = new EmailCandidate(
                            githubUserJson.getString("email"),
                            githubEmail.getMessage());
                        String location = githubUserJson.getString("location");
                        if (location != null) {
                          client.get("api.openweathermap.org", "/data/2.5/weather?appid=11eb0760b1f5bca7047fd269ed919a7f&q=" + location)
                              .send(ar2 -> {
                                if (ar2.succeeded()) {
                                  // Obtain response
                                  JsonObject weather = ar.result().bodyAsJsonObject();
                                  emailCandidate.setMessage(emailCandidate.getMessage() + "\n" + weather.toString());
                                  emailCandidates.add(emailCandidate);
                                }
                              });
                        } else {
                          emailCandidates.add(emailCandidate);
                        }
                      }
                    }
                  });
            }

            StringBuilder addresses = new StringBuilder();
            for (EmailCandidate emailCandidate : emailCandidates) {
              emailer.sendEmail(emailCandidate.getEmail(), emailCandidate.getMessage());
              addresses.append(emailCandidate.getEmail()).append(",");
            }

            future.complete(Response.ok(new JsonObject().put("msg", "emails sent to: " + addresses.toString())));
          },
          res -> {
            if (res.succeeded()) {
              message.reply(res.result());
            } else {
              message.reply(Response.ko(500, res.cause().getMessage()));
            }
          });
    });
  }

  private EmailGithub tryParseEmailGithub(Message<JsonObject> message) {
    try {
      return message.body().mapTo(EmailGithub.class);
    } catch (IllegalArgumentException e) {
      System.out.println("Incorrect Json for EmailGithub pojo.");
      message.reply(Response.ko(400, e.getMessage()));
    }
    return null;
  }

  private class EmailCandidate {
    private String email;
    private String message;

    public EmailCandidate(String email, String message) {
      this.email = email;
      this.message = message;
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }
  }
}
