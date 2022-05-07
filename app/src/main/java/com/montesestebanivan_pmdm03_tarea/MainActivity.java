package com.montesestebanivan_pmdm03_tarea;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private BDgestor bdGestor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar ab = getSupportActionBar();
        if (ab != null){

            ab.setDisplayShowHomeEnabled(true);
            ab.setLogo(R.mipmap.ic_launcher_round);
            ab.setDisplayUseLogoEnabled(true);
        }

        pedirPermisosParaLeerContactos();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_action_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.confFeli) {
            confFelicitaciones();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onRestart(){
        super.onRestart();

        ListView listView = findViewById(R.id.listViewContactos);
        listView.invalidate();
        mostrarListaContactos();
    }

    private void pedirPermisosParaLeerContactos(){
        //https://developer.android.com/training/permissions/requesting#java

        if ( comprobarPermisos(Manifest.permission.READ_CONTACTS) || comprobarPermisos(Manifest.permission.SEND_SMS)) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS,Manifest.permission.SEND_SMS}, 100);

        } else {

            inicializarBD();
            mostrarListaContactos();
        }
    }

    private boolean comprobarPermisos(String permisoManifiesto){

        return (ContextCompat.checkSelfPermission(this, permisoManifiesto) != PackageManager.PERMISSION_GRANTED);
    }

    //Gestiona la contestación a la petición de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0){

            if (Arrays.stream(grantResults).allMatch(b-> b == PackageManager.PERMISSION_GRANTED)){
                inicializarBD();
                mostrarListaContactos();
            }
        }
    }


    private void inicializarBD(){

            bdGestor = new BDgestor(MainActivity.this);

    }

    private void mostrarListaContactos(){

        if ( !bdGestor.getOptListContactos().isPresent()){

            bdGestor.getAndGenerateOptListContactos();

        }

        AdapterContacto adapterContacto = new AdapterContacto(this,R.id.listViewContactos,bdGestor.getOptListContactos().orElse(new ArrayList<>()));
        ListView listView = findViewById(R.id.listViewContactos);
        listView.setAdapter(adapterContacto);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Contacto contacto = bdGestor.getOptListContactos().get().get(position);
                Intent i = new Intent(MainActivity.this.getApplicationContext(),VerContactoActivity.class);
                //Intent i = new Intent(getApplicationContext(),VerContactoActivity.class);
                i.putExtra("contacto",contacto);
                MainActivity.this.startActivity(i);
            }
        });
    }

    private void confFelicitaciones(){

        Fragmentos.TimePickerFragment timePickerFragment = new Fragmentos.TimePickerFragment();
        timePickerFragment.setOnTimeSetListener(new TimePickerDialog.OnTimeSetListener(){

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Log.w("NuevoTime","Hora: " + hourOfDay + " <|> Minutos: " + minute);
                confAlarma(hourOfDay,minute);
            }
        });

        timePickerFragment.show(getSupportFragmentManager(), "timePicker");
    }

    private void confAlarma(int hora, int minut){
        //https://developer.android.com/training/scheduling/alarms#java
        Calendar calendario = Calendar.getInstance();
        calendario.setTimeInMillis(System.currentTimeMillis());
        calendario.set(Calendar.HOUR_OF_DAY,hora);
        calendario.set(Calendar.MINUTE,minut);

        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(),GestorAlarma.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,calendario.getTimeInMillis(),AlarmManager.INTERVAL_DAY,pendingIntent);
    }


}//Fin