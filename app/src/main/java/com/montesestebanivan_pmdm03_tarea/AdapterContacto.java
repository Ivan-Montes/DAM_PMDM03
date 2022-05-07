package com.montesestebanivan_pmdm03_tarea;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.stream.Collectors;

public class AdapterContacto  extends ArrayAdapter<Contacto> {

    private List<Contacto> listContactos;

    public AdapterContacto(@NonNull Context context, int resource, @NonNull List<Contacto> objects) {
        super(context, resource, objects);
        listContactos = objects;
    }

    @Override
    public View getView(int position, View cnvtView, ViewGroup prnt){
        return crearFilaPersonalizada(position, cnvtView, prnt);
    }

    public View crearFilaPersonalizada(int position, View convertView, ViewGroup parent){

        LayoutInflater inflador = LayoutInflater.from(getContext());
        View filaContacto = inflador.inflate(R.layout.listview_contacts_fila, parent, false);

        ImageView imagen = (ImageView) filaContacto.findViewById(R.id.imagenContacto);
        if (listContactos.get(position).getRutaImagen() != null){
            imagen.setImageURI(Uri.parse(listContactos.get(position).getRutaImagen().toString()));//android.net.Uri not Serializable - java.net.URI SI serializable
        }else{
            imagen.setImageResource(R.drawable.support_icon_512px);
        }
        imagen.setAdjustViewBounds(true);
        imagen.setScaleType(ImageView.ScaleType.CENTER_CROP);

        TextView tvNombre = (TextView) filaContacto.findViewById(R.id.txtContactoNombre);
        tvNombre.setText(listContactos.get(position).getNombre());

        TextView tvTelf = (TextView) filaContacto.findViewById(R.id.txtContactoTelefono);
        tvTelf.setText(listContactos.get(position).getSetTelefonos()
                                                    .stream()
                                                    .collect(Collectors.joining(", ")));

        TextView tvNotificacion = (TextView) filaContacto.findViewById(R.id.txtContactoNotificacion);
        String noti = listContactos.get(position).getTipoNotif();
        int notiTxtRstring;
        switch (noti){
            case "0":
                notiTxtRstring = R.string.solo_noti;
                break;
            case "1":
                notiTxtRstring = R.string.sms;
                break;
            default:
                notiTxtRstring = R.string.sin_noti;
        }
        tvNotificacion.setText(notiTxtRstring);

        return filaContacto;
    }

}
