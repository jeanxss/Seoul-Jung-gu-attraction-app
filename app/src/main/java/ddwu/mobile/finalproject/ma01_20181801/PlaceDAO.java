package ddwu.mobile.finalproject.ma01_20181801;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PlaceDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertPlace(Place place);

    @Update
    int updatePlace(Place place);

    @Delete
    int deletePlace(Place place);

    @Query("SELECT * FROM place_table")
    List<Place> getAllPlaces();

    @Query("SELECT * FROM place_table WHERE _id = :id")
    Place getPlaceById(int id);

    @Query("SELECT * FROM place_table WHERE title = :title")
    Place getPlaceByTitle(String title);

}

