package parking.group6.csc413.projectmap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * These functions are used to manage the SQLite database. Basic functionality includes adding,
 * deleting, and searching for favorite addresses.
 */
public class ConnectDB extends SQLiteOpenHelper  {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "SanFranParkIt";
    private static final String FAVORITES_TABLE = "parking";

    // Column name variables for favorites table
    public static final String ID_COL = "ID";
    public static final String ADDRESS_COL = "Address";
    public static final String LATITUDE_COL = "Latitude";
    public static final String LONGITUDE_COL = "Longitude";
    public static final String TIMES_COL = "Times";

    public ConnectDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_FAVORITES_TABLE = "CREATE TABLE " +
                FAVORITES_TABLE + "("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ADDRESS_COL+ " TEXT, "
                + LONGITUDE_COL + " DOUBLE, "
                + LATITUDE_COL + " DOUBLE, "
                + TIMES_COL + " TEXT )";
        db.execSQL(CREATE_FAVORITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FAVORITES_TABLE);
        this.onCreate(db);
    }

    public void addParking(Parking newParking) {
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(ADDRESS_COL, newParking.getAddress());
        values.put(LATITUDE_COL, newParking.getLatitude());
        values.put(LONGITUDE_COL, newParking.getLongitude());
        values.put(TIMES_COL, newParking.getTimes());

        // 3. insert values
        db.insert(FAVORITES_TABLE, null, values);

        // 4. close
        db.close();
    }

    public Parking getParking(Parking parking) {
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        // 2. build and execute query
        String query = "Select * FROM " + FAVORITES_TABLE + " WHERE "
                       + ADDRESS_COL + " =   \"" + parking.getAddress() + "\"";
        Cursor cursor = db.rawQuery(query, null);

        // 3. create an object to store the query results if found
        Parking returnParking = new Parking();
        if(cursor.moveToFirst()) {
            cursor.moveToFirst();
            parking.setAddress(cursor.getString(1));
            parking.setLatitude(Double.parseDouble(cursor.getString(2)));
            parking.setLongitude(Double.parseDouble(cursor.getString(3)));
            parking.setTimes(cursor.getString(4));
            cursor.close();
        } else {
        // else return null;
            returnParking = null;
        }
        db.close();
        return returnParking;
    }

    public void deleteParking(Parking parking) {
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. build and execute delete
        String query = "DELETE * FROM " + FAVORITES_TABLE +
                       " WHERE " + ADDRESS_COL + " =  \"" + parking.getAddress() + "\"";
        db.execSQL(query);

        // 3. close
        db.close();
    }

}
