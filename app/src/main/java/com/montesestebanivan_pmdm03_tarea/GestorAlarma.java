package com.montesestebanivan_pmdm03_tarea;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.*;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import android.telephony.SmsManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

public class GestorAlarma extends BroadcastReceiver {
    //https://www.c-sharpcorner.com/article/creating-and-scheduling-alarms-in-android/

    @Override
    public void onReceive(Context context, Intent intent) {

        //La alarma al ser configurada con tiempo inexacto puede saltar un par de minutos más tarde
        Log.w("Alarma","Recibida");
        gestionarAviso(context);

    }


    private void gestionarAviso(Context context){

        BDgestor bd = new BDgestor(context);
        Optional<ArrayList<Contacto>> opList = bd.quienCumpleHoy();
        bd.close();

        HashSet<String> setNombres = new HashSet<>();
        HashSet<Contacto> setContacts = new HashSet<>();

        opList.ifPresent(contactos -> contactos
                .stream()
                .peek( con -> setNombres.add(con.getNombre())) //Como mínimo se muestra notificación
                .forEach(c -> {
                    if (c.getTipoNotif().equals("1")) {
                        setContacts.add(c);
                    }
                }));

        String nombres = setNombres.stream().collect(Collectors.joining(", "));
        mandarNotificacion(context, nombres);
        mandarSms(context, setContacts);
    }

    private void mandarNotificacion(Context context, String contactos){
        //https://developer.android.com/training/notify-user/build-notification?hl=es-419

        String canalId = "CHANNEL_ID_101";
        CharSequence name = context.getString(R.string.canal_notificacion);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel channel = new NotificationChannel(canalId, name, importance);
        channel.setDescription("Canal predeterminado para notificaciones");

        Notification notify =  new NotificationCompat.Builder(context, canalId)
                .setSmallIcon(R.drawable.cake512)
                .setContentTitle("Cumpleaños Helper")
                .setContentText("Despliega para ver todo el texto")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Feliz cumpleaños a l@s siguientes afortunad@s: "+ contactos))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.cake512))
                .build();

        //NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationManager notificationManager =  context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        notificationManager.notify(777, notify);
    }

    private void mandarSms(Context context, HashSet<Contacto> mapContactos){
    //https://www.geeksforgeeks.org/sending-a-text-message-over-the-phone-using-smsmanager-in-android/

        try{
            mapContactos
                    .stream()
                    .filter( c -> (c.getTelefono() != null)  &&  (!c.getTelefono().isEmpty()) )
                    .forEach( co ->{
                        SmsManager smsManager=SmsManager.getDefault();
                        smsManager.sendTextMessage(co.getTelefono(),null,"Mensaje: " + co.getMensaje(),null,null);
                        System.out.println("Enviado mensaje en método mandarSms");
                        //Se superponen entre ellos y luego es eliminado por el siguiente Toast por lo que estos no se ven
                        Toast.makeText(context.getApplicationContext(),"SMS enviado a " + co.getNombre(),Toast.LENGTH_SHORT).show();
                    });

           Toast t = Toast.makeText(context.getApplicationContext(),"SMS enviados, compruebe en la mensajería de su Telf",Toast.LENGTH_LONG);
           t.setGravity(Gravity.CENTER,0,0);
           View v = t.getView();
           v.setBackgroundColor(Color.parseColor("#ababab"));
           t.show();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}//Fin