package com.example.listviewexample01;

import java.text.DecimalFormat;

public class Subtitulo {
    protected long horaIni;
    protected long horaFin;
    protected String textoSub;
    protected String textoSubTra;
    protected long id;
    
    public Subtitulo(long horaIni, long horaFin, String textoSub, String textoSubTra){
    	super();
    	this.horaIni=horaIni;
    	this.horaFin=horaFin;
    	this.textoSub=textoSub;
    	this.textoSubTra=textoSubTra;
    }

    public Subtitulo(long horaIni, long horaFin, String textoSub, String textoSubTra, long id){
    	super();
    	this.horaIni=horaIni;
    	this.horaFin=horaFin;
    	this.textoSub=textoSub;
    	this.textoSubTra=textoSubTra;
    	this.id=id;
    }

	public long getHoraIni() {
		return horaIni;
	}

	public String getHoraIniT() {
		DecimalFormat df = new DecimalFormat("00");
		return String.format("%s:%s:%s", 
				df.format(horaIni / (60 * 60 * 1000) % 24),
				df.format(horaIni / (60 * 1000) % 60),
				df.format(horaIni / 1000 % 60));
	}

	public void setHoraIni(long horaIni) {
		this.horaIni = horaIni;
	}

	public long getHoraFin() {
		return horaFin;
	}

	public String getHoraFinT() {
		DecimalFormat df = new DecimalFormat("00");
		return String.format("%s:%s:%s", 
				df.format(horaFin / (60 * 60 * 1000) % 24),
				df.format(horaFin / (60 * 1000) % 60),
				df.format(horaFin / 1000 % 60));
	}

	public void setHoraFin(long horaFin) {
		this.horaFin = horaFin;
	}

	public String getTextoSub() {
		return textoSub;
	}

	public void setTextoSub(String textoSub) {
		this.textoSub = textoSub;
	}

	public String getTextoSubTra() {
		return textoSubTra;
	}

	public void setTextoSubTra(String textoSubTra) {
		this.textoSubTra = textoSubTra;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
}
