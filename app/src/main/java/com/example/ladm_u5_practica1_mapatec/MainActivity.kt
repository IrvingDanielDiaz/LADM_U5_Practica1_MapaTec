package com.example.ladm_u5_practica1_mapatec

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var baseRemota = FirebaseFirestore.getInstance()
    var posicion = ArrayList<Data>()
    var resultado ="vacío"
    var latitudActual = 0.0
    var longitudActual = 0.0
    var lugares = ArrayList<String>()
    var pCs = ArrayList<Data>()
    lateinit var locacion : LocationManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),1)
        }
        baseRemota.collection("tecnologico").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if(firebaseFirestoreException != null){
                this.setTitle("Conexión sin éxito")
                return@addSnapshotListener
            }

            posicion.clear()
            lugares.clear()
            for (document in querySnapshot!!){
                var data = Data()
                data.nombre = document.getString("nombre").toString()
                data.posicion1 = document.getGeoPoint("posicion1")!!
                data.posicion2 = document.getGeoPoint("posicion2")!!
                data.posicion3 = document.getGeoPoint("pc")!!
                data.datosExtras = document.getString("de").toString()!!
                posicion.add(data)
                lugares.add(document.getString("nombre").toString())
            }
            this.setTitle("Conexión con éxito")
            var adaptador = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,lugares)
            lista.adapter = adaptador
        }

        lista.setOnItemClickListener { parent, view, position, id ->
            var dato1 = posicion[position].posicion3.latitude
            var dato2 =posicion[position].posicion3.longitude
            var dato3 = lugares[position]
            var intent : Intent = Intent(this,MapsActivity::class.java)
            intent.putExtra("dato1",dato1)
            intent.putExtra("dato2",dato2)
            intent.putExtra("lugar",dato3)
            startActivity(intent)

        }
            locacion = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            var oyente = Oyente(this)
            locacion.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,01f,oyente)
    }
}

class Oyente(puntero:MainActivity) : android.location.LocationListener{
    var p = puntero
    override fun onLocationChanged(location: Location) {
        p.latitudActual = location.latitude
        p.longitudActual = location.longitude
        var geoPosicion = GeoPoint(location.latitude,location.longitude)
        for(item in p.posicion){
            if(item.estoyEn(geoPosicion)){
                p.ubicacion.setText(item.nombre)
                var a = item.datosExtras.replace(",","\n")
                p.txtDatosExtras.setText("Información: \n"+a)

            }
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

    }

    override fun onProviderEnabled(provider: String?) {

    }

    override fun onProviderDisabled(provider: String?) {

    }

}