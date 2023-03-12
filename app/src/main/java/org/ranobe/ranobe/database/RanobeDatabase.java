package org.ranobe.ranobe.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import org.ranobe.ranobe.App;
import org.ranobe.ranobe.config.Ranobe;
import org.ranobe.ranobe.database.converters.ListConverter;
import org.ranobe.ranobe.database.dao.NovelDao;
import org.ranobe.ranobe.models.Novel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Novel.class}, version = Ranobe.DATABASE_VERSION)
@TypeConverters({ListConverter.class})
public abstract class RanobeDatabase extends RoomDatabase {
    public static final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private static volatile RanobeDatabase INSTANCE;

    public static RanobeDatabase database() {
        if (INSTANCE == null) {
            synchronized (RanobeDatabase.class) {
                if (INSTANCE != null) return INSTANCE;

                INSTANCE = Room.databaseBuilder(
                        App.getContext().getApplicationContext(),
                        RanobeDatabase.class,
                        Ranobe.DATABASE_NAME
                ).build();
            }
        }
        return INSTANCE;
    }

    // tables
    public abstract NovelDao novels();
}
