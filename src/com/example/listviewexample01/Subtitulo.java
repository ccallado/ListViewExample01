package com.example.listviewexample01;

import java.text.DecimalFormat;

public class Subtitulo implements Cloneable {
    @Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

	protected int horaIni;
    protected int horaFin;
    protected String textoSub;
    protected String textoSubTra;
    protected long id;
    
    public Subtitulo(int horaIni, int horaFin, String textoSub, String textoSubTra){
    	super();
    	this.horaIni=horaIni;
    	this.horaFin=horaFin;
    	this.textoSub=textoSub;
    	this.textoSubTra=textoSubTra;
    }

    public Subtitulo(int horaIni, int horaFin, String textoSub, String textoSubTra, long id){
    	super();
    	this.horaIni=horaIni;
    	this.horaFin=horaFin;
    	this.textoSub=textoSub;
    	this.textoSubTra=textoSubTra;
    	this.id=id;
    }

	public int getHoraIni() {
		return horaIni;
	}

	public String getHoraIniT() {
		DecimalFormat df = new DecimalFormat("00");
		return String.format("%s:%s:%s", 
				df.format(horaIni / (60 * 60 * 1000) % 24),
				df.format(horaIni / (60 * 1000) % 60),
				df.format(horaIni / 1000 % 60));
	}

	public void setHoraIni(int horaIni) {
		this.horaIni = horaIni;
	}

	public int getHoraFin() {
		return horaFin;
	}

	public String getHoraFinT() {
		DecimalFormat df = new DecimalFormat("00");
		return String.format("%s:%s:%s", 
				df.format(horaFin / (60 * 60 * 1000) % 24),
				df.format(horaFin / (60 * 1000) % 60),
				df.format(horaFin / 1000 % 60));
	}

	public void setHoraFin(int horaFin) {
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
