package org.timenkov.test.model;

import io.vertx.core.json.JsonObject;

public class Response {

  private int statusCode;
  private ResponseType responseType;
  private JsonObject body;

  public int getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  public ResponseType getResponseType() {
    return responseType;
  }

  public void setResponseType(ResponseType responseType) {
    this.responseType = responseType;
  }

  public JsonObject getBody() {
    return body;
  }

  public void setBody(JsonObject body) {
    this.body = body;
  }

  public static JsonObject ok(JsonObject body) {
    Response response = new Response();
    response.setBody(body);
    response.setStatusCode(200);
    response.setResponseType(ResponseType.SUCCESS);
    return JsonObject.mapFrom(response);
  }

  public static JsonObject ko(int statusCode, String message) {
    Response response = new Response();
    response.setBody(new JsonObject().put("msg", message));
    response.setStatusCode(statusCode);
    response.setResponseType(ResponseType.FAILED);
    return JsonObject.mapFrom(response);
  }

  private enum ResponseType {
    SUCCESS,
    FAILED
  }
}
