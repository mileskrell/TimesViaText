package com.example.mmkrell.timesviatext;

public class Stop {

    private int stopId;
    private int stopCode;
    private String stopName;
    private String stopDesc;
    private double stopLat;
    private double stopLon;
    private int locationType;
    private int parentStation;
    private int wheelchairBoarding;

    public Stop() {
        this(0, 0, "STOPNAME", "STOPDESC", 1.0, 2.0, 1, 2, 3);
    }

    public Stop(int stopId, int stopCode, String stopName, String stopDesc, double stopLat, double stopLon, int locationType, int parentStation, int wheelchairBoarding) {
        this.stopId = stopId;
        this.stopCode = stopCode;
        this.stopName = stopName;
        this.stopDesc = stopDesc;
        this.stopLat = stopLat;
        this.stopLon = stopLon;
        this.locationType = locationType;
        this.parentStation = parentStation;
        this.wheelchairBoarding = wheelchairBoarding;
    }
    public int getStopId() {
        return stopId;
    }

    public int getStopCode() {
        return stopCode;
    }

    public String getStopName() {
        return stopName;
    }

    public String getStopDesc() {
        return stopDesc;
    }

    public double getStopLat() {
        return stopLat;
    }

    public double getStopLon() {
        return stopLon;
    }

    public int getLocationType() {
        return locationType;
    }

    public int getParentStation() {
        return parentStation;
    }

    public int getWheelchairBoarding() {
        return wheelchairBoarding;
    }
}
