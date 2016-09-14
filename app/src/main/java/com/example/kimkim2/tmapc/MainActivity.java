package com.example.kimkim2.tmapc;

        import android.content.Context;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.graphics.Color;
        import android.location.Address;
        import android.location.Geocoder;
        import android.location.Location;
        import android.location.LocationListener;
        import android.location.LocationManager;
        import android.os.Bundle;
        import android.support.v7.app.AlertDialog;
        import android.support.v7.app.AppCompatActivity;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.RelativeLayout;
        import android.widget.Toast;


        import com.skp.Tmap.TMapData;
        import com.skp.Tmap.TMapPOIItem;
        import com.skp.Tmap.TMapPoint;
        import com.skp.Tmap.TMapPolyLine;
        import com.skp.Tmap.TMapView;

        import org.w3c.dom.Document;
        import org.w3c.dom.Element;
        import org.w3c.dom.NamedNodeMap;
        import org.w3c.dom.Node;
        import org.w3c.dom.NodeList;
        import org.xmlpull.v1.XmlPullParser;
        import org.xmlpull.v1.XmlPullParserFactory;

        import java.io.ByteArrayInputStream;
        import java.io.IOException;
        import java.io.InputStream;
        import java.net.MalformedURLException;
        import java.util.ArrayList;
        import java.util.Date;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Locale;
        import java.util.Map;
        import java.util.logging.LogManager;

        import javax.xml.parsers.DocumentBuilder;
        import javax.xml.parsers.DocumentBuilderFactory;


public class MainActivity extends AppCompatActivity {

    String TAG = "PAAR";
    TMapView mMapView = null;
    RelativeLayout mapContainer = null;
    EditText my_location;
    EditText my_destination;
    Button VR;
    Button Search;
    Button roadservice;
    Context mContext;
    Geocoder coder;
    CameraOverlayview mOverlayview;
    public static double latitude_plic;
    public static double longitude_plic;
    public static double des_latitude_plic;
    public static double des_longitude_plic;

    class MyListenerClass implements View.OnClickListener {
        public void onClick(View v) {
            if (v.getId() == R.id.search) {
                //Intent intent = new Intent(MainActivity.this,Listview.class);
                //startActivity(intent);
                findAllPoi();
            } else if (v.getId() == R.id.VR) {
                List<Address> des_location = null;
                String end = my_destination.getText().toString().replace("[", "").replace("]", "").replace(")", "").replace("(", "").trim();
                Toast.makeText(getApplicationContext(), "도착지 : " + end, Toast.LENGTH_SHORT).show();

                if (end == null || end.length() == 0) {
                    showToast("검색어가 입력되지 않았습니다.");
                    return;
                }
                try {    //  도착위치 값 지오코딩.
                    des_location = coder.getFromLocationName(end, 5);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "입출력오류 :" + e.getMessage() + "", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                des_latitude_plic = des_location.get(0).getLatitude();
                des_longitude_plic = des_location.get(0).getLongitude();

                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                intent.putExtra("latitude_id", String.valueOf(des_latitude_plic));  // CameraOverlayview 에 목적지값 전송 - cameraActivity 통해서 경유
                intent.putExtra("longitude_id", String.valueOf(des_longitude_plic));
                startActivity(intent);

            } else if (v.getId() == R.id.road) {
                List<Address> des_location = null;
                String query = my_destination.getText().toString().replace("[", "").replace("]", "").replace(")", "").replace("(", "").trim();
                if (query == null || query.length() == 0) {
                    showToast("검색어가 입력되지 않았습니다.");
                    return;
                }

                try {    //  도착위치 값 지오코딩.
                    des_location = coder.getFromLocationName(query, 5);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "입출력오류 :" + e.getMessage() + "", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                des_latitude_plic = des_location.get(0).getLatitude();
                des_longitude_plic = des_location.get(0).getLongitude();

                naviGuide();
                drawPedestrianPath();
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapContainer = (RelativeLayout) findViewById(R.id.Tmap);

        mMapView = new TMapView(this);
        mapContainer.addView(mMapView);
        mMapView.setSKPMapApiKey("17a82275-ae41-30b6-8ad4-67f929b71596");
        mMapView.setLanguage(TMapView.LANGUAGE_KOREAN);
        mMapView.setMapType(TMapView.MAPTYPE_STANDARD);
        mMapView.setIconVisibility(true);
        mMapView.setZoomLevel(17);
        mMapView.MapZoomIn();
        mMapView.MapZoomOut();
        my_location = (EditText) findViewById(R.id.start_edit);
        my_location.setEnabled(false);
        my_destination = (EditText) findViewById(R.id.destination_edit); // 검색창
        my_destination.setEnabled(false);
        Search = (Button) findViewById(R.id.search); // 검색버튼
        roadservice = (Button) findViewById(R.id.road); // 길찾기버튼
        VR = (Button) findViewById(R.id.VR);  // VR버튼
        mContext = this;
        Intent intent = getIntent();
        my_destination.setText(intent.getStringExtra("des_info"));
        mOverlayview = new CameraOverlayview(this);
        coder = new Geocoder(getApplicationContext(), Locale.KOREA);

        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.poi_star);
        mMapView.setIcon(bitmap);

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {

                latitude_plic = location.getLatitude();
                longitude_plic = location.getLongitude();
                mOverlayview.setCurrentPoint(latitude_plic, longitude_plic);  // 현재위치 업데이트를 위해 mOverlayview에 값 전송

                my_location.setText("현 위치");
                mMapView.setCenterPoint(longitude_plic, latitude_plic);
                mMapView.setLocationPoint(longitude_plic, latitude_plic);
                mMapView.setTrackingMode(true);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 3, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 3, locationListener);
        MyListenerClass buttonListener = new MyListenerClass();
        Search.setOnClickListener(buttonListener);
        roadservice.setOnClickListener(buttonListener);
        VR.setOnClickListener(buttonListener);
    }







    public void naviGuide() {
        TMapPoint point1 = new TMapPoint(latitude_plic, longitude_plic);
        TMapPoint point2 = new TMapPoint(des_latitude_plic, des_longitude_plic);

        TMapData tmapdata = new TMapData();

        tmapdata.findPathDataAllType(TMapData.TMapPathType.PEDESTRIAN_PATH,point1, point2, new TMapData.FindPathDataAllListenerCallback(){
            @Override
            public void onFindPathDataAll(Document doc) {
                doc.getDocumentElement().normalize();
                Element root = doc.getDocumentElement();

                int length = root.getElementsByTagName("Placemark").getLength();
                for(int i=0; i<length; i++) {
                    Node placemark = root.getElementsByTagName("Placemark").item(i);

                    Node tmapindex
                            = ((Element)placemark).getElementsByTagName("tmap:index").item(0);

                    Log.e
                            ("PARSER", "tmap Index =" + tmapindex.getTextContent());

                    Node nodeType
                            = ((Element)placemark).getElementsByTagName("tmap:nodeType").item(0);
                    Log.e
                            ("PARSER", "nodeType =" + nodeType.getTextContent());

                    Node coordinate = ((Element)placemark).getElementsByTagName("coordinates").item(0);
                    Log.e
                            ("PARSER", "coordinate =" + coordinate.getTextContent());


                    if(nodeType.getTextContent().equals("POINT")) {
                        Node turnType
                                = ((Element) placemark).getElementsByTagName("tmap:turnType").item(0);
                        String a = turnType.getTextContent();
                        Log.e
                                ("PARSER", "point 지점에서 =" + Turntype(a));
                    }

                    Log.e
                            ("PARSER", " ");

                }

            }
        });
    }

    public String Turntype(String c)
    {
        switch (c)
        {
            case "11" : c="직진"; break;
            case "12" : c="좌회전"; break;
            case "13" : c="우회전"; break;
            case "14" : c="U-turn"; break;
            case "16" : c="8시 방향 좌회전"; break;
            case "17" : c="10방향 좌회전"; break;
            case "18" : c="2시 방향 우회전"; break;
            case "19" : c="4시 방향 우회전"; break;
            case "184" : c="첫번째 경유지"; break;
            case "186" : c="두번째 경유지"; break;
            case "187" : c="세번째 경유지"; break;
            case "188" : c="네번째 경유지"; break;
            case "189" : c="다섯번째 경유지"; break;
            case "125" : c="육교"; break;
            case "126" : c="지하보도"; break;
            case "127" : c="계단 진입"; break;
            case "128" : c="경사로 진입"; break;
            case "129" : c="계단 + 경사로 진입"; break;
            case "200" : c="출발지"; break;
            case "201" : c="목적지"; break;
            case "211" : c="횡단보도"; break;
            case "212" : c="좌측 횡단보도"; break;
            case "213" : c="우측 횡단보도"; break;
            case "214" : c="8시 방향 횡단보도"; break;
            case "215" : c="10시 방향 횡단보도"; break;
            case "216" : c="2시 방향 횡단보도"; break;
            case "217" : c="4시 방향 횡단보도"; break;
            case "218" : c="엘리베이터"; break;
        }


        return c;
    }



    public void findAllPoi() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Guide road Search part,,");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("검색", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String strData = input.getText().toString();
                TMapData tmapdata = new TMapData();

                tmapdata.findAllPOI(strData, new TMapData.FindAllPOIListenerCallback() {
                    @Override
                    public void onFindAllPOI(ArrayList<TMapPOIItem> poiItem) {
                        Intent intent = new Intent(MainActivity.this, Listview.class);
                        ArrayList<String> search_list = new ArrayList<String>();

                        for (int i = 0; i < poiItem.size(); i++) {
                            TMapPOIItem item = poiItem.get(i);
                            String[] address = new String[poiItem.size()];
                            address[i] = String.valueOf(item.getPOIName().toString());
                            search_list.add(address[i]);
                            intent.putExtra("address", search_list);
                        }
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void drawPedestrianPath() {
        TMapPoint point1 = new TMapPoint(latitude_plic, longitude_plic);
        TMapPoint point2 = new TMapPoint(des_latitude_plic, des_longitude_plic);

        TMapData tmapdata = new TMapData();
        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, point1, point2, new TMapData.FindPathDataListenerCallback() {

            public void onFindPathData(TMapPolyLine polyLine) {
                polyLine.setLineColor(Color.BLUE);
                mMapView.addTMapPath(polyLine);
            }
        });


    }


    private void showToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}



