package ddwu.mobile.finalproject.ma01_20181801;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    Place lPlace;
    TextView mkTitle;
    TextView mkAddr;
    ImageView mkImage;
    Place mkPlace;

    //Parsing
    //EditText etTarget;
    ListView lvList;
    String apiAddress;

    String query;

    PlaceAdapter adapter;
    ArrayList<Place> resultList;
    PlaceXmlParser parser;
    ImageFileManager imgManager;

    //DB
    PlaceDB placeDB;
    PlaceDAO placeDAO;
    MyPlaceDB myPlaceDB;
    MyPlaceDAO myPlaceDAO;

    private final CompositeDisposable mDisposable = new CompositeDisposable();

    //구글맵
    final int REQ_PERMISSION_CODE = 100;

    TextView tvText;

    FusedLocationProviderClient flpClient;
    Location mLastLocation;

    private GoogleMap mGoogleMap;       // 지도 객체
    private Marker mCenterMarker;         // 중앙 표시 Marker
    private List<Marker> markerList;
    private Marker mPoiMarker;
    private Polyline mPolyline;
    private Marker currentMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mkTitle = findViewById(R.id.mkTitle);
        mkAddr = findViewById(R.id.mkAddr);
        mkImage = findViewById(R.id.mkImage);

        //etTarget = findViewById(R.id.etTarget);
        lvList = findViewById(R.id.lvList2);

        //DB
        placeDB = PlaceDB.getDatabase(this);
        placeDAO = placeDB.placeDAO();
        myPlaceDB = MyPlaceDB.getDatabase(this);
        myPlaceDAO = myPlaceDB.myplaceDAO();
        //

        resultList = new ArrayList();
        adapter = new PlaceAdapter(this, R.layout.listview_place, resultList);
        lvList.setAdapter(adapter);

        //apiAddress = getResources().getString(R.string.api_url);
        apiAddress = "http://apis.data.go.kr/" +
                "B551011/KorService/areaBasedList" +
                "?numOfRows=100&MobileOS=ETC&MobileApp=AppTest&ServiceKey=J%2Bn0sQmI26t%2FkmZvkOF3A0B2nmf27F4YwvTTpH2GCH0VcnZMCTHOFeEKhjKGlscyCYerYbL8%2F0IEsnSnVYWVnA%3D%3D&listYN=Y&arrange=O&contentTypeId=12&areaCode=1" +
                "&sigunguCode=";
        parser = new PlaceXmlParser();
        imgManager = new ImageFileManager(this);

        //<구글맵>
        //tvText = findViewById(R.id.tvText);

        //parser = new FakeParser();      // 모의 parser 생성

        flpClient = LocationServices.getFusedLocationProviderClient(this);

        markerList = new ArrayList<>();

        SupportMapFragment mapFragment      // map 객체 생성
                = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(mapReadyCallback);

        //<위치>
        checkPermission();
        //위치 확인 시작
        flpClient.requestLocationUpdates(
                getLocationRequest(),
                mLocCallback,
                Looper.getMainLooper()
        );
        //마지막 위치 확인
        //getLastLocation();

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (placeDAO.getPlaceById(1) != null) {
                    lPlace = placeDAO.getPlaceById(1);
                    Log.d("첫번째 title", lPlace.getTitle());
                    Log.d("첫번째 place", lPlace.toString());
                }
                else
                    Log.d("첫번째 place", "null");
            }
        }).start();

        lvList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                /* 작성할 부분 */
                /*롱클릭한 항목의 이미지 주소를 가져와 내부 메모리에 지정한 이미지 파일을 외부저장소로 이동
                 * ImageFileManager 의 이동 기능 사용
                 * 이동을 성공할 경우 파일 명, 실패했을 경우 null 을 반환하므로 해당 값에 따라 Toast 출력*/
                /*Place dto = resultList.get(position);

                if (dto.getImageFileName() != null) {       // 외부저장소에 저장한 파일명이 기록되어 있을 경우
                    Toast.makeText(MainActivity.this, "Already moved!", Toast.LENGTH_SHORT).show();
                } else {
                    String savedName = imgManager.moveStorage(dto.getFirstimage());
                    Log.i(TAG, "Saved file name: " + savedName);
                    if (savedName != null) {
                        dto.setImageFileName(savedName);    // 외부저장소에 저장한 파일명을 dto 에 저장
                        Toast.makeText(MainActivity.this, savedName + " is saved to Ext.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Save failure!", Toast.LENGTH_SHORT).show();
                    }
                }*/

                // Glide 를 사용하여 이미지 파일을 외장메모리에 저장
                // 해당 부분은 파일매니저로 분리 필요

                Glide.with(MainActivity.this)
                        .asBitmap()
                        .load(resultList.get(position).getFirstimage())
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                                // 파일 처리 클래스로 분리 필요

                                if (isExternalStorageWritable()) {
                                    File file = new File (getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                                            "myalbum");
                                    if (!file.mkdirs()) {
                                        Log.d(TAG, "directory not created");
                                    }
                                    File saveFile = new File(file.getPath(), "test.jpg");
                                    try {
                                        FileOutputStream fos = new FileOutputStream((saveFile));
                                        resource.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                        fos.flush();
                                        fos.close();
                                        Toast.makeText(MainActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
                                    } catch (IOException e) { e.printStackTrace(); }
                                }
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });

                //my attraction list에 저장
                try {
                    Place myListPlace = new mkTask().execute(resultList.get(position).getTitle()).get();
                    Log.d("myListPlace", myListPlace.toString());
                    //new insertTask().execute(myListPlace);
                    Single<Long> insertResult = myPlaceDAO.insertMyPlace(new MyPlace(
                            myListPlace.get_id(),
                            myListPlace.getAddr1(),
                            myListPlace.getAddr2(),
                            myListPlace.getFirstimage(),
                            myListPlace.getFirstimage2(),
                            myListPlace.getMapx(),
                            myListPlace.getMapy(),
                            myListPlace.getSigungucode(),
                            myListPlace.getTitle(),
                            myListPlace.getImageFileName()
                    ));

                    mDisposable.add (
                            insertResult.subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(result -> Log.d(TAG, "Insertion success: " + result),
                                            throwable -> Log.d(TAG, "error"))   );
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return true;
            }
        });

    }


    private boolean isExternalStorageWritable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }


    OnMapReadyCallback mapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            mGoogleMap = googleMap;

//            지도 초기 위치 이동
            LatLng latLng = new LatLng(37.5675596477, 126.9765267272);
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));

//            지도 중심 마커 추가
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title("현위치")
                    .snippet("이동중");

            mCenterMarker = mGoogleMap.addMarker(markerOptions);
            mCenterMarker.showInfoWindow();

            // 마커 클릭 이벤트
            mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(@NonNull Marker lMarker) {
                    currentMarker = lMarker;
                    Log.d("currentMarker.getTitle()", currentMarker.getTitle());

                    //마커 클릭 시 화면에 출력
                    if (!currentMarker.getTitle().equals("현위치")) {
                        try {
                            Place markerPlace = new mkTask().execute(currentMarker.getTitle()).get();
                            //Log.d("markerPlace.toString()", markerPlace.toString());
                            if (markerPlace != null) {
                                mkTitle.setText(markerPlace.getTitle());
                                mkAddr.setText(markerPlace.getAddr1() + " " + markerPlace.getAddr2());
                                Log.d("markerPlace.getFirstimage()", markerPlace.getFirstimage());
                                Glide.with(MainActivity.this)
                                        .load(markerPlace.getFirstimage())
                                        .into(mkImage);
                            } else {
                                Log.d("markerPlace", "null");
                            }
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            });

        }
    };


    class mkTask extends AsyncTask<String, Void, Place> {
        @Override
        protected Place doInBackground(String... inputParams) {
            Place mkTaskPlace = null;
            String mkTitle = inputParams[0];
            if (placeDAO.getPlaceByTitle(mkTitle) != null) {
                mkTaskPlace = placeDAO.getPlaceByTitle(mkTitle);
                Log.d("marker.getTitle()", mkTitle);
                Log.d("place.toString()", mkTaskPlace.toString());
            }
            return mkTaskPlace;
        }
    }

    
    LocationCallback mLocCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            for (Location loc : locationResult.getLocations()) {
                double lat = loc.getLatitude();
                double lng = loc.getLongitude();
                //setTvText(String.format("(%.6f, %.6f)", lat, lng));

//                지도 위치 이동
                mLastLocation = loc;
                //LatLng currentLoc = new LatLng (lat, lng);
                LatLng currentLoc = new LatLng(37.563177, 126.9873604); //명동성당
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 13));

//                지도 마커 위치 이동
                mCenterMarker.setPosition(currentLoc);

//                지도 선을 그리기 위한 지점(위도/경도) 추가
//                List<LatLng> latLngs = mPolyline.getPoints();
//                latLngs.add(currentLoc);
//                mPolyline.setPoints(latLngs);
            }
        }
    };


    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        imgManager.clearSaveFilesOnInternal();
        //Rxjava
        mDisposable.clear();
    }

    @Override
    protected void onPause() {
        super.onPause();
        flpClient.removeLocationUpdates(mLocCallback);
    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnSearch:
                //Parsing
                //query = etTarget.getText().toString();
                query = "24";
                try {
                    new PlaceAsyncTask().execute(apiAddress + URLEncoder.encode(query, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                if (resultList != null) {
                    adapter.setList(resultList);
                    adapter.notifyDataSetChanged();
                }

                break;
            case R.id.btnList:
                Intent intent = new Intent(MainActivity.this, MyPlaceActivity.class);
                startActivity(intent);
                break;
        }
    }


    //위치
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION_CODE:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "위치권한 획득 완료", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "위치권한 미획득", Toast.LENGTH_SHORT).show();
                }
        }
    }


    private void checkPermission() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            // 권한이 있을 경우 수행할 동작
            Toast.makeText(this,"Permissions Granted", Toast.LENGTH_SHORT).show();
        } else {
            // 권한 요청
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, REQ_PERMISSION_CODE);
        }
    }


    private void getLastLocation() {
        checkPermission();
        flpClient.getLastLocation().addOnSuccessListener(
                new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            //setTvText( String.format("최종위치: (%.6f, %.6f)", latitude, longitude) );
                            mLastLocation = location;
                        } else {
                            Toast.makeText(MainActivity.this, "No location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        flpClient.getLastLocation().addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Unknown");
                    }
                }
        );

    }


    //openApi parsing
    class PlaceAsyncTask extends AsyncTask<String, Integer, String> {
        ProgressDialog progressDlg;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDlg = ProgressDialog.show(MainActivity.this, "Wait", "Downloading...");
        }

        @Override
        protected String doInBackground(String... strings) {
            String address = strings[0];
            String result = downloadContents(address);
            if (result == null) return "Error!";
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, result);

            resultList = parser.parse(result);      // 파싱 수행

            adapter.setList(resultList);    // Adapter 에 파싱 결과를 담고 있는 ArrayList 를 설정
            adapter.notifyDataSetChanged();

            progressDlg.dismiss();

            LatLng poiLatLng;

            for (int i = 0; i < resultList.size(); i++) {
//              poiList 의 POI 로 마커 추가 기능 수행
                //POI의 마커 정보 지정
                poiLatLng = new LatLng(Double.parseDouble(resultList.get(i).getMapy()), Double.parseDouble(resultList.get(i).getMapx()));
                MarkerOptions poiMarkerOptions = new MarkerOptions();
                poiMarkerOptions.position(poiLatLng);
                poiMarkerOptions.title(resultList.get(i).getTitle());
                poiMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                //지도에 마커 추가 후 추가한 마커 정보 기록
                mPoiMarker = mGoogleMap.addMarker(poiMarkerOptions);
                mPoiMarker.setTag(resultList.get(i).get_id());
                mPoiMarker.showInfoWindow();
                //markerList.add(mPoiMarker);
                //markerList.get(i).showInfoWindow();
            }

            //db에 Place insert
            if (lPlace == null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for(int i = 0; i < resultList.size(); i++) {
                            Log.d("resultList.get(i) 출력 : ", resultList.get(i).toString());

                            long result = placeDAO.insertPlace(new Place(
                                    resultList.get(i).getAddr1(),
                                    resultList.get(i).getAddr2(),
                                    resultList.get(i).getFirstimage(),
                                    resultList.get(i).getFirstimage2(),
                                    resultList.get(i).getMapx(),
                                    resultList.get(i).getMapy(),
                                    resultList.get(i).getSigungucode(),
                                    resultList.get(i).getTitle()
                            ));
                            Log.d(TAG, "Insert Result: " + result);
                            lPlace = resultList.get(0);     // 필요
                        }
                    }
                }).start();
            } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (placeDAO.getAllPlaces() != null) {
                            List<Place> placeList = placeDAO.getAllPlaces();
                            for (Place place : placeList) {
                                Log.d("placeDAO.getAllPlaces()", place.toString());
                            }
                        }
                    }
                }).start();
            }

        }

        /* 네트워크 관련 메소드 */
        /* 네트워크 환경 조사 */
        private boolean isOnline() {
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            return (networkInfo != null && networkInfo.isConnected());
        }

        /* URLConnection 을 전달받아 연결정보 설정 후 연결, 연결 후 수신한 InputStream 반환
         * 네이버용을 수정 - ClientID, ClientSeceret 추가 strings.xml 에서 읽어옴*/
        private InputStream getNetworkConnection(HttpURLConnection conn) throws Exception {

            // 클라이언트 아이디 및 시크릿 그리고 요청 URL 선언
            conn.setReadTimeout(3000);
            conn.setConnectTimeout(3000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setRequestProperty("X-Naver-Client-Id", getResources().getString(R.string.client_id));
            conn.setRequestProperty("X-Naver-Client-Secret", getResources().getString(R.string.client_secret));

            if (conn.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + conn.getResponseCode());
            }

            return conn.getInputStream();
        }

        /* InputStream을 전달받아 문자열로 변환 후 반환 */
        protected String readStreamToString(InputStream stream) {
            StringBuilder result = new StringBuilder();

            try {
                InputStreamReader inputStreamReader = new InputStreamReader(stream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String readLine = bufferedReader.readLine();

                while (readLine != null) {
                    result.append(readLine + "\n");
                    readLine = bufferedReader.readLine();
                }

                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result.toString();
        }

        /* 주소(address)에 접속하여 문자열 데이터를 수신한 후 반환 */
        protected String downloadContents(String address) {
            HttpURLConnection conn = null;
            InputStream stream = null;
            String result = null;

            try {
                URL url = new URL(address);
                conn = (HttpURLConnection) url.openConnection();
                stream = getNetworkConnection(conn);
                result = readStreamToString(stream);
                if (stream != null) stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn != null) conn.disconnect();
            }

            return result;
        }

    }

    
    /*class insertTask extends AsyncTask<Place, Void, Void> {
        @Override
        protected Void doInBackground(Place... places) {
            if (places[0] != null) {
                MyPlace myPlace = new MyPlace(
                        places[0].getAddr1(),
                        places[0].getAddr2(),
                        places[0].getFirstimage(),
                        places[0].getFirstimage2(),
                        places[0].getMapy(),
                        places[0].getMapy(),
                        places[0].getSigungucode(),
                        places[0].getTitle()
                        );
                long result = myPlaceDAO.insertMyPlace(myPlace);
                Log.d(TAG, "MyPlace Insert Result: " + result);
            }
            return null;
        }
    }*/

    /*void findPlaceByTitle(String title) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (placeDAO.getPlaceByTitle(title) != null) {
                    Place place = placeDAO.getPlaceByTitle(title);
                    Log.d("marker.getTitle()", title);
                    Log.d("place.toString()", place.toString());
                    mkPlace = place;
                }
            }
        }).start();
    }*/

    /*Consumer<Long> consumer = new Consumer<Long>() {
            @Override
            public void accept(Long insertionResult) {
                Log.d(TAG, "Insertioin success: " + insertionResult);
            }
        };*/

    /*private void setTvText(String text) {
        String before = System.getProperty("line.separator") + tvText.getText();
        tvText.setText(text + before);
    }*/


}



/*
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnSearch:
                //Parsing
                //query = etTarget.getText().toString();
                query = "24";
                try {
                    new PlaceAsyncTask().execute(apiAddress + URLEncoder.encode(query, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                //DB
//                for (int i=0; i<resultList.size(); i++) {
//                    //db에 Place insert
//                    Single<Long> insertResult = placeDAO.insertPlace(new Place(
//                            resultList.get(i).getAddr1(),
//                            resultList.get(i).getAddr2(),
//                            resultList.get(i).getFirstimage(),
//                            resultList.get(i).getFirstimage2(),
//                            resultList.get(i).getMapx(),
//                            resultList.get(i).getMapy(),
//                            resultList.get(i).getSigungucode(),
//                            resultList.get(i).getTitle()
//                    ));
//
//                    mDisposable.add(
//                            insertResult.subscribeOn(Schedulers.io())
//                                    .observeOn(AndroidSchedulers.mainThread())
//                                    .subscribe(insertionResult -> Log.d(TAG, "Insertion success: " + insertionResult),
//                                            throwable -> Log.d(TAG, "error")));
//                }

//                Flowable<List<Place>> resultPlaces = placeDAO.getAllPlaces();
//                mDisposable.add( resultPlaces
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(places -> {
//                                    for (Place place : places) {
//                                        Log.d(TAG, place.toString());
//                                    }
//
//                                },
//                                throwable -> Log.d(TAG, "error", throwable)) );

                */
/*new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (placeDAO.getPlaceById(1) != null) {
                            place = placeDAO.getPlaceById(1);
                            Log.d("첫번째 타이틀", place.getTitle());
                            Log.d("첫번째 place", place.toString());
                        }
                        else
                            Log.d("첫번째 place", "null");
                    }
                }).start();

                for (i = 0; i < resultList.size(); i++) {
                    //db에 Place insert
                    if (place == null) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                long result = placeDAO.insertPlace(new Place(
                                        resultList.get(i).getAddr1(),
                                        resultList.get(i).getAddr2(),
                                        resultList.get(i).getFirstimage(),
                                        resultList.get(i).getFirstimage2(),
                                        resultList.get(i).getMapx(),
                                        resultList.get(i).getMapy(),
                                        resultList.get(i).getSigungucode(),
                                        resultList.get(i).getTitle()
                                ));
                                Log.d(TAG, "Insert Result: " + result);
                            }
                        }).start();

                    }
                }*//*


                break;
            case R.id.btnList:
                Intent intent = new Intent(MainActivity.this, MyPlaceActivity.class);
                startActivity(intent);
                break;
        }
    }*/


/*
    @Override
    protected void onPostExecute(String result) {
        Log.i(TAG, result);

        resultList = parser.parse(result);      // 파싱 수행

        adapter.setList(resultList);    // Adapter 에 파싱 결과를 담고 있는 ArrayList 를 설정
        adapter.notifyDataSetChanged();

        progressDlg.dismiss();

        LatLng poiLatLng;

        //place_db.db에 Place insert
        //list of place의 마커 추가

//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    place = placeDAO.getPlaceByTitle(resultList.get(0).getTitle());
//                    Log.d("첫번째 타이틀", resultList.get(0).getTitle());
//                    Log.d("첫번째 place", place.toString());
//                }
//            }).start();

        for (int i = 0; i < resultList.size(); i++) {
            //Log.d("resultList.get(i) 출력 : ", resultList.get(i).toString());
            //db에 Place insert
                */
/*if (place == null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            long result = placeDAO.insertPlace(new Place(
                                    resultList.get(i).getAddr1(),
                                    resultList.get(i).getAddr2(),
                                    resultList.get(i).getFirstimage(),
                                    resultList.get(i).getFirstimage2(),
                                    resultList.get(i).getMapx(),
                                    resultList.get(i).getMapy(),
                                    resultList.get(i).getSigungucode(),
                                    resultList.get(i).getTitle()
                            ));
                            Log.d(TAG, "Insert Result: " + result);
                        }
                    }).start();

//                    Single<Long> insertResult = placeDAO.insertPlace(new Place(
//                            resultList.get(i).getAddr1(),
//                            resultList.get(i).getAddr2(),
//                            resultList.get(i).getFirstimage(),
//                            resultList.get(i).getFirstimage2(),
//                            resultList.get(i).getMapx(),
//                            resultList.get(i).getMapy(),
//                            resultList.get(i).getSigungucode(),
//                            resultList.get(i).getTitle()
//                    ));
//
//                    mDisposable.add(
//                            insertResult.subscribeOn(Schedulers.io())
//                                    .observeOn(AndroidSchedulers.mainThread())
//                                    .subscribe(insertionResult -> Log.d(TAG, "Insertion success: " + insertionResult),
//                                            throwable -> Log.d(TAG, "error")));

                }*//*


            //List<Address> addresses = new ReverseGeoTask().execute(poiList.get(i)).get();
            //Log.d("addresses", addresses.toString());

//              poiList 의 POI 로 마커 추가 기능 수행
            //POI의 마커 정보 지정
            poiLatLng = new LatLng(Double.parseDouble(resultList.get(i).getMapy()), Double.parseDouble(resultList.get(i).getMapx()));
            MarkerOptions poiMarkerOptions = new MarkerOptions();
            poiMarkerOptions.position(poiLatLng);
            poiMarkerOptions.title(resultList.get(i).getTitle());
            poiMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

            //지도에 마커 추가 후 추가한 마커 정보 기록
            mPoiMarker = mGoogleMap.addMarker(poiMarkerOptions);
            mPoiMarker.setTag(resultList.get(i).get_id());
            mPoiMarker.showInfoWindow();
            //markerList.add(mPoiMarker);
            //markerList.get(0).showInfoWindow();

            //마커 클릭 이벤트
                */
/*mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(@NonNull Marker marker) {
                        Toast.makeText(MainActivity.this, "마커: " +
                                mPoiMarker.getId(), Toast.LENGTH_SHORT).show();

                        //마커 클릭 시 해당 항목 찾기
                        *//*
*/
/*new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (placeDAO.getPlaceByTitle(marker.getTitle()) != null) {
                                    Place place = placeDAO.getPlaceByTitle(marker.getTitle());
                                    Log.d("marker.getTitle()", marker.getTitle());
                                    Log.d("place.toString()", place.toString());
                                }
                            }
                        }).start();
*//*
*/
/*

                    }
                });*//*


        }

//            new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (placeDAO.getPlaceById(1) != null) {
//                            lPlace = placeDAO.getPlaceById(1);
//                            Log.d("첫번째 타이틀", lPlace.getTitle());
//                            Log.d("첫번째 place", lPlace.toString());
//                        }
//                        else
//                            Log.d("첫번째 place", "null");
//                    }
//                }).start();

        //db에 Place insert
        if (lPlace == null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for(int i = 0; i < resultList.size(); i++) {
                        Log.d("resultList.get(i) 출력 : ", resultList.get(i).toString());

                        long result = placeDAO.insertPlace(new Place(
                                resultList.get(i).getAddr1(),
                                resultList.get(i).getAddr2(),
                                resultList.get(i).getFirstimage(),
                                resultList.get(i).getFirstimage2(),
                                resultList.get(i).getMapx(),
                                resultList.get(i).getMapy(),
                                resultList.get(i).getSigungucode(),
                                resultList.get(i).getTitle()
                        ));
                        Log.d(TAG, "Insert Result: " + result);
                        lPlace = resultList.get(0);     // 필요
                    }
                }
            }).start();
        }

        if (lPlace != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (placeDAO.getAllPlaces() != null) {
                        List<Place> placeList = placeDAO.getAllPlaces();
                        for (Place place : placeList) {
                            Log.d("placeDAO.getAllPlaces()", place.toString());
                        }
                    }
                }
            }).start();
        }

    }

    */
/* 네트워크 관련 메소드 *//*

    */
/* 네트워크 환경 조사 *//*

    private boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    */
/* URLConnection 을 전달받아 연결정보 설정 후 연결, 연결 후 수신한 InputStream 반환
     * 네이버용을 수정 - ClientID, ClientSeceret 추가 strings.xml 에서 읽어옴*//*

    private InputStream getNetworkConnection(HttpURLConnection conn) throws Exception {

        // 클라이언트 아이디 및 시크릿 그리고 요청 URL 선언
        conn.setReadTimeout(3000);
        conn.setConnectTimeout(3000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.setRequestProperty("X-Naver-Client-Id", getResources().getString(R.string.client_id));
        conn.setRequestProperty("X-Naver-Client-Secret", getResources().getString(R.string.client_secret));

        if (conn.getResponseCode() != HttpsURLConnection.HTTP_OK) {
            throw new IOException("HTTP error code: " + conn.getResponseCode());
        }

        return conn.getInputStream();
    }

    */
/* InputStream을 전달받아 문자열로 변환 후 반환 *//*

    protected String readStreamToString(InputStream stream) {
        StringBuilder result = new StringBuilder();

        try {
            InputStreamReader inputStreamReader = new InputStreamReader(stream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String readLine = bufferedReader.readLine();

            while (readLine != null) {
                result.append(readLine + "\n");
                readLine = bufferedReader.readLine();
            }

            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result.toString();
    }

    */
/* 주소(address)에 접속하여 문자열 데이터를 수신한 후 반환 *//*

    protected String downloadContents(String address) {
        HttpURLConnection conn = null;
        InputStream stream = null;
        String result = null;

        try {
            URL url = new URL(address);
            conn = (HttpURLConnection) url.openConnection();
            stream = getNetworkConnection(conn);
            result = readStreamToString(stream);
            if (stream != null) stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }

        return result;
    }

}*/
