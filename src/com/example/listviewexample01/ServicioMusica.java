package com.example.listviewexample01;

import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
//import com.aocate.media.MediaPlayer;
//import com.aocate.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

public class ServicioMusica extends Service {
	// MediaPlayer reproductor = new MediaPlayer(getApplicationContext());

	IBinder mBinder = new LocalBinder();

	MediaPlayer reproductor = new MediaPlayer();
//	public MediaPlayer reproductor = new MediaPlayer(this);

	public class LocalBinder extends Binder {
		public ServicioMusica getServerInstance() {
			return ServicioMusica.this;
		}
	}

	@Override
	public void onCreate() {
		Toast.makeText(this, "Servicio creado", Toast.LENGTH_SHORT).show();
		//reproductor = new MediaPlayer();
		preparaAudio(this);
	}

	@Override
	public int onStartCommand(Intent intenc, int flags, int idArranque) {
		Toast.makeText(this, "Servicio arrancado " + idArranque,
				Toast.LENGTH_SHORT).show();
		reproductor.start();
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		Toast.makeText(this, "Servicio detenido", Toast.LENGTH_SHORT).show();
		reproductor.stop();
	}

	@Override
	public IBinder onBind(Intent intencion) {
		return mBinder;
	}

	public void cambiaAudio(Context context) {
		preparaAudio(context);
	}

	public void seek(int posicionIni, int posicionFin) {
		reproductor.seekTo(posicionIni);
	}

	public void play() {
		if (reproductor.isPlaying() == false)
			reproductor.start();
		else
			reproductor.pause();
	}

	public void pause() {
		reproductor.pause();
	}

	public int posicion() {
		return reproductor.getCurrentPosition();
	}
	
	private void preparaAudio(Context context){
		SharedPreferences prefs = context.getSharedPreferences(
				"com.example.listviewexample01_preferences",
				Context.MODE_PRIVATE);

		String nombrePeli = prefs.getString("nombrePeli", "");

		// reproductor = MediaPlayer.create(this, Uri.fromFile(new
		// File(nombrePeli)));

		try {

			// Notification notification = new Notification(
			// R.drawable.playbackstart, path, System.currentTimeMillis());
			// nm.notify(NOTIFY_ID, notification);

			reproductor.reset();
			reproductor.setDataSource(nombrePeli);
			reproductor.prepare();
			// reproductor.start();
			reproductor.setVolume(1, 1);
			reproductor.setOnCompletionListener(new OnCompletionListener() {
				public void onCompletion(MediaPlayer arg0) {
					System.out.println("Hi i am at the End");
				}
			});
		} catch (Exception e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
}