package mihi.adone.happyplace.Activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import mihi.adone.happyplace.Database.DatabaseHandler
import mihi.adone.happyplace.Models.HappyPlaceModel
import mihi.adone.happyplace.R
import mihi.adone.happyplace.adaptor.HappyPlacesAdapter


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val fabAddHappyPlace: FloatingActionButton = findViewById(R.id.fabAddHappyPlace)
        fabAddHappyPlace.setOnClickListener {
            val intent = Intent(this, AddHappyPlaceActivity::class.java)
            startActivityForResult(intent,ADD_PLACE_ACTIVITY_REQUEST)
        }
        getHappyPlacesListFromLocalDB()

    }

    @SuppressLint("CutPasteId")
    private fun setupHappyPlacesRecyclerView(happyPlacesList: ArrayList<HappyPlaceModel>) {

        findViewById<RecyclerView>(R.id.recyclerView).layoutManager = LinearLayoutManager(this)
        findViewById<RecyclerView>(R.id.recyclerView).setHasFixedSize(true)

        val placesAdapter = HappyPlacesAdapter(this, happyPlacesList)
        findViewById<RecyclerView>(R.id.recyclerView).adapter = placesAdapter
    }

    private fun getHappyPlacesListFromLocalDB() {

        val dbHandler = DatabaseHandler(this)

        val getHappyPlacesList = dbHandler.getHappyPlacesList()

        if (getHappyPlacesList.size > 0) {
            findViewById<RecyclerView>(R.id.recyclerView).visibility = View.VISIBLE
            findViewById<TextView>(R.id.tv_no_records_available).visibility = View.GONE
            setupHappyPlacesRecyclerView(getHappyPlacesList)
        } else {
            findViewById<RecyclerView>(R.id.recyclerView).visibility = View.GONE
            findViewById<TextView>(R.id.tv_no_records_available).visibility = View.VISIBLE
        }
        // END
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==ADD_PLACE_ACTIVITY_REQUEST){
            if(requestCode== Activity.RESULT_OK){
                getHappyPlacesListFromLocalDB()
            }else{
                Log.e("Activity","Cancelled or back pressed")
            }
        }
    }
    companion object{
        var ADD_PLACE_ACTIVITY_REQUEST=1
    }
}