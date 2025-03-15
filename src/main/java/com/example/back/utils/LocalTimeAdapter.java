package com.example.back.utils;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class LocalTimeAdapter implements JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {

   private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a hh:mm:ss"); // 'a'는 AM/PM (Locale 적용)

   @Override
   public JsonElement serialize(LocalTime src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.format(formatter).replace("AM", "오전").replace("PM", "오후"));
   }

   @Override
   public LocalTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      return LocalTime.parse(json.getAsString(), formatter);
   }
}
