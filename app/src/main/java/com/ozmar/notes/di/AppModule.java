package com.ozmar.notes.di;


import android.arch.persistence.room.Room;
import android.content.Context;

import com.ozmar.notes.database.AppDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


@Module
public class AppModule {

    @Provides
    Context provideContext(App application) {
        return application.getApplicationContext();
    }


    @Singleton
    @Provides
    AppDatabase provideDatabase(App mApplication) {
        return Room.databaseBuilder(mApplication, AppDatabase.class, "notes-database")
                .fallbackToDestructiveMigration()
                .build();
    }
}
