package com.example.mmkrell.timesviatext;


import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by Miles on 1/4/2017.
 */

class GTFSHelper extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "gtfs.db";
    private static final int DATABASE_VERSION = 1;

    GTFSHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
}
