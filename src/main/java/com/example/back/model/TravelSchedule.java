package com.example.back.model;

import lombok.Data;

import java.util.List;

@Data
public class TravelSchedule {
	private String day;
	private List<ScheduleItem> activities;

	@Data
	public static class ScheduleItem {
		private String time;
		private String title;
		private String desc;
	}
}
