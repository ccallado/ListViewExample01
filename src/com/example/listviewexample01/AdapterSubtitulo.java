package com.example.listviewexample01;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AdapterSubtitulo extends BaseAdapter {

	protected Activity activity;
	protected ArrayList<Subtitulo> items;

	public AdapterSubtitulo(Activity activity, ArrayList<Subtitulo> items) {
		this.activity=activity;
		this.items=items;
	}
	
	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return items.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Generamos una convertView por motivos de eficiencia
		View v = convertView;
		//Asociamos el layout de la lista que hemos creado
		if(convertView == null){
			LayoutInflater inf = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inf.inflate(R.layout.sub_titulo_simple, null);
		}
		
		// Creamos un objeto subtitulo
		Subtitulo sub = items.get(position);

		//Rellenamos la hora de inicio
		TextView horaIni = (TextView) v.findViewById(R.id.horaIni);
		horaIni.setText(sub.getHoraIniT());

		//Rellenamos la hora de fin
		TextView horaFin = (TextView) v.findViewById(R.id.horaFin);
		horaFin.setText(sub.getHoraFinT());

		//Rellenamos el SubTitulo
		TextView subTitulo = (TextView) v.findViewById(R.id.subTitulo);
		subTitulo.setText(Html.fromHtml(sub.getTextoSub()));

		//Rellenamos el SubTituloTra
		TextView subTituloTra = (TextView) v.findViewById(R.id.subTituloTra);
		subTituloTra.setText(Html.fromHtml(sub.getTextoSubTra()));

		// Retornamos la vista
		return v;
	}

}
