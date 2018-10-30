package org.timenkov.test.model;

import java.util.ArrayList;
import java.util.List;

public class EmailGithub {

  private List<String> usernames = new ArrayList<>();
  private String message;

  public List<String> getUsernames() {
    return usernames;
  }

  public void setUsernames(List<String> usernames) {
    this.usernames = usernames;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
