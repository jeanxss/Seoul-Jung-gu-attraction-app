package ddwu.mobile.finalproject.ma01_20181801;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {MyPlace.class}, version=1)
public abstract class MyPlaceDB extends RoomDatabase {
    public abstract MyPlaceDAO myplaceDAO();

    private static volatile MyPlaceDB INSTANCE;

    static MyPlaceDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (MyPlaceDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    MyPlaceDB.class, "myPlace_db.db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
