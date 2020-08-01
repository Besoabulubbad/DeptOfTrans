package com.example.deptoftrans;

public class ParkingModel {
   public String parkingId,name,description,total,reservedCount,availableCount;
    public   AreaMarker areaMarker;
    public AreaCoordinates areaCoordinates;
ParkingModel(String parkingId, String name, String description, String total, String reservedCount, String availableCount, AreaMarker areaMarker, AreaCoordinates areaCoordinates)
{
    this.parkingId=parkingId; this.name=name; this.description=description; this.total=total; this.reservedCount=reservedCount;
    this.availableCount=availableCount; this.areaMarker=areaMarker; this.areaCoordinates=areaCoordinates;
}
}
class AreaMarker
{
    public Double x,y, longitude,lat;
AreaMarker(Double x,Double y ,Double longitude, Double lat)
{
    this.x=x; this.y=y; this.longitude=longitude; this.lat=lat;
}

}
class AreaCoordinates
{
    public Double pointX,pointY,pointX1,pointY1,pointX2,pointY2,pointX3,pointY3,pointX4,pointY4;
AreaCoordinates(Double pointX,Double pointY,Double pointX1,Double pointY1,Double pointX2,Double pointY2,Double pointX3,Double pointY3 ,Double pointX4,Double pointY4)
{
    this.pointX=pointX; this.pointY=pointY;
    this.pointX1=pointX1; this.pointY1=pointY1;
    this.pointX2=pointX2; this.pointY2=pointY2;
    this.pointX3=pointX3; this.pointY3=pointY3;
    this.pointX4=pointX4; this.pointY4=pointY4;
}
}