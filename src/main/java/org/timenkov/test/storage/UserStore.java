package org.timenkov.test.storage;

import org.timenkov.test.model.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserStore {

  private Map<String, User> users = new ConcurrentHashMap<>();

  public User findUser(String email) {
    return users.get(email);
  }

  public void saveUser(User user) {
    users.putIfAbsent(user.getEmail(), user);
  }

  public void deleteUser(User user) {
    users.remove(user.getEmail());
  }
}
