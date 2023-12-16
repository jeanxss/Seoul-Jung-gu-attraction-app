package ddwu.mobile.finalproject.ma01_20181801;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


public class MyPlaceActivity extends AppCompatActivity {

    public static final String TAG = "MyPlaceActivity";

    ListView lvList;

    MyPlaceAdapter adapter;
    ArrayList<MyPlace> resultList;

    ImageFileManager imgManager;

    MyPlaceDB myPlaceDB;
    MyPlaceDAO myPlaceDAO;

    private final CompositeDisposable mDisposable = new CompositeDisposable();

    List<MyPlace> lMyPlaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myplace);

        lvList = findViewById(R.id.lvList2);

        resultList = new ArrayList();
        adapter = new MyPlaceAdapter(this, R.layout.listview_place, resultList);
        lvList.setAdapter(adapter);

        imgManager = new ImageFileManager(this);

        myPlaceDB = MyPlaceDB.getDatabase(this);
        myPlaceDAO = myPlaceDB.myplaceDAO();

        //추가
        Flowable<List<MyPlace>> resultMyPlaces = myPlaceDAO.getAllMyPlaces();

        mDisposable.add( resultMyPlaces
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(myPlaces -> {
                            for (MyPlace myPlace : myPlaces) {
                                lMyPlaces = myPlaces;
                                Log.d("getAllMyPlaces", myPlace.toString());
                            }
                            resultList.clear();
                            resultList.addAll(myPlaces);
                            adapter.notifyDataSetChanged();
                        },
                        throwable -> Log.d("getAllMyPlaces", "error", throwable)) );


        /*lvList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int i, long l) {
                Log.d(TAG, "id: " + lvList.getAdapter().getItem(i));

                MyPlace deletePlace = (MyPlace) lvList.getAdapter().getItem(i);

                Completable deleteResult = myPlaceDAO.deleteMyPlace(deletePlace);
                mDisposable.add(deleteResult
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> Log.d(TAG, "Delete success: "),
                                throwable -> Log.d(TAG, "error")) );

                return true;
            }
        });*/

        lvList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                final int pos = position;
                AlertDialog.Builder builder = new AlertDialog.Builder(MyPlaceActivity.this);
                builder.setTitle("My Attraction 삭제")
                        .setMessage(resultList.get(pos).getTitle() + " 장소를 삭제하시겠습니까?")
                        .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MyPlace deletePlace = (MyPlace) lvList.getAdapter().getItem(position);

                                Completable deleteResult = myPlaceDAO.deleteMyPlace(deletePlace);
                                mDisposable.add(deleteResult
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(() -> Log.d(TAG, "Delete success: "),
                                                throwable -> Log.d(TAG, "error")) );
                            }
                        })
                        .setNegativeButton("취소", null)
                        .setCancelable(false)
                        .show();
                return true;
            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        imgManager.clearSaveFilesOnInternal();
        mDisposable.clear();
    }


}
