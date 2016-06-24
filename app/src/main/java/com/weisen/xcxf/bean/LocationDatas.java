package com.weisen.xcxf.bean;

public class LocationDatas {
	private String id;
	private String myTime;
	private Double myLat = 0.0;
	private Double myLng = 0.0;
	
	public String getMyTime() {
		return myTime;
	}
	public void setMyTime(String myTime) {
		this.myTime = myTime;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Double getMyLat() {
		return myLat;
	}
	public void setMyLat(Double myLat) {
		this.myLat = myLat;
	}
	public Double getMyLng() {
		return myLng;
	}
	public void setMyLng(Double myLng) {
		this.myLng = myLng;
	}
	
	
}
