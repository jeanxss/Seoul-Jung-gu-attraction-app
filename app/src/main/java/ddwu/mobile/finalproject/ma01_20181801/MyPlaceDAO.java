package ddwu.mobile.finalproject.ma01_20181801;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface MyPlaceDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Single<Long> insertMyPlace(MyPlace place);

/*    @Update
    Completable updateMyPlace(Place place);*/

    @Delete
    Completable deleteMyPlace(MyPlace place);

    @Query("SELECT * FROM myPlace_table")
    Flowable<List<MyPlace>> getAllMyPlaces();

/*    @Query("SELECT * FROM myPlace_table WHERE _id = :id")
    Flowable<List<MyPlace>> getMyPlaceById(int id);

    @Query("SELECT * FROM myPlace_table WHERE title = :title")
    Flowable<List<MyPlace>> getMyPlaceByTitle(String title);*/

}

