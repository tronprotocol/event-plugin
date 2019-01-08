package org.tron.mongodb.util;

import java.io.Serializable;

import org.bson.Document;

import com.alibaba.fastjson.JSON;

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
