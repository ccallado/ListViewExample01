package com.example.listviewexample01;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.example.listviewexample01.ServicioMusica.LocalBinder;

public class MainActivity extends Activity {
	TextView nombrePeli;
	final static int RQS_OPEN_AUDIO_MP3 = 1;
	String srcPath = null;
	ArrayList<Subtitulo> lista;
	ListView lv;
	SeekBar seekbar;
	boolean mBounded;
	ServicioMusica mServer;
	static SharedPreferences prefs;
	ImageButton arrancar;
	static final int UPDATE_INTERVAL = 1000;
	private Timer timer = new Timer();
	int margen;
	static int margenAudio;
	static boolean margenAudioSuma;
	static int ampliarAudio;
	static int voyPor = 0;
	static int tipoReproduccion;
	static String varNombrePeli;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		lv = (ListView) findViewById(R.id.listView);
		lista = obtenerItems();

		final AdapterSubtitulo adapter = new AdapterSubtitulo(this, lista);
		lv.setAdapter(adapter);

		// Si realizo una pulsación larga sobre el nombrede la película lanzo un
		// intent para buscar un fichero
		nombrePeli = (TextView) findViewById(R.id.nombrePeli);
		nombrePeli.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View v) {
				Intent intent = new Intent();
				intent.setType("audio/mp3");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				// intent.setAction(Intent.ACTION_PICK);
				startActivityForResult(
						Intent.createChooser(intent, "Open Audio (mp3) file"),
						RQS_OPEN_AUDIO_MP3);
				return true;
			}
		});

		cargaPreferencias(this);
		File f = new File(varNombrePeli);
		Uri audioFileUri = Uri.fromFile(f);
		if (!"".equals(varNombrePeli) && f.exists()) {
			nombrePeli.setText(audioFileUri.getLastPathSegment());
			rellenaSubtitulos(audioFileUri);
		}

		revisaPosicionAudio();
		// Preparo la barra deslizadora con la longitud de subtitulos
		// y creo un manejador de eventos para el cambio
		seekbar = (SeekBar) findViewById(R.id.seekBar1);
		seekbar.setMax(lista.size());

		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
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

		// Relleno los subtitulos con el nombre de la película
		// rellenaSubtitulos(audioFileUri);

		arrancar = (ImageButton) findViewById(R.id.btnPlay);

		arrancar.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				PulsadoPlay();
			}
		});

		ImageButton detener = (ImageButton) findViewById(R.id.btnPausa);

		detener.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				mServer.pause();
			}
		});

		ImageButton siguiente = (ImageButton) findViewById(R.id.btnSigui);

		siguiente.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				voyPor++;
				if (arrancar.getTag() == "play") {
					PulsadoPlay();
				} else {
					mServer.seek(((lista.get(voyPor).horaIni - margenAudio) - ampliarAudio),
							((lista.get(voyPor).horaFin - margenAudio) + ampliarAudio));
					lv.setSelection(voyPor - 2);
					seekbar.setProgress(voyPor);
				}
			}
		});

		ImageButton anterior = (ImageButton) findViewById(R.id.btnPrevio);

		anterior.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				if (voyPor != 0) {
					voyPor--;
					if (arrancar.getTag() == "play") {
						PulsadoPlay();
					} else {
						mServer.seek(((lista.get(voyPor).horaIni - margenAudio) - ampliarAudio),
								((lista.get(voyPor).horaFin - margenAudio) + ampliarAudio));
						lv.setSelection(voyPor - 2);
						seekbar.setProgress(voyPor);
					}
				}
			}
		});

		lv.setOnItemLongClickListener(new OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                    int pos, long id) {
                // TODO Auto-generated method stub

                voyPor = pos;

                return true;
            }
        }); 
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			lanzarPreferencias(lv);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		cargaPreferencias(this);
		Intent mIntent = new Intent(this, ServicioMusica.class);
		bindService(mIntent, mConnection, BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mBounded) {
			unbindService(mConnection);
			mBounded = false;
		}

		Editor edPref = prefs.edit();
		edPref.putString("voyPor", voyPor + "");
		edPref.commit();
	}

	ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceDisconnected(ComponentName name) {
			Toast.makeText(MainActivity.this, "Service is disconnected", 1000)
					.show();
			mBounded = false;
			mServer = null;
		}

		public void onServiceConnected(ComponentName name, IBinder service) {
			Toast.makeText(MainActivity.this, "Service is connected", 1000)
					.show();
			mBounded = true;
			LocalBinder mLocalBinder = (LocalBinder) service;
			mServer = mLocalBinder.getServerInstance();
		}
	};

	private void revisaPosicionAudio() {
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				if (mServer.posicion() >= 
						((lista.get(voyPor).horaFin - margenAudio) + ampliarAudio)) {
					//Actualiza margenAudio de preferencias, Si se ha cambiado
					margenAudio = Integer
							.valueOf(prefs.getString("margenAudio", "0"));
					margenAudioSuma = prefs.getBoolean("margenAudioSuma", false);
					if (!margenAudioSuma)
						margenAudio = margenAudio * -1;

					ampliarAudio = Integer
							.valueOf(prefs.getString("ampliarAudio", "0"));

					if (tipoReproduccion == 2) {
						runOnUiThread(new Runnable() {
							public void run() {
								if (arrancar.getTag() == "pause") {
									PulsadoPlay();
								}
							}
						});
					}
					else {
						if (tipoReproduccion == 1) {
							runOnUiThread(new Runnable() {
								public void run() {
								voyPor++;
								mServer.seek(((lista.get(voyPor).horaIni - margenAudio) - ampliarAudio),
										((lista.get(voyPor).horaFin - margenAudio) + ampliarAudio));
								lv.setSelection(voyPor - 2);
								seekbar.setProgress(voyPor);
								}
							});
						}
						else {
							if (tipoReproduccion == 0) {
								runOnUiThread(new Runnable() {
									public void run() {
									voyPor++;
									lv.setSelection(voyPor - 2);
									seekbar.setProgress(voyPor);
									}
								});
							}
						}
					}
				}
			}
		}, 0, UPDATE_INTERVAL);
	}

	// Pulsado el botón Play
	public void PulsadoPlay() {
		// Intent intent = new Intent(MainActivity.this,
		// ServicioMusica.class);
		// startService(intent);
		mServer.seek(((lista.get(voyPor).horaIni - margenAudio) - ampliarAudio),
				((lista.get(voyPor).horaFin - margenAudio) + ampliarAudio));
		lv.setSelection(voyPor);
		// lv.setSelected(true);
		seekbar.setProgress(voyPor);
		mServer.play();
		if (arrancar.getTag() == "pause") {
			arrancar.setImageDrawable(getResources().getDrawable(
					android.R.drawable.ic_media_play));
			arrancar.setTag("play");
		} else {
			arrancar.setImageDrawable(getResources().getDrawable(
					android.R.drawable.ic_media_pause));
			arrancar.setTag("pause");
		}
	}

	// Función que lanza la actividad Preferencias
	public void lanzarPreferencias(View view) {
		Intent i = new Intent(this, Preferencias.class);
		startActivity(i);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// Rellena la lista de subtítulos la primera vez que se abre el programa
	private ArrayList<Subtitulo> obtenerItems() {
		ArrayList<Subtitulo> items = new ArrayList<Subtitulo>();

		items.add(new Subtitulo(1000, 2000,
				"Realiza una pulsación larga sobre el",
				"TÍTULO DE LA PELÍCULA", 1));
		items.add(new Subtitulo(3000, 4000, "Para seleccionar la audición",
				"deseada.", 2));
		// items.add(new Subtitulo(5000, 6000, "Patatas3", "Tuberculo3",3));
		// items.add(new Subtitulo(7000, 8000, "Patatas4", "Tuberculo4",4));
		// items.add(new Subtitulo(9000, 10000, "Patatas5", "Tuberculo5",5));
		// items.add(new Subtitulo(11000, 12000, "Patatas1", "Tuberculo1",1));
		// items.add(new Subtitulo(13000, 14000,
		// "Patatas2 albondigas chorizo meadillos con tomate y sal",
		// "Tuberculo2",2));
		// items.add(new Subtitulo(15000, 16000, "Patatas3",
		// "Tuberculo3 andoni zubi 44 el que gana es el better",3));
		// items.add(new Subtitulo(17000, 18000, "Patatas4", "Tuberculo4",4));
		// items.add(new Subtitulo(19000, 210000, "Patatas5", "Tuberculo5",5));
		//
		return items;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == RQS_OPEN_AUDIO_MP3) {
				Uri audioFileUri = data.getData();
				nombrePeli.setText(audioFileUri.getLastPathSegment());
				rellenaSubtitulos(audioFileUri);
				voyPor = 0;
				Editor edPref = prefs.edit();
				edPref.putString("voyPor", voyPor + "");
				edPref.commit();

				//Arranca servicio de música
				mServer.cambiaAudio(this);
			}
		}
	}

	// Rellena los subtitulos con los ficheros encontrados
	public void rellenaSubtitulos(Uri audioFileUri) {
		try {
			// variables locales
			int ultSubEsp = 0;
			// int margen = 333; //Tiempo antes y despues que no se tendrá en
			// cuenta de los subtitulos

			Subtitulo subEspSig = new Subtitulo(0, 0, "", "", 0);
			Subtitulo subEspTmp = new Subtitulo(0, 0, "", "", 0);
			Subtitulo subIng = new Subtitulo(0, 0, "", "", 0);

			// Uri audioFileUrlistai = data.getData();
			// nombrePeli.setText(audioFileUri.getLastPathSegment());
			srcPath = audioFileUri.getPath().substring(
					0,
					audioFileUri.getPath().length()
							- audioFileUri.getLastPathSegment().length());
			String[] listaFicheros = new String[] { "", "" };

			Editor edPref = prefs.edit();
			edPref.putString("nombrePeli", audioFileUri.getPath());
			edPref.commit();

			Boolean varAgrupar = prefs.getBoolean("agruparSubtitulos", false);
			// Tiempo antes y despues que no se tendrá en cuenta de los
			// subtitulos
			margen = Integer
					.valueOf(prefs.getString("margenSubtitulos", "333"));

			BuscadorDeFicheros.dameFicheros(srcPath, BuscadorDeFicheros
					.dameRegex(audioFileUri.getLastPathSegment().substring(0,
							audioFileUri.getLastPathSegment().length() - 4)
							+ "*.srt"), listaFicheros, false);

			// cmdReset();
			// cmdSetDataSource(srcPath);
			lista.clear();
			for (int i = 0; i < listaFicheros.length; i++) {
				File fileFichero = new File(listaFicheros[i]);

				BufferedReader bufFichero = new BufferedReader(
						new InputStreamReader(new FileInputStream(fileFichero),
								"UTF-8"));

				// Leo la primera línea del fichero

				String textoLinea = bufFichero.readLine();
				String varTexto;

				// Mientras haya líneas en el fichero
				while (textoLinea != null) {
					// Creo subtitulo temporal uno nuevo por cada objeto
					Subtitulo Tmp = new Subtitulo(0, 0, "", "", 0);

					// Pongo el ID
					Tmp.id = Long.parseLong(textoLinea) + 0;
					textoLinea = bufFichero.readLine();
					// Pongo la hora de inicio
					Tmp.horaIni = 
							Integer.parseInt(textoLinea.substring(0, 2)) * 1000 * 60 * 60 +
							Integer.parseInt(textoLinea.substring(3, 5)) * 1000 * 60 +
							Integer.parseInt(textoLinea.substring(6, 8)) * 1000 +
							Integer.parseInt(textoLinea.substring(9, 12));
					// Pongo la hora de finalización
					Tmp.horaFin = 
							Integer.parseInt(textoLinea.substring(17, 19)) * 1000 * 60 * 60 +
							Integer.parseInt(textoLinea.substring(20, 22)) * 1000 * 60 + 
							Integer.parseInt(textoLinea.substring(23, 25)) * 1000 +
							Integer.parseInt(textoLinea.substring(26, 29));

					// Saco el subtitulo del idioma (puede estar compuesto por
					// varias líneas)
					// Puede estar en HTML y hay que renderizarlo para hacerlo
					// texto plano
					textoLinea = bufFichero.readLine();
					varTexto = "";

//					while (!(textoLinea == null || "".equals(textoLinea))) {
					while (!(textoLinea == null || isNumeric(textoLinea))) {
						if (!("".equals(textoLinea))) {
							varTexto = varTexto + textoLinea + "<br>";
						}

						textoLinea = bufFichero.readLine();
					}
					// Quito el último salto de línea
					if (!("".equals(varTexto))) {
						varTexto = varTexto.substring(0, varTexto.length() - 4);
					}

					// Pongo Subtitulo en primer idioma o segundo
					if (i == 0)
						Tmp.setTextoSub(varTexto);
					else
						// list.contains(mio.horaIni);
						Tmp.setTextoSubTra(varTexto);

					subIng = (Subtitulo) Tmp.clone();

					// Si es el primer idioma
					if (i == 0) {
						lista.add(new Subtitulo(Tmp.horaIni, Tmp.horaFin,
								Tmp.textoSub, Tmp.textoSubTra, Tmp.id));
					}
					// Si es el segundo idioma
					else {
						// Busco el primer subtitulo del primer idioma
						// que incluya al subtitulo del segundo idioma
						while (ultSubEsp < lista.size()) {
							Tmp = (Subtitulo) subIng.clone();
							subEspTmp = lista.get(ultSubEsp);
							// Si fecha final segundo idioma > fecha inicio
							// primer idioma
							// if (Tmp.horaFin >= subEspTmp.horaIni &&
							// Tmp.horaIni > subEspTmp.horaFin) {
							if (Tmp.horaFin - subEspTmp.horaIni >= margen
									&& Tmp.horaIni - subEspTmp.horaFin > margen) {
								ultSubEsp++;
								continue;
							}

							// Recojo el siguiente subtitulo del primer idioma
							// si existe
							try {
								subEspSig = lista.get(ultSubEsp + 1);
							} catch (Exception e) {
								subEspSig = new Subtitulo(99999999, 99999999,
										"", "", 0);
							}

							// No hay subtitulo del primer idioma
							if (subEspTmp.horaIni > Tmp.horaFin) {
								// 1 - - - -
								// Creo un subtitulo nuevo segundo idioma sin
								// primer idioma
								lista.add(ultSubEsp, new Subtitulo(Tmp.horaIni,
										Tmp.horaFin, Tmp.textoSub,
										Tmp.textoSubTra, Tmp.id));
								// Siguiente subtitulo Ing
								break;
							} else {
								// Ya había comenzado el subtitulo del primer
								// idioma
								// if (subEspTmp.horaIni < Tmp.horaIni) {
								if (Tmp.horaIni - subEspTmp.horaIni > margen) {
									// El primer idioma continua después del
									// segundo
									// if (subEspTmp.horaFin > Tmp.horaFin) {
									if (subEspTmp.horaFin - Tmp.horaFin > margen) {
										// Hay que dividir el subtitulo
										if ("".equals(subEspTmp.textoSubTra)) {
											// 0 1 1 0 0
											// Incluir subtitulo Ing en EspT
											subEspTmp.textoSubTra = Tmp.textoSubTra;
											// Actualizar subtitulo Esp desde
											// EspT
											// lista.set(ultSubEsp, subEspTmp);
											// Siguiente subtitulo Ing
											break;
										} else {
											// 0 1 1 0 1
											if (varAgrupar) {
												// Incluir subtitulo Ing en EspT
												subEspTmp.textoSubTra = subEspTmp.textoSubTra
														+ "<br>"
														+ Tmp.textoSubTra;
												// Siguiente subtitulo Ing
												break;
											} else {
												// Hora Final Tmp = Hora Final
												// EspT
												Tmp.horaFin = subEspTmp.horaFin;
												// Hora Final EspT = Hora Inicio
												// Ing - 1
												subEspTmp.horaFin = Tmp.horaIni - 1;
												// Incluir subtitulo EspT en Tmp
												Tmp.textoSub = subEspTmp.textoSub;
												// Insertar Subtitulo Tmp
												ultSubEsp++;
												lista.add(
														ultSubEsp,
														new Subtitulo(
																Tmp.horaIni,
																Tmp.horaFin,
																Tmp.textoSub,
																Tmp.textoSubTra,
																Tmp.id));
												// Siguiente subtitulo Esp
												ultSubEsp++;
												// Siguiente subtitulo Ing
												break;
											}
										}
									} else {
										// El segundo idioma se solapa con el
										// siguiente del primer idioma
										// if (Tmp.horaFin > subEspSig.horaIni)
										// {
										if (Tmp.horaFin - subEspSig.horaIni > margen) {
											// Hay que dividir el subtitulo
											if (!"".equals(subEspTmp.textoSubTra)) {
												// 0 1 0 1 1
												if (varAgrupar) {
													// Hora Final EspT = Hora
													// Final Esp Siguiente
													subEspTmp.horaFin = subEspSig.horaFin;
													// Incluir subtitulo Esp
													// Siguiente en EspT
													subEspTmp.textoSub = subEspTmp.textoSub
															+ "<br>"
															+ subEspSig.textoSub;
													// Incluir subtitulo Ing en
													// EspT
													// subEspTmp.textoSubTra =
													// subEspTmp.textoSubTra +
													// "<br>" + Tmp.textoSubTra;
													// Hora Inicial Ingles =
													// Hora Inicio Esp
													// subIng.horaIni =
													// subEspTmp.horaIni;
													// Elimina subtitulo Esp
													// Siguiente
													lista.remove(ultSubEsp + 1);
													// Siguiente subtitulo Ing
													// break;
												} else {
													// Hora Final EspT = Hora
													// Inicio Ing - 1
													subEspTmp.horaFin = Tmp.horaIni - 1;
													// Hora Final Tmp = Hora
													// Inicio Esp Siguiente – 1
													Tmp.horaFin = subEspSig.horaIni - 1;
													// Hora Inicial Ingles =
													// Hora Inicio Esp Siguiente
													subIng.horaIni = subEspSig.horaIni;
													// Incluir subtitulo Esp en
													// Tmp
													Tmp.textoSub = subEspTmp.textoSub;
													// Incluir subtitulo Ing en
													// Tmp
													Tmp.textoSubTra = Tmp.textoSubTra;
													// Insertar Subtitulo Tmp
													ultSubEsp++;
													lista.add(
															ultSubEsp,
															new Subtitulo(
																	Tmp.horaIni,
																	Tmp.horaFin,
																	Tmp.textoSub,
																	Tmp.textoSubTra,
																	Tmp.id));
													// Actualizar subtitulo Esp
													// desde EspT
													// lista.set(ultSubEsp,
													// subEspTmp);
													// Siguiente subtitulo Esp
													ultSubEsp++;
												}
											} else {
												// 0 1 0 1 0
												if (varAgrupar) {
													// Hora Final EspT = Hora
													// Final Esp Siguiente
													subEspTmp.horaFin = subEspSig.horaFin;
													// Incluir subtitulo Esp
													// Siguiente en EspT
													subEspTmp.textoSub = subEspTmp.textoSub
															+ "<br>"
															+ subEspSig.textoSub;
													// Incluir subtitulo Ing en
													// EspT
													// subEspTmp.textoSubTra =
													// Tmp.textoSubTra ;
													// Hora Inicial Ingles =
													// Hora Inicio Esp
													subIng.horaIni = subEspTmp.horaIni;
													// Elimina subtitulo Esp
													// Siguiente
													lista.remove(ultSubEsp + 1);
												} else {
													// Hora Final EspT = Hora
													// Inicio Esp Siguiente – 1
													subEspTmp.horaFin = subEspSig.horaIni - 1;
													// Hora Inicio Tmp = Hora
													// Inicio Esp Siguiente
													// Tmp.horaIni =
													// subEspSig.horaIni;
													subIng.horaIni = subEspSig.horaIni;
													// Incluir subtitulo Ing en
													// EspT
													subEspTmp.textoSubTra = Tmp.textoSubTra;
													// Actualizar subtitulo Esp
													// desde EspT
													// lista.set(ultSubEsp,
													// subEspTmp);
													// Siguiente subtitulo Esp
													ultSubEsp++;
												}
											}
										} else {
											// Hay que dividir el subtitulo
											if (!"".equals(subEspTmp.textoSubTra)) {
												// 0 1 0 0 1
												if (varAgrupar) {
													// Hora Final EspT = Hora
													// Final Ing
													subEspTmp.horaFin = Tmp.horaFin;
													// Incluir subtitulo Ing en
													// EspT
													subEspTmp.textoSubTra = subEspTmp.textoSubTra
															+ "<br>"
															+ Tmp.textoSubTra;
													// Siguiente subtitulo Esp
													ultSubEsp++;
													// Siguiente subtitulo Ing
													break;
												} else {
													// Hora Fin Esp = Hora
													// Inicio Ing
													subEspTmp.horaFin = Tmp.horaIni - 1;
													// Hora Final Tmp = Hora Fin
													// Ing
													Tmp.horaFin = Tmp.horaFin;
													// Incluir subtitulo Esp en
													// Tmp
													Tmp.textoSub = subEspTmp.textoSub;
													// Incluir subtitulo Ing en
													// Tmp
													Tmp.textoSubTra = Tmp.textoSubTra;
													// Insertar Subtitulo Tmp
													ultSubEsp++;
													lista.add(
															ultSubEsp,
															new Subtitulo(
																	Tmp.horaIni,
																	Tmp.horaFin,
																	Tmp.textoSub,
																	Tmp.textoSubTra,
																	Tmp.id));
													// Siguiente subtitulo Esp
													ultSubEsp++;
													// Siguiente subtitulo Ing
													break;
												}
											} else {
												// 0 1 0 0 0
												// Hora Final EspT = Hora Fin
												// Ing
												subEspTmp.horaFin = Tmp.horaFin;
												// Incluir subtitulo Ing en EspT
												subEspTmp.textoSubTra = Tmp.textoSubTra;
												// Actualizar subtitulo Esp
												// desde EspT
												// lista.set(ultSubEsp,
												// subEspTmp);
												// Siguiente subtitulo Esp
												ultSubEsp++;
												// Siguiente subtitulo Ing
												break;
											}
										}
									}
								} else {
									// El primer idioma continua después del
									// segundo
									// if (subEspTmp.horaFin > Tmp.horaFin) {
									if (subEspTmp.horaFin - Tmp.horaFin > margen) {
										// El segundo idioma NO se solapa con el
										// siguiente del primer idioma
										// if (!(Tmp.horaFin >
										// subEspSig.horaIni)) {
										if (!(Tmp.horaFin - subEspSig.horaIni > margen)) {
											// No hay que dividir el subtitulo
											if (!"".equals(subEspTmp.textoSubTra)) {
												// 0 0 1 0 1
												if (varAgrupar) {
													// Hora Inicio Tmp = Hora
													// Inicio Ing
													// Tmp.horaIni =
													// Tmp.horaIni;
													// Hora Inicio EspT = Hora
													// Inicio Ing
													subEspTmp.horaIni = Tmp.horaIni;
													// Incluir subtitulo Ing en
													// EspT
													subEspTmp.textoSubTra = subEspTmp.textoSubTra
															+ "<br>"
															+ Tmp.textoSubTra;
													// Siguiente subtitulo Ing
													break;
												} else {
													// Hora Inicio Tmp = Hora
													// Inicio Ing
													Tmp.horaIni = Tmp.horaIni;
													// Hora Final Tmp = Hora
													// Inicio Esp - 1
													Tmp.horaFin = subEspTmp.horaIni - 1;
													// Incluir subtitulo Esp en
													// Tmp
													Tmp.textoSub = subEspTmp.textoSub;
													// Incluir subtitulo Ing en
													// Tmp
													Tmp.textoSubTra = Tmp.textoSubTra;
													// Insertar Subtitulo Tmp
													ultSubEsp++;
													lista.add(
															ultSubEsp,
															new Subtitulo(
																	Tmp.horaIni,
																	Tmp.horaFin,
																	Tmp.textoSub,
																	Tmp.textoSubTra,
																	Tmp.id));
													// Siguiente subtitulo Ing
													break;
												}
											} else {
												// 0 0 1 0 0
												// Hora Inicio EspT = Hora
												// Inicio Ing
												subEspTmp.horaIni = Tmp.horaIni;
												// Incluir subtitulo Ing en EspT
												subEspTmp.textoSubTra = Tmp.textoSubTra;
												// Actualizar subtitulo Esp
												// desde EspT
												// lista.set(ultSubEsp,
												// subEspTmp);
												// Siguiente subtitulo Ing
												break;
											}
										}
									} else {
										// El segundo idioma se solapa con el
										// siguiente del primer idioma
										// if (Tmp.horaFin > subEspSig.horaIni)
										// {
										if (Tmp.horaFin - subEspSig.horaIni > margen) {
											// 0 0 0 1 0
											if (varAgrupar) {
												// Hora Inicio EspT = Hora
												// Inicio Ing
												subEspTmp.horaIni = Tmp.horaIni;
												// Hora Final EspT = Hora Final
												// Esp Siguiente
												subEspTmp.horaFin = subEspSig.horaFin;
												// Incluir subtitulo Esp
												// Siguiente en EspT
												subEspTmp.textoSub = subEspTmp.textoSub
														+ "<br>"
														+ subEspSig.textoSub;
												// Elimina subtitulo Esp
												// Siguiente
												lista.remove(ultSubEsp + 1);
											} else {
												// Hora Inicio segundo idioma =
												// Hora Inicio Esp Siguiente
												subIng.horaIni = subEspSig.horaIni;
												Tmp.horaIni = subEspSig.horaIni;
												// Hora Inicio EspT = Hora
												// Inicio Ing
												// subEspTmp.horaIni =
												// Tmp.horaIni;
												// Hora Final EspT = Hora Inicio
												// Esp Siguiente – 1
												subEspTmp.horaFin = subEspSig.horaIni - 1;
												// Incluir subtitulo Ing en EspT
												subEspTmp.textoSubTra = Tmp.textoSubTra;
												// Actualizar subtitulo Esp
												// desde EspT
												lista.set(ultSubEsp, subEspTmp);
												// Siguiente subtitulo Esp
												ultSubEsp++;
											}
										} else {
											// 0 0 0 0 0
											// Hora Inicio EspT = Hora Inicio
											// Ing
											subEspTmp.horaIni = Tmp.horaIni;
											// Hora Final EspT = Hora Fin Ing
											subEspTmp.horaFin = Tmp.horaFin;
											// Incluir subtitulo Ing en EspT
											subEspTmp.textoSubTra = Tmp.textoSubTra;
											// Actualizar subtitulo Esp desde
											// EspT
											// lista.set(ultSubEsp, subEspTmp);
											// Siguiente subtitulo Esp
											ultSubEsp++;
											// Siguiente subtitulo Ing
											break;
										}
									}
								}
							}
						}
					}

					// Voy a por el siguiente subtitulo en el fichero
					// textoLinea = bufFichero.readLine();
				}
				// Cierro el fichero
				bufFichero.close();
			}

			// Actualizo el listView
			seekbar.setMax(lista.size());
			((BaseAdapter) lv.getAdapter()).notifyDataSetChanged();
			lv.setSelection(0);
			seekbar.setProgress(0);
		} catch (Exception ex) {
			Log.e("Ficheros",
					"Error al leer fichero desde tarjeta SD" + ex.getMessage());
		}
	}

	private static boolean isNumeric(String cadena){ 
		try { 
			Integer.parseInt(cadena); 
			return true; 
		} catch (NumberFormatException nfe){ 
			return false; 
		} 
	} 
	
	private static void cargaPreferencias(Context context){
		prefs = context.getSharedPreferences(
				"com.example.listviewexample01_preferences",
				Context.MODE_PRIVATE);
		varNombrePeli = prefs.getString("nombrePeli", "");

		tipoReproduccion = Integer
				.valueOf(prefs.getString("tipoReproduccion", "0"));

		voyPor = Integer.valueOf(prefs.getString("voyPor", "0"));

		margenAudio = Integer
				.valueOf(prefs.getString("margenAudio", "0"));
		margenAudioSuma = prefs.getBoolean("margenAudioSuma", false);
		if (!margenAudioSuma)
			margenAudio = margenAudio * -1;
		
		ampliarAudio = Integer
				.valueOf(prefs.getString("ampliarAudio", "0"));
	}
}
