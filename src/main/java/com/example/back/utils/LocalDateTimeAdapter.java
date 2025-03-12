package com.example.back.utils;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

   // 날짜 포맷 정의 (필요에 따라 변경)
   private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

   @Override
   public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
      // LocalDateTime을 JSON 문자열로 변환
      return new JsonPrimitive(src.format(formatter));
   }

   @Override
   public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      // JSON 문자열을 LocalDateTime으로 변환
      return LocalDateTime.parse(json.getAsString(), formatter);
   }
}
