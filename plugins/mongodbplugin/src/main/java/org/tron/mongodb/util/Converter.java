package org.tron.mongodb.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.Serializable;
import org.bson.Document;

public class Converter {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static Document jsonStringToDocument(String jsonString) {
    return Document.parse(jsonString);
  }

  public static String objectToJsonString(Serializable entity) {
    try {
      return OBJECT_MAPPER.writeValueAsString(entity);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Failed to serialize entity to JSON", e);
    }
  }

  public static <T> T jsonStringToObject(String jsonString, Class<T> clazz) {
    try {
      return OBJECT_MAPPER.readValue(jsonString, clazz);
    } catch (IOException e) {
      throw new IllegalArgumentException("Failed to deserialize JSON to object", e);
    }
  }

}
