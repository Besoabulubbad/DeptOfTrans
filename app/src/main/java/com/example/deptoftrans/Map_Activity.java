package com.example.deptoftrans;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryDimension;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.IdentifyGraphicsOverlayResult;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.google.android.material.navigation.NavigationView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.zip.Inflater;

public class Map_Activity extends AppCompatActivity {


    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ImageView navButton;
    private Boolean nVaigation=false;
    private MapView mMapView;
    private LocationDisplay mLocationDisplay;
    private GraphicsOverlay mGraphicsOverlay;
    private BitmapDrawable parkingMarker;
    private   PictureMarkerSymbol parkingPicture;
    private Graphic markerGraphics;
    private ParkingModel[] parkingModel;
    private Callout mCallout=null;
    LayoutInflater inflater;

    private static final String sTag = "Gesture";
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        inflater=this.getLayoutInflater();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
       initViews();
        try {
            parkingModel= readFile();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mLocationDisplay = mMapView.getLocationDisplay();
        mLocationDisplay.addDataSourceStatusChangedListener(dataSourceStatusChangedEvent -> {
            if (dataSourceStatusChangedEvent.isStarted() || dataSourceStatusChangedEvent.getError() == null) {
                return;
            }

            int requestPermissionsCode = 2;
            String[] requestPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

            if (!(ContextCompat.checkSelfPermission(Map_Activity.this, requestPermissions[0]) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(Map_Activity.this, requestPermissions[1]) == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(Map_Activity.this, requestPermissions, requestPermissionsCode);
            } else {
                String message = String.format("Error in DataSourceStatusChangedListener: %s",
                        dataSourceStatusChangedEvent.getSource().getLocationDataSource().getError().getMessage());
                Toast.makeText(Map_Activity.this, message, Toast.LENGTH_LONG).show();
            }
        });
        mLocationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.COMPASS_NAVIGATION);
        mLocationDisplay.startAsync();
        setupMap();
        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {

            @Override
            public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                Log.d(sTag, "onSingleTapConfirmed: " + motionEvent.toString());


                // get the point that was clicked and convert it to a point in map coordinates
                android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()),
                        Math.round(motionEvent.getY()));
                    final ListenableFuture<IdentifyGraphicsOverlayResult> identifyGraphic = mMapView.identifyGraphicsOverlayAsync(mGraphicsOverlay, screenPoint, 10.0, false, 2);
                // create a map point from screen point
                identifyGraphic.addDoneListener(new Runnable() {
                    @Override
                    public void run() {

                        IdentifyGraphicsOverlayResult grOverlayResult = null;
                        try {
                            grOverlayResult = identifyGraphic.get();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        // get the list of graphics returned by identify graphic overlay
                        List<Graphic> graphic = grOverlayResult.getGraphics();
                        // get size of list in results
                        int identifyResultSize = graphic.size();
                        if (graphic.size()>0) {
                            for (int i = 0; i <= graphic.size(); i++) {
                                Point graphicPoint = (Point) graphic.get(i).getGeometry();
                                double x = graphicPoint.getX();
                                double y = graphicPoint.getY();
                                for (ParkingModel in : parkingModel) {
                                    if (in.areaMarker.x.equals(x) & in.areaMarker.y.equals(y)) {
                                        // convert to WGS84 for lat/lon format

                                        // create a textview for the callout
                                        TextView calloutContent = new TextView(getApplicationContext());
                                        calloutContent.setTextColor(Color.BLACK);
                                        calloutContent.setSingleLine();
                                        // format coordinates to 4 decimal places
                                        View mCalloutView = inflater.inflate(R.layout.mcallout_layout,null);
                                        TextView name = (TextView) mCalloutView.findViewById(R.id.parking_name);
                                        TextView total = (TextView) mCalloutView.findViewById(R.id.total_txt);
                                        TextView reservedCount = (TextView) mCalloutView.findViewById(R.id.reservedcount_txt);
                                        TextView availableCount = (TextView) mCalloutView.findViewById(R.id.available_count_txt);
                                        TextView availablePerc = (TextView) mCalloutView.findViewById(R.id.avilable_perc_txt);
                                        TextView areaName = (TextView) mCalloutView.findViewById(R.id.parking_name_txt);
                                        areaName.setText("  Area parking "+in.name+" Area");
                                        name.setText(in.name);
                                        total.setText(String.valueOf(in.total));
                                        reservedCount.setText(String.valueOf(in.reservedCount));
                                        availableCount.setText(String.valueOf(in.availableCount));
                                        int ava,tot;
                                        ava=in.availableCount;
                                        tot=in.total;
                                        double percD=((double)ava/(double)tot)*100;
                                        availablePerc.setText(String.format("%d%%", (int)percD));
                                        name.setEnabled(true);
                                        total.setEnabled(true);
                                        reservedCount.setEnabled(true);
                                        availableCount.setEnabled(true);
                                        availablePerc.setEnabled(true);
                                        // get callout, set content and show
                                        mCallout = mMapView.getCallout();
                                        mCallout.setLocation(graphicPoint);
                                        mCallout.getStyle().setBorderColor(Color.WHITE);
                                        mCallout.getStyle().setBorderWidth(0);
                                        mCallout.getStyle().setCornerRadius(20);

                                        mCallout.setContent(mCalloutView);
                                        mCallout.show();

                                        // center on tapped point
                                        mMapView.setViewpointCenterAsync(graphicPoint);
                                        return;
                                    }
                                }
                            }
                        }
                        else if(mCallout!=null) {
                            if(mCallout.isShowing())
                            mCallout.dismiss();
                        }

                    }

                });

                    return true;
            }
        });

        setupLocationDisplay();
        createGraphics(parkingModel);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mLocationDisplay.startAsync();
        } else {
            Toast.makeText(Map_Activity.this, getResources().getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onPause() {
        if (mMapView != null) {
            mMapView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        if (mMapView != null) {
            mMapView.dispose();
        }
        super.onDestroy();
    }
    private ParkingModel[] readFile() throws IOException, JSONException {
        InputStream is  = getResources().openRawResource(R.raw.parking);
        ParkingModel[] parkingModel;
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            is.close();
        }

        String jsonString = writer.toString();
        JSONObject parkingJson = new JSONObject(jsonString);
        JSONArray collection = parkingJson.getJSONArray("collection");
        parkingModel = new ParkingModel[collection.length()];
        for (int i=0 ; i<collection.length();i++)
        {
            JSONObject parkingObject= collection.getJSONObject(i);
            JSONObject areaMarkerObject=parkingObject.getJSONObject("areaMarker");
            JSONArray areaCoordinatesArray=parkingObject.getJSONArray("areaCoordinates");
            AreaMarker areaMarker = null;
            AreaCoordinates areaCoordinates=null;
               areaMarker = new AreaMarker(areaMarkerObject.getDouble("x"),areaMarkerObject.getDouble("y"),areaMarkerObject.getDouble("long"),areaMarkerObject.getDouble("lat"));
            Double[] lat = new Double[0],longitude = new Double[0];
            lat=new Double[areaCoordinatesArray.length()];
            longitude=new Double[areaCoordinatesArray.length()];
            for (int i2=0;i2<areaCoordinatesArray.length(); i2++)
            {
               JSONArray areaCoordinatesArray2 = areaCoordinatesArray.getJSONArray(i2);
                   lat[i2]= areaCoordinatesArray2.getDouble(0);
                   longitude[i2]=areaCoordinatesArray2.getDouble(1);


            }
            areaCoordinates=new AreaCoordinates(lat[0],longitude[0],lat[1],longitude[1],lat[2],longitude[2],lat[3],longitude[3],lat[4],longitude[4]);
            parkingModel[i]= new ParkingModel(parkingObject.getString("parkingId"),parkingObject.getString("name"),parkingObject.getString("description"),parkingObject.getInt("total"),parkingObject.getInt("reservedCount"),parkingObject.getInt("availableCount"),areaMarker,areaCoordinates);
        }
        return parkingModel;
 }
    private void initViews() {
        drawerLayout=findViewById(R.id.drawer_layout);
        navigationView= findViewById(R.id.nv);
        mMapView=findViewById(R.id.mapView);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        navButton=findViewById(R.id.navButton);
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nVaigation){
                    drawerLayout.closeDrawer(GravityCompat.END);
                }else{
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }

        });
        drawerLayout.addDrawerListener(new ActionBarDrawerToggle(this,
                drawerLayout,
                0,
                0){
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                nVaigation=false;//is Closed
            }
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                drawerView.bringToFront();
                drawerView.requestLayout();
                nVaigation=true;//is Opened
            }});

    }
    private void setupMap() {
        if (mMapView != null) {
            Basemap.Type basemapType = Basemap.Type.OPEN_STREET_MAP;
            double latitude = 24.3865729;
            double longitude = 54.2784284;
            int levelOfDetail = 13;
            ArcGISMap map = new ArcGISMap(basemapType, latitude, longitude, levelOfDetail);
            mMapView.setMap(map);

        }
    }
    private void setupLocationDisplay() {
        mLocationDisplay = mMapView.getLocationDisplay();
    }
    private void createGraphicsOverlay() {
        mGraphicsOverlay = new GraphicsOverlay();
        mMapView.getGraphicsOverlays().add(mGraphicsOverlay);

    }
    private void createMarkers(ParkingModel[] parkingMode) {
        for(ParkingModel in : parkingMode)
        {
            parkingMarker = (BitmapDrawable)(ContextCompat.getDrawable(this, R.drawable.parking_marker_icon));
            parkingPicture= new PictureMarkerSymbol(parkingMarker);
            parkingPicture.setHeight(50);
            parkingPicture.setWidth(50);
            com.esri.arcgisruntime.geometry.Point markerPoint = new com.esri.arcgisruntime.geometry.Point( in.areaMarker.x,  in.areaMarker.y, SpatialReferences.getWebMercator());
            markerGraphics = new Graphic(markerPoint,parkingPicture);

            mGraphicsOverlay.getGraphics().add(markerGraphics);

        }


    }

    private void createPolygonGraphics(ParkingModel[] parkingMode) {
        for (ParkingModel in : parkingMode) {
            SimpleFillSymbol polygonSymbol = null;
            int total, reservedCount, red, green, yellow;
            total = in.total;
            reservedCount =in.reservedCount;
            red = total * 90 / 100;
            yellow = total * 70 / 100;
            green = total * 40 / 100;

            PointCollection polygonPoints = new PointCollection(SpatialReferences.getWebMercator());
            polygonPoints.add(new com.esri.arcgisruntime.geometry.Point(in.areaCoordinates.pointX,
                    in.areaCoordinates.pointY));
            polygonPoints.add(new com.esri.arcgisruntime.geometry.Point(in.areaCoordinates.pointX1,
                    in.areaCoordinates.pointY1));
            polygonPoints.add(new com.esri.arcgisruntime.geometry.Point(in.areaCoordinates.pointX2,
                    in.areaCoordinates.pointY2));
            polygonPoints.add(new com.esri.arcgisruntime.geometry.Point(in.areaCoordinates.pointX3,
                    in.areaCoordinates.pointY3));
            polygonPoints.add(new com.esri.arcgisruntime.geometry.Point(in.areaCoordinates.pointX4,
                    in.areaCoordinates.pointY4));
            Polygon polygon = new Polygon(polygonPoints);

            if (reservedCount <= green) {
                polygonSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.GREEN,
                        new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.GREEN, 2.0f));
            } else if (reservedCount <= yellow) {
                polygonSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.YELLOW,
                        new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.YELLOW, 2.0f));
            } else {

                polygonSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.RED,
                        new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 2.0f));
            }
            assert polygonSymbol != null;
            Graphic polygonGraphic = new Graphic(polygon, polygonSymbol);
            mGraphicsOverlay.getGraphics().add(polygonGraphic);
        }

    }




    private void createGraphics(ParkingModel[] parkingModel) {
        createGraphicsOverlay();
        createMarkers(parkingModel);
        createPolygonGraphics(parkingModel);


    }
}