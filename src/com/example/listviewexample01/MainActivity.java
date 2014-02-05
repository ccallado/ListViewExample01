package com.example.listviewexample01;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity {
	TextView nombrePeli;
	final static int RQS_OPEN_AUDIO_MP3 = 1;
	String srcPath = null;
	ArrayList<Subtitulo> lista;
	ListView lv;
	SeekBar seekbar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

	    lv = (ListView) findViewById(R.id.listView);
	    lista = obtenerItems();
	    
	    final AdapterSubtitulo adapter = new AdapterSubtitulo(this, lista);
	    lv.setAdapter(adapter);

	    //Si realizo una pulsación larga sobre el nombrede la película lanzo un intent para buscar un fichero
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

	    //Preparo la barra deslizadora con la longitud de subtitulos 
	    //y creo un manejador de eventos para el cambio
	    seekbar = (SeekBar) findViewById(R.id.seekBar1);
        seekbar.setMax(lista.size());
        
        seekbar.setOnSeekBarChangeListener( new OnSeekBarChangeListener() {
        	public void onProgressChanged(SeekBar seekBar, int progress,
        			boolean fromUser) {
        		lv.setSelection(progress);
        	}

        	public void onStartTrackingTouch(SeekBar seekBar) {	
        		// TODO Auto-generated method stub
        	}

        	public void onStopTrackingTouch(SeekBar seekBar) {
        		// TODO Auto-generated method stub
        	}
        });
        
        ImageButton arrancar = (ImageButton) findViewById(R.id.btnPlay);

        arrancar.setOnClickListener(new OnClickListener() {
               public void onClick(View view) {
            	   Intent intent = new Intent(MainActivity.this, ServicioMusica.class);
                   startService(intent);
               }
        });

        ImageButton detener = (ImageButton) findViewById(R.id.btnPausa);
        
        detener.setOnClickListener(new OnClickListener() {
               public void onClick(View view) {
                      stopService(new Intent(MainActivity.this,
                                   ServicioMusica.class));
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

// Función que lanza la actividad Preferencias
    public void lanzarPreferencias (View view) {
    	Intent i = new Intent(this, Preferencias.class);
    	startActivity(i);
    }

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
					//variables locales
					int ultSubEsp = 0;
					int margen = 333; //Tiempo antes y despues que no se tendrá en cuenta de los subtitulos
					
					Subtitulo subEspSig = new Subtitulo(0, 0, "", "",0);
					Subtitulo subEspTmp = new Subtitulo(0, 0, "", "",0);
					Subtitulo subIng = new Subtitulo(0, 0, "", "",0);
					
					Uri audioFileUri = data.getData();
					nombrePeli.setText(audioFileUri.getLastPathSegment());
					srcPath = audioFileUri.getPath().substring(0, audioFileUri.getPath().length()-audioFileUri.getLastPathSegment().length());
					String[] listaFicheros = new String[]{"",""};
					
					SharedPreferences prefs =
						     getSharedPreferences("com.example.listviewexample01_preferences", Context.MODE_PRIVATE);	
					Editor edPref = prefs.edit();
					edPref.putString("nombrePeli", audioFileUri.getPath());
					edPref.commit();
					
					Boolean varAgrupar = prefs.getBoolean("agruparSubtitulos", false);

					BuscadorDeFicheros.dameFicheros(srcPath, BuscadorDeFicheros.dameRegex(audioFileUri.getLastPathSegment().substring(0, audioFileUri.getLastPathSegment().length()-4) + "*.srt"), listaFicheros, false);

					//cmdReset();
					//cmdSetDataSource(srcPath);
					lista.clear();
					for (int i = 0; i < listaFicheros.length; i++) {
						File fileFichero = new File(listaFicheros[i]);
					
						BufferedReader bufFichero =
								new BufferedReader(
										new InputStreamReader(
												new FileInputStream(fileFichero), "ISO-8859-1"));
						
						//Leo la primera línea del fichero
						
						String textoLinea = bufFichero.readLine();
						String varTexto;
						
						//Mientras haya líneas en el fichero
						while(textoLinea != null){
							//Creo subtitulo temporal uno nuevo por cada objeto
							Subtitulo Tmp = new Subtitulo(0,0,"","",0);

							//Pongo el ID
							Tmp.id = Long.parseLong(textoLinea) + 0;
							textoLinea = bufFichero.readLine();
							//Pongo la hora de inicio
							Tmp.horaIni = 
									Long.parseLong(textoLinea.substring(0, 2)) * 60 * 60 * 1000 +
									Long.parseLong(textoLinea.substring(3, 5)) * 60 * 1000 +
									Long.parseLong(textoLinea.substring(6, 8)) * 1000 +
									Long.parseLong(textoLinea.substring(9, 12));
							//Pongo la hora de finalización
							Tmp.horaFin= 
									Long.parseLong(textoLinea.substring(17, 19)) * 60 * 60 * 1000 +
									Long.parseLong(textoLinea.substring(20, 22)) * 60 * 1000 +
									Long.parseLong(textoLinea.substring(23, 25)) * 1000 +
									Long.parseLong(textoLinea.substring(26, 29));
							
							//Saco el subtitulo del idioma (puede estar compuesto por varias líneas)
							//Puede estar en HTML y hay que renderizarlo para hacerlo texto plano
							textoLinea = bufFichero.readLine();
							varTexto="";
							
							while(!(textoLinea == null || "".equals(textoLinea))){
								varTexto = varTexto + textoLinea + "\n";
								textoLinea = bufFichero.readLine();
							}
							//Quito el último salto de línea
							if ("".equals(varTexto)){
								varTexto = varTexto.substring(0, varTexto.length() - 1);
							}
							
							//Pongo Subtitulo en primer idioma o segundo
							if (i == 0)
								Tmp.setTextoSub(varTexto);
							else
								//list.contains(mio.horaIni);
								Tmp.setTextoSubTra(varTexto);
							
							subIng = (Subtitulo)Tmp.clone();
	
							//Si es el primer idioma
							if (i == 0) {
								lista.add(new Subtitulo(Tmp.horaIni, Tmp.horaFin, Tmp.textoSub, Tmp.textoSubTra, Tmp.id));
							}
							//Si es el segundo idioma
							else {
								//Busco el primer subtitulo del primer idioma
								//que incluya al subtitulo del segundo idioma 
								while (ultSubEsp < lista.size()) {
									Tmp = (Subtitulo)subIng.clone();
									subEspTmp = lista.get(ultSubEsp);
									//Si fecha final segundo idioma > fecha inicio primer idioma
									//if (Tmp.horaFin >= subEspTmp.horaIni && Tmp.horaIni > subEspTmp.horaFin) {
									if (Tmp.horaFin - subEspTmp.horaIni >= margen && Tmp.horaIni - subEspTmp.horaFin > margen) {
										ultSubEsp++;
										continue;
									}

									//Recojo el siguiente subtitulo del primer idioma si existe
									try {
										subEspSig = lista.get(ultSubEsp+1);
									} catch (Exception e) {
										subEspSig = new Subtitulo(0, 0, "", "",0);
									}
										
									//No hay subtitulo del primer idioma 
									if (subEspTmp.horaIni > Tmp.horaFin) {
										//1 - - - -
										//Creo un subtitulo nuevo segundo idioma sin primer idioma
										lista.add(ultSubEsp, new Subtitulo(Tmp.horaIni, Tmp.horaFin, Tmp.textoSub, Tmp.textoSubTra, Tmp.id));
//										Siguiente subtitulo Ing
										break;
									}
									else {
										//Ya había comenzado el subtitulo del primer idioma
										//if (subEspTmp.horaIni < Tmp.horaIni) {
										if ( Tmp.horaIni - subEspTmp.horaIni > margen) {
											//El primer idioma continua después del segundo
											//if (subEspTmp.horaFin > Tmp.horaFin) {
											if (subEspTmp.horaFin - Tmp.horaFin > margen) {
												//0 1 1 0 0
//												Incluir subtitulo Ing en EspT
												subEspTmp.textoSubTra = Tmp.textoSubTra ;
//												Actualizar subtitulo Esp desde EspT
												lista.set(ultSubEsp, subEspTmp);
//												Siguiente subtitulo Ing
												break;
											}
											else {
												//El segundo idioma se solapa con el siguiente del primer idioma
												//if (Tmp.horaFin > subEspSig.horaIni) {
												if (Tmp.horaFin - subEspSig.horaIni > margen) {
													//Hay que dividir el subtitulo
													if (!"".equals(subEspTmp.textoSubTra )){
														//0 1 0 1 1
														if (varAgrupar) {
//															Hora Final EspT = Hora Final Esp Siguiente
															subEspTmp.horaFin = subEspSig.horaFin;
//															Incluir subtitulo Esp Siguiente en EspT
															subEspTmp.textoSub = subEspTmp.textoSub + "\n" + subEspSig.textoSub;
//															Incluir subtitulo Ing en EspT
//															subEspTmp.textoSubTra = subEspTmp.textoSubTra + "\n" + Tmp.textoSubTra;
//															Elimina subtitulo Esp Siguiente
															lista.remove(ultSubEsp+1);
														}
														else {
//															Hora Final EspT = Hora Inicio Ing - 1
															subEspTmp.horaFin = Tmp.horaIni - 1;
//															Hora Final Tmp = Hora Inicio Esp Siguiente – 1
															Tmp.horaFin = subEspSig.horaIni - 1;
//															Hora Inicial Ingles = Hora Inicio Esp Siguiente
															subIng.horaIni = subEspSig.horaIni;
//															Incluir subtitulo Esp en Tmp
															Tmp.textoSub = subEspTmp.textoSub;
//															Incluir subtitulo Ing en Tmp
															Tmp.textoSubTra = Tmp.textoSubTra;
//															Insertar Subtitulo Tmp
															ultSubEsp++;
															lista.add(ultSubEsp, new Subtitulo(Tmp.horaIni, Tmp.horaFin, Tmp.textoSub, Tmp.textoSubTra, Tmp.id));
//															Actualizar subtitulo Esp desde EspT
															//lista.set(ultSubEsp, subEspTmp);
//															Siguiente subtitulo Esp
															ultSubEsp++;
														}
													}
													else {
														//0 1 0 1 0
														if (varAgrupar) {
//															Hora Final EspT = Hora Final Esp Siguiente
															subEspTmp.horaFin = subEspSig.horaFin;
//															Incluir subtitulo Esp Siguiente en EspT
															subEspTmp.textoSub = subEspTmp.textoSub + "\n" + subEspSig.textoSub;
//															Elimina subtitulo Esp Siguiente
															lista.remove(ultSubEsp+1);
														}
														else {
//															Hora Final EspT = Hora Inicio Esp Siguiente – 1
															subEspTmp.horaFin = subEspSig.horaIni-1;
//															Hora Inicio Tmp = Hora Inicio Esp Siguiente
															//Tmp.horaIni = subEspSig.horaIni;
															subIng.horaIni = subEspSig.horaIni;
//															Incluir subtitulo Ing en EspT
															subEspTmp.textoSubTra = Tmp.textoSubTra;
//															Actualizar subtitulo Esp desde EspT
															//lista.set(ultSubEsp, subEspTmp);
//															Siguiente subtitulo Esp
															ultSubEsp++;
														}
													}
												}
												else {
													//Hay que dividir el subtitulo
													if (!"".equals(subEspTmp.textoSubTra )){
														//0 1 0 0 1
														if (varAgrupar) {
//															Hora Final EspT = Hora Final Ing
															subEspTmp.horaFin = Tmp.horaFin;
//															Incluir subtitulo Ing en EspT
															subEspTmp.textoSubTra = subEspTmp.textoSubTra + "\n" + Tmp.textoSubTra;
//															Siguiente subtitulo Esp
															ultSubEsp++;
//															Siguiente subtitulo Ing
															break;
														}
														else {
//															Hora Fin Esp = Hora Inicio Ing
															subEspTmp.horaFin = Tmp.horaIni - 1;
//															Hora Final Tmp = Hora Fin Ing
															Tmp.horaFin= Tmp.horaFin;
//															Incluir subtitulo Esp en Tmp
															Tmp.textoSub = subEspTmp.textoSub;
//															Incluir subtitulo Ing en Tmp
															Tmp.textoSubTra=Tmp.textoSubTra;
//															Insertar Subtitulo Tmp
															ultSubEsp++;
															lista.add(ultSubEsp, new Subtitulo(Tmp.horaIni, Tmp.horaFin, Tmp.textoSub, Tmp.textoSubTra, Tmp.id));
//															Siguiente subtitulo Esp
															ultSubEsp++;
//															Siguiente subtitulo Ing
															break;
														}
													}
													else {
														//0 1 0 0 0
//														Hora Final EspT = Hora Fin Ing
														subEspTmp.horaFin = Tmp.horaFin;
//														Incluir subtitulo Ing en EspT
														subEspTmp.textoSubTra = Tmp.textoSubTra;
//														Actualizar subtitulo Esp desde EspT
														//lista.set(ultSubEsp, subEspTmp);
//														Siguiente subtitulo Esp
														ultSubEsp++;
//														Siguiente subtitulo Ing
														break;
													}
												}
											}
										}
										else {
											//El primer idioma continua después del segundo
											//if (subEspTmp.horaFin > Tmp.horaFin) {
											if (subEspTmp.horaFin - Tmp.horaFin > margen) {
												//El segundo idioma NO se solapa con el siguiente del primer idioma
												//if (!(Tmp.horaFin > subEspSig.horaIni)) {
												if (!(Tmp.horaFin - subEspSig.horaIni > margen)) {
													//No hay que dividir el subtitulo
													if (!"".equals(subEspTmp.textoSubTra )){
														//0 0 1 0 1
														if (varAgrupar) {
//															Hora Inicio Tmp = Hora Inicio Ing
															Tmp.horaIni = Tmp.horaIni;
//															Incluir subtitulo Ing en EspT
															subEspTmp.textoSubTra = subEspTmp.textoSubTra + "\n" + Tmp.textoSubTra;
//															Siguiente subtitulo Ing
															break;
														}
														else {
//															Hora Inicio Tmp = Hora Inicio Ing
															Tmp.horaIni = Tmp.horaIni;
//															Hora Final Tmp = Hora Inicio Esp - 1
															Tmp.horaFin = subEspTmp.horaIni - 1;
//															Incluir subtitulo Esp en Tmp
															Tmp.textoSub = subEspTmp.textoSub;
//															Incluir subtitulo Ing en Tmp
															Tmp.textoSubTra = Tmp.textoSubTra;
//															Insertar Subtitulo Tmp
															ultSubEsp++;
															lista.add(ultSubEsp, new Subtitulo(Tmp.horaIni, Tmp.horaFin, Tmp.textoSub, Tmp.textoSubTra, Tmp.id));
//															Siguiente subtitulo Ing
															break;
														}
													}
													else {
														//0 0 1 0 0
//														Hora Inicio EspT = Hora Inicio Ing
														subEspTmp.horaIni = Tmp.horaIni;
//														Incluir subtitulo Ing en EspT
														subEspTmp.textoSubTra = Tmp.textoSubTra;
//														Actualizar subtitulo Esp desde EspT
														//lista.set(ultSubEsp, subEspTmp);
//														Siguiente subtitulo Ing
														break;
													}
												}
											}
											else {
												//El segundo idioma se solapa con el siguiente del primer idioma
												//if (Tmp.horaFin > subEspSig.horaIni) {
												if (Tmp.horaFin - subEspSig.horaIni > margen) {
													//0 0 0 1 0
													if (varAgrupar) {
//														Hora Final EspT = Hora Final Esp Siguiente
														subEspTmp.horaFin = subEspSig.horaFin;
//														Incluir subtitulo Esp Siguiente en EspT
														subEspTmp.textoSub = subEspTmp.textoSub + "\n" + subEspSig.textoSub;
//														Elimina subtitulo Esp Siguiente
														lista.remove(ultSubEsp+1);
													}
													else {
//														Hora Inicio segundo idioma = Hora Inicio Esp Siguiente
														subIng.horaIni = subEspSig.horaIni;
														Tmp.horaIni = subEspSig.horaIni;
//														Hora Inicio EspT = Hora Inicio Ing
														//subEspTmp.horaIni = Tmp.horaIni;
//														Hora Final EspT = Hora Inicio Esp Siguiente – 1
														subEspTmp.horaFin = subEspSig.horaIni - 1;
//														Incluir subtitulo Ing en EspT
														subEspTmp.textoSubTra = Tmp.textoSubTra;
//														Actualizar subtitulo Esp desde EspT
														lista.set(ultSubEsp, subEspTmp);
//														Siguiente subtitulo Esp
														ultSubEsp++;
													}
												}
												else {
													//0 0 0 0 0
//													Hora Inicio EspT = Hora Inicio Ing
													subEspTmp.horaIni = Tmp.horaIni;
//													Hora Final EspT = Hora Fin Ing
													subEspTmp.horaFin = Tmp.horaFin;
//													Incluir subtitulo Ing en EspT
													subEspTmp.textoSubTra = Tmp.textoSubTra;
//													Actualizar subtitulo Esp desde EspT
													//lista.set(ultSubEsp, subEspTmp);
//													Siguiente subtitulo Esp
													ultSubEsp++;
//													Siguiente subtitulo Ing
													break;
												}
											}
										}
									}
								}
							}

							//Voy a por el siguiente subtitulo en el fichero
							textoLinea = bufFichero.readLine();
			            } 
						//Cierro el fichero
						bufFichero.close();
					}
					
					//Actualizo el listView
					seekbar.setMax(lista.size());
					((BaseAdapter) lv.getAdapter()).notifyDataSetChanged();
			 		lv.setSelection(0);
				}
				catch (Exception ex)
				{
					Log.e("Ficheros", "Error al leer fichero desde tarjeta SD");
				}
			} 
		} 
	}
}

