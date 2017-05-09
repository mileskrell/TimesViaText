package com.example.mmkrell.timesviatext;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

class CTAHelper extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "cta.db";
    private static final int DATABASE_VERSION = 1;

    // This is static so that only one instance needs to exist throughout the app
    private static SQLiteDatabase databaseInstance;

    CTAHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    static void setDatabaseInstance(SQLiteDatabase databaseInstance) {
        CTAHelper.databaseInstance = databaseInstance;
    }

    static SQLiteDatabase getDatabaseInstance() {
        return databaseInstance;
    }
}
