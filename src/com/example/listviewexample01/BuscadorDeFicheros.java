package com.example.listviewexample01;

/**
 * Javier Abellán, 11 Mayo 2006
 *
 * Buscador de ficheros.
 */
import java.io.File;
import java.util.LinkedList;
import java.util.regex.Pattern;

/**
 * Se le pasa una máscara de nombre de ficheros en formato regex de java y
 * busca, recursivamente o no, todos los ficheros que cumplen dicha máscara.
 * 
 * @author Chuidiang
 */
public class BuscadorDeFicheros {
	/**
	 * Busca todos los ficheros que cumplen la máscara que se le pasa y los mete
	 * en la listaFicheros que se le pasa.
	 * 
	 * @param pathInicial
	 *            Path inicial de búsqueda. Debe ser un directorio que exista y
	 *            con permisos de lectura.
	 * @param mascara
	 *            Una máscara válida para la clase Pattern de java.
	 * @param listaFicheros
	 *            Una lista de ficheros a la que se añadirán los File que
	 *            cumplan la máscara. No puede ser null. El método no la vacía.
	 * @param busquedaRecursiva
	 *            Si la búsqueda debe ser recursiva en todos los subdirectorios
	 *            por debajo del pathInicial.
	 */
	public static void dameFicheros(String pathInicial, String mascara,
			String[] listaFicheros, boolean busquedaRecursiva) {
		int llevo = 0;
		File directorioInicial = new File(pathInicial);
		if (directorioInicial.isDirectory()) {
			File[] ficheros = directorioInicial.listFiles();
			for (int i = 0; i < ficheros.length; i++) {
				if (ficheros[i].isDirectory() && busquedaRecursiva)
					dameFicheros(ficheros[i].getAbsolutePath(), mascara,
							listaFicheros, busquedaRecursiva);
				else if (Pattern.matches(mascara, ficheros[i].getName())) {
					listaFicheros[llevo] = ficheros[i].getPath();
					llevo++;
				}
			}
		}
	}

	/**
	 * Se le pasa una máscara de fichero típica de ficheros con * e ? y devuelve
	 * la cadena regex que entiende la clase Pattern.
	 * 
	 * @param mascara
	 *            Un String que no sea null.
	 * @return Una máscara regex válida para Pattern.
	 */
	public static String dameRegex(String mascara) {
		mascara = mascara.replace(".", "\\.");
		mascara = mascara.replace("*", ".*");
		mascara = mascara.replace("?", ".");
		return mascara;
	}

	/**
	 * Main de prueba.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("\\");
		String[] ficherosJava = new String[] {};
		dameFicheros("/home/ccallado", dameRegex("*.pdf"), ficherosJava, false);
		for (int i = 0; i < ficherosJava.length; i++)
			System.out.println(ficherosJava[i]);
	}
}
