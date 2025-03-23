package com.example.back.utils;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalTime;

public class LocalTimeAdapter implements JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {

   @Override
   public JsonElement serialize(LocalTime src, Type typeOfSrc, JsonSerializationContext context) {
      int hour = src.getHour();
      String period = hour < 12 ? "오전" : "오후";

      int displayHour = hour % 12;
      if (displayHour == 0) displayHour = 12;

      String formattedTime = String.format(
            "%s %02d:%02d:%02d",
            period,
            displayHour,
            src.getMinute(),
            src.getSecond()
      );

      return new JsonPrimitive(formattedTime);
   }

   @Override
   public LocalTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      String timeStr = json.getAsString().trim(); // 예: "오후 03:15:20"
      String[] parts = timeStr.split(" ");

      if (parts.length != 2) {
         throw new JsonParseException("시간 형식이 올바르지 않습니다: " + timeStr);
      }

      String period = parts[0]; // "오전" 또는 "오후"
      String[] hms = parts[1].split(":");

      if (hms.length != 3) {
         throw new JsonParseException("시:분:초 형식이 올바르지 않습니다: " + parts[1]);
      }

      int hour = Integer.parseInt(hms[0]) % 12;
      if ("오후".equals(period)) hour += 12;

      int minute = Integer.parseInt(hms[1]);
      int second = Integer.parseInt(hms[2]);

      return LocalTime.of(hour, minute, second);
   }
}
