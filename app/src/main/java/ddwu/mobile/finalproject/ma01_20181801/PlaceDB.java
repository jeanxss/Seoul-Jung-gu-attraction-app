package ddwu.mobile.finalproject.ma01_20181801;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Place.class}, version=1)
public abstract class PlaceDB extends RoomDatabase {
    public abstract PlaceDAO placeDAO();

    private static volatile PlaceDB INSTANCE;

    static PlaceDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (PlaceDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    PlaceDB.class, "place_db.db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
