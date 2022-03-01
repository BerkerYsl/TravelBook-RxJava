package com.berkeruysal.travelbookotlin.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.berkeruysal.travelbookotlin.R
import com.berkeruysal.travelbookotlin.adapter.PlaceAdapter
import com.berkeruysal.travelbookotlin.databinding.ActivityMainBinding
import com.berkeruysal.travelbookotlin.model.Place
import com.berkeruysal.travelbookotlin.roomdb.PlaceDatabase
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers


import java.lang.ClassCastException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val compositeDisposable= CompositeDisposable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)

        //veri tabanını oluşturuyoruz aktivite için ve verileri çekiyoruz göstermek için

        try {
            val db=Room.databaseBuilder(applicationContext,PlaceDatabase::class.java,"Places").build()
            val placeDao=db.placeDao()
            compositeDisposable.add(
                placeDao.getAll().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(this::handleResponse)
            )
        }
        catch (e:ClassCastException)
        {
            e.printStackTrace()
        }

    }
    //bu metodun liste döndürmesi lazım çünkü artık veriler gösterilecek ve getall'da veriler var.
    private fun handleResponse(placelist:List<Place>)
    {
        binding.recyclerView.layoutManager=LinearLayoutManager(this)
        val adapter=PlaceAdapter(placelist)
        binding.recyclerView.adapter=adapter

    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.place_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId== R.id.add_place)
        {
            val intent=Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }
}