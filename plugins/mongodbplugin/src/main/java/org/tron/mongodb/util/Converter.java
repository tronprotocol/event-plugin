package org.tron.mongodb.util;

import com.alibaba.fastjson.JSON;
import java.io.Serializable;
import org.bson.Document;

public class Converter {

  public static Document jsonStringToDocument(String jsonString) {
    return Document.parse(jsonString);
  }

  public static String objectToJsonString(Serializable entity) {
    return JSON.toJSONString(entity);
  }

  public static <T> T jsonStringToObject(String jsonString, Class<T> clazz) {
    return JSON.parseObject(jsonString, clazz);
  }

}
