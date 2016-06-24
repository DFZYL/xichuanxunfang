package com.weisen.xcxf.bean;

import java.util.ArrayList;
import java.util.List;

public class PeriodTime {

	private String id;
	private String name;
	private int time;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public PeriodTime(String id, String name, int time) {
		super();
		this.id = id;
		this.name = name;
		this.time = time;
	}

	public static List<PeriodTime> getList() {
		List<PeriodTime> periodList = new ArrayList<PeriodTime>();
		PeriodTime periodTime = new PeriodTime("1", "不上报", 0);
		periodList.add(periodTime);
		PeriodTime periodTime2 = new PeriodTime("2", "步行模式", 10);
		periodList.add(periodTime2);
		PeriodTime periodTime3 = new PeriodTime("3", "200米", 200);
		periodList.add(periodTime3);
		PeriodTime periodTime4 = new PeriodTime("4", "300米", 300);
		periodList.add(periodTime4);
		PeriodTime periodTime5 = new PeriodTime("5", "400米", 400);
		periodList.add(periodTime5);
		PeriodTime periodTime6 = new PeriodTime("6", "500米", 500);
		periodList.add(periodTime6);
		return periodList;
	}
}
