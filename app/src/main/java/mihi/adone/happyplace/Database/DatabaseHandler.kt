package mihi.adone.happyplace.Database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import mihi.adone.happyplace.Models.HappyPlaceModel

class DatabaseHandler(context: Context):
SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object{
        private const val DATABASE_VERSION =1
        private const val DATABASE_NAME="HappyPlaceDatabase"
        private const val TABLE_HAPPY_PLACE ="HappyPlaceTable"


        //all columns
        private const val KEY_ID = "_id"
        private const val KEY_TITLE= "title"
        private const val KEY_IMAGE = "image"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_DATE = "date"
        private const val KEY_LOCATION = "location"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"
    }
    override fun onCreate(db: SQLiteDatabase?) {
        //create table with fields

        val CREATE_HAPPY_PLACE_TABLE = ("CREATE TABLE "+ TABLE_HAPPY_PLACE +" ("
                +KEY_ID +" INTEGER PRIMARY KEY, "
                +KEY_TITLE + " TEXT, "
                +KEY_IMAGE + " TEXT, "
                +KEY_DESCRIPTION+ " TEXT, "
                +KEY_DATE+ " TEXT, "
                +KEY_LOCATION+ " TEXT, "
                +KEY_LATITUDE+ " REAL, "
                +KEY_LONGITUDE+ " REAL )")
        db?.execSQL(CREATE_HAPPY_PLACE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS " + TABLE_HAPPY_PLACE)
        onCreate(db)
    }
    fun addHappyPlace(happyPlace : HappyPlaceModel): Long {
        val db = this.writableDatabase

        // below we are creating
        // a content values variable
        val values = ContentValues()

        // we are inserting our values
        // in the form of key-value pair
        values.put(KEY_TITLE, happyPlace.id)

        values.put(KEY_TITLE, happyPlace.title)
        values.put(KEY_IMAGE, happyPlace.image)
        values.put(KEY_DESCRIPTION, happyPlace.description)
        values.put(KEY_DATE, happyPlace.date)
        values.put(KEY_LOCATION, happyPlace.location)
        values.put(KEY_LATITUDE, happyPlace.latitude)
        values.put(KEY_LONGITUDE, happyPlace.longitude)

        // here we are creating a
        // writable variable of
        // our database as we want to
        // insert value in our database

        // all values are inserted into database
        val result = db.insert(TABLE_HAPPY_PLACE, null, values)

        // at last we are
        // closing our database
        db.close()
        return result
    }

    fun getHappyPlacesList():ArrayList<HappyPlaceModel>{
        val happyPlaceList = ArrayList<HappyPlaceModel>  ()
        val selectQuery = "SELECT * FROM $TABLE_HAPPY_PLACE"
        val db = this.readableDatabase
        try{
            val cursor : Cursor = db.rawQuery(selectQuery, null)
            if(cursor.moveToFirst()){

                    val idIndex = cursor.getColumnIndex(KEY_ID)
                    val titleIndex = cursor.getColumnIndex(KEY_TITLE)
                    val imageIndex = cursor.getColumnIndex(KEY_IMAGE)
                    val descriptionIndex = cursor.getColumnIndex(KEY_DESCRIPTION)
                    val dateIndex = cursor.getColumnIndex(KEY_DATE)
                    val locationIndex = cursor.getColumnIndex(KEY_LOCATION)
                    val latitudeIndex = cursor.getColumnIndex(KEY_LATITUDE)
                    val longitudeIndex = cursor.getColumnIndex(KEY_LONGITUDE)
//                    val place = HappyPlaceModel()
//                    happyPlaceList.add(place)

                while (cursor.moveToNext()) {
                    val id = if (idIndex >= 0) cursor.getInt(idIndex) else -1
                    val title = if (titleIndex >= 0 && !cursor.isNull(titleIndex)) cursor.getString(titleIndex) else ""
                    val image = if (imageIndex >= 0 && !cursor.isNull(imageIndex)) cursor.getString(imageIndex) else ""
                    val description = if (descriptionIndex >= 0 && !cursor.isNull(descriptionIndex)) cursor.getString(descriptionIndex) else ""
                    val date = if (dateIndex >= 0 && !cursor.isNull(dateIndex)) cursor.getString(dateIndex) else ""
                    val location = if (locationIndex >= 0 && !cursor.isNull(locationIndex)) cursor.getString(locationIndex) else ""
                    val latitude = if (latitudeIndex >= 0 && !cursor.isNull(latitudeIndex)) cursor.getDouble(latitudeIndex) else 0.0
                    val longitude = if (longitudeIndex >= 0 && !cursor.isNull(longitudeIndex)) cursor.getDouble(longitudeIndex) else 0.0

                    if (id >= 0 && latitude >= 0 && longitude >= 0) {
                        val place = HappyPlaceModel(
                            id,
                            title,
                            image,
                            description,
                            date,
                            location,
                            latitude,
                            longitude
                        )
                        happyPlaceList.add(place)
                    } else {
                        // Handle invalid values for the columns
                    }
                }
            }
            cursor.close()
        }catch(e:SQLiteException){
            db.execSQL(selectQuery)
            return ArrayList()

        }
        return happyPlaceList

    }

}
