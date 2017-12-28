package com.ozmar.notes.di;


import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;

import com.ozmar.notes.SharedPreferencesHelper;
import com.ozmar.notes.database.AppDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


@Module
public class AppModule {

    @Provides
    Application provideApplication(App application) {
        return application;
    }

    @Provides
    Context provideContext(App application) {
        return application.getApplicationContext();
    }


    @Singleton
    @Provides
    AppDatabase provideDatabase(App application) {
        return Room.databaseBuilder(application, AppDatabase.class, "notes-database")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Singleton
    @Provides
    SharedPreferencesHelper providePreferencesHelper(App application) {
        return new SharedPreferencesHelper(application);
    }
}
