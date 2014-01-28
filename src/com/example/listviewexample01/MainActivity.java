package com.example.listviewexample01;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {
	TextView nombrePeli;
	final static int RQS_OPEN_AUDIO_MP3 = 1;
	String srcPath = null;
	ArrayList<Subtitulo> list;
	ListView lv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

//	    String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
//	        "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
//	        "Linux", "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux",
//	        "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2",
//	        "Android", "iPhone", "WindowsMobile" };

	    lv = (ListView) findViewById(R.id.listView);
	    list = obtenerItems();
	    
	    final AdapterSubtitulo adapter = new AdapterSubtitulo(this, list);
	    lv.setAdapter(adapter);

	    nombrePeli = (TextView)findViewById(R.id.nombrePeli);
	    nombrePeli.setOnLongClickListener(new OnLongClickListener(){
			public boolean onLongClick(View v) {
				Intent intent = new Intent();
				intent.setType("audio/mp3");
				intent.setAction(Intent.ACTION_GET_CONTENT);
//				intent.setAction(Intent.ACTION_PICK);
				startActivityForResult(Intent.createChooser(
						intent, "Open Audio (mp3) file"), RQS_OPEN_AUDIO_MP3);
				return true;
			} 
	    });

//	    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//	      @Override
//	      public void onItemClick(AdapterView<?> parent, final View view,
//	          int position, long id) {
///*	    	  final String item = (String) parent.getItemAtPosition(position);
//	    	  list.remove(item);
//	    	  adapter.notifyDataSetChanged();
//
//	    	  //view.animate().setDuration(2000).alpha(0)
//	          //	.withEndAction(new Runnable() {
//	          //		@Override
//	          //		public void run() {
//	          //			list.remove(item);
//	          //			adapter.notifyDataSetChanged();
//	          //			view.setAlpha(1);
//	          //		}
//	          //	});
//*/
//	    	  	Toast.makeText(getApplicationContext(),
//	    	    	      "Click ListItem Number " + position, Toast.LENGTH_LONG)
//	    	    	      .show();
//	      	}
//	    });
	}

//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		Toast.makeText(this,
//				String.valueOf((getListView()).getCheckedItemPosition()),
//				Toast.LENGTH_LONG).show();
//		return true;
//	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private ArrayList<Subtitulo> obtenerItems() {
		ArrayList<Subtitulo> items = new ArrayList<Subtitulo>();
		
		items.add(new Subtitulo(1000, 2000, "Patatas1", "Tuberculo1",1));
		items.add(new Subtitulo(3000, 4000, "Patatas2", "Tuberculo2",2));
		items.add(new Subtitulo(5000, 6000, "Patatas3", "Tuberculo3",3));
		items.add(new Subtitulo(7000, 8000, "Patatas4", "Tuberculo4",4));
		items.add(new Subtitulo(9000, 10000, "Patatas5", "Tuberculo5",5));
		items.add(new Subtitulo(11000, 12000, "Patatas1", "Tuberculo1",1));
		items.add(new Subtitulo(13000, 14000, "Patatas2 albondigas chorizo meadillos con tomate y sal", "Tuberculo2",2));
		items.add(new Subtitulo(15000, 16000, "Patatas3", "Tuberculo3 andoni zubi 44 el que gana es el better",3));
		items.add(new Subtitulo(17000, 18000, "Patatas4", "Tuberculo4",4));
		items.add(new Subtitulo(19000, 210000, "Patatas5", "Tuberculo5",5));
		
		return items;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == RQS_OPEN_AUDIO_MP3) {
				try
				{
					int j = 0;
					Subtitulo sub = new Subtitulo(0, 0, "", "",0);
					
					Uri audioFileUri = data.getData();
					nombrePeli.setText(audioFileUri.getLastPathSegment());
					srcPath = audioFileUri.getPath().substring(0, audioFileUri.getPath().length()-audioFileUri.getLastPathSegment().length());
					String[] listaFicheros = new String[]{"",""};
					BuscadorDeFicheros.dameFicheros(srcPath, BuscadorDeFicheros.dameRegex(audioFileUri.getLastPathSegment().substring(0, audioFileUri.getLastPathSegment().length()-4) + "*.srt"), listaFicheros, false);

					//cmdReset();
					//cmdSetDataSource(srcPath);
					list.clear();
					for (int i = 0; i < listaFicheros.length; i++) {
						File f = new File(listaFicheros[i]);
					
						BufferedReader fin =
								new BufferedReader(
										new InputStreamReader(
												new FileInputStream(f)));
										
						String texto = fin.readLine();
						String varTexto;
						while(texto != null){
							Subtitulo mio = new Subtitulo(0,0,"","",0);
							mio.id = Long.parseLong(texto) + 0;
							texto = fin.readLine();
							mio.horaIni = 
									Long.parseLong(texto.substring(0, 2)) * 60 * 60 * 1000 +
									Long.parseLong(texto.substring(3, 5)) * 60 * 1000 +
									Long.parseLong(texto.substring(6, 8)) * 1000 +
									Long.parseLong(texto.substring(9, 12));
							
							mio.horaFin= 
									Long.parseLong(texto.substring(17, 19)) * 60 * 60 * 1000 +
									Long.parseLong(texto.substring(20, 22)) * 60 * 1000 +
									Long.parseLong(texto.substring(23, 25)) * 1000 +
									Long.parseLong(texto.substring(26, 29));
							
							texto = fin.readLine();
							varTexto="";
							while(!(texto == null || "".equals(texto))){
								varTexto = varTexto + texto + "\n";
								texto = fin.readLine();
							}
							varTexto = varTexto.substring(0, varTexto.length() - 1);
							if (i == 0)
								mio.setTextoSub(varTexto);
							else
								//list.contains(mio.horaIni);
								mio.setTextoSubTra(varTexto);
	
							if (i == 0) {
								while (j < list.size()) {
									sub = list.get(j);
									if (sub.horaIni <= mio.horaIni && sub.horaFin >= mio.horaFin){
										
									}
								}
							}
							else {
								list.add(new Subtitulo(mio.horaIni, mio.horaFin, mio.textoSub, mio.textoSubTra, mio.id));
							}

							texto = fin.readLine();
			            } 
						
						fin.close();
					}

					((BaseAdapter) lv.getAdapter()).notifyDataSetChanged();
				}
				catch (Exception ex)
				{
					Log.e("Ficheros", "Error al leer fichero desde tarjeta SD");
				}
			} 
		} 
	}
}

