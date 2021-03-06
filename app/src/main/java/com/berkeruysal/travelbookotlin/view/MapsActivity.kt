package com.berkeruysal.travelbookotlin.view

import com.berkeruysal.travelbookotlin.databinding.ActivityMapsBinding
import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.berkeruysal.travelbookotlin.R
import com.berkeruysal.travelbookotlin.model.Place
import com.berkeruysal.travelbookotlin.roomdb.PlaceDao
import com.berkeruysal.travelbookotlin.roomdb.PlaceDatabase
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var sharedPreferences: SharedPreferences
    private var trackBoolean:Boolean?=null
    private var selectedLatitude:Double?=null
    private var selectedLongtitude:Double?=null
    private lateinit var db:PlaceDatabase
    private lateinit var placeDao: PlaceDao
    val compositeDisposable=CompositeDisposable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        registerLauncher()
        sharedPreferences=this.getSharedPreferences("com.berkeruysal.kotlintravelbook", MODE_PRIVATE)
        trackBoolean=false
        selectedLatitude=0.0
        selectedLongtitude=0.0
        db=Room.databaseBuilder(applicationContext,PlaceDatabase::class.java,"Places")
        //allowMainThreadQueries()
            .build() //burada verdi??imiz Places ismi heryerde ayn?? olmal??
        placeDao=db.placeDao()

    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(this)
        //Any? olarak d??nd?????? i??in getSystemService t??r?? onu LocationManager'a cast etmemiz gerekli.
        locationManager= this.getSystemService(LOCATION_SERVICE) as LocationManager
        locationListener= object:LocationListener {
            override fun onLocationChanged(p0: Location) {
                trackBoolean=sharedPreferences.getBoolean("trackBoolean",false)
                //trackboolean ilk a????ld??????nda false olacak ve buras?? 1 kere ??al????acka ancak sonras??nda true olaca???? i??in buras?? bir daha ??al????mayacak
                //ve konum de??i??ti??inde tekrar bizi ayn?? yere atmayacak
                if (trackBoolean==false)
                {
                    val userLocation=LatLng(p0.latitude,p0.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15f))
                    sharedPreferences.edit().putBoolean("trackBoolean",true).apply()
                }

            }

        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION))
            {//request permission
                Snackbar.make(binding.root,"Permission needed for Location",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission"){
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }.show()
            }
            else
            {//request permission
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
        else
        {
            //permission granted
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
            val lastLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastLocation!=null)
            {
                val lastUserLocation=LatLng(lastLocation.latitude,lastLocation.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15f))
            }
            mMap.isMyLocationEnabled=true //haritada bizi mavi olarak g??sterir.
        }

    }

    private fun registerLauncher()
    {
        permissionLauncher=registerForActivityResult(ActivityResultContracts.RequestPermission()){
            if(it)
            {
                //permission accepted
                if (ContextCompat.checkSelfPermission(this@MapsActivity,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)

            }
            else{
                //permission denied
                Toast.makeText(this@MapsActivity,"Permission Needed!",Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onMapLongClick(p0: LatLng)
    {
        mMap.clear() //clear eklemezsek kullan??c?? uzun t??klad?????? her yere marker b??rak??r ve ??ncekiler silinmez
        mMap.addMarker(MarkerOptions().position(p0)) //kullan??c?? uzun t??klarsa herhangi bir yere oraya k??rm??z?? marker koyabilir
        selectedLatitude=p0.latitude
        selectedLongtitude=p0.longitude
    }

    fun save(view: View)
    {
        if (selectedLatitude!=null && selectedLongtitude!=null)
        {
            val place=Place(placeText.text.toString(),selectedLatitude!!,selectedLongtitude!!)
            compositeDisposable.add(
                placeDao.insert(place).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread() as io.reactivex.rxjava3.core.Scheduler).subscribe(this::handleResponse) //Kotlin'de referans ::handleResponse ??eklinde verilir burada fonksiyonu ??al????t??r falan demiyoruz. ref veriyoruz.
                //subscribe bittikten sonra ne olaca????n?? s??ylememiz laz??m onuda ayr?? bir fonksiyon handleResponse i??erisinde yapaca????z
                //asl??dna yapaca????m??z i??lem main aktiviteye geri d??nmek
                //rxjava haliyle java'ya ait oldu??u i??in ??nce scheduler sonras??nda AndroidScheduler kulland??k ????nk?? di??erleri Java i??erisinde de varken MainThread yaln??zca Android'de var.
            )

        }

    }
    private fun handleResponse()
    {
        val intent=Intent(this@MapsActivity,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) //aktiviteyi terk ederken a????k olan t??m aktiviteleri kapat??r??z
        startActivity(intent)
    }
    fun delete(view:View)
    {

    }
}


