/*
 * Created on 28-jul-2003
 *
 */

package simple2.representacionruta;

import java.awt.Color;

/**
 * 
 * @author Montserrat Sotomayor Gonzalez
 *
 */
/**
 * 
 * Esta clase representa un dibujo de un rect�ngulo con un texto en su interior.
 */
public class CajaRegistro extends ElementoDibujable {
	/**
	 * Posici�n x de la esquina superior izquierda de la caja.
	 */
	private int x;

	/**
	 * Posici�n y de la esquina superior izquierda de la caja.
	 */
	private int y;

	/**
	 * Ancho de la caja.
	 */
	private int ancho;

	/**
	 * Alto de la caja.
	 */
	private int alto;

	/**
	 * Texto que se muestra dentro de la caja.
	 */
	private String texto;

	/**
	 * Crea una instancia de la clase. El ancho y el alto son valores por
	 * defecto
	 * 
	 * @param dib
	 *            La superficie sobre la que se debe dibujar este objeto.
	 * @param x
	 *            Posici�n x de la esquina superior izquierda de la caja
	 * @param y
	 *            Posici�n y de la esquina superior izquierda de la caja.
	 * @param texto
	 *            Texto a mostrar dentro de la caja.
	 */
	public CajaRegistro(InterfaceDibujo dib, int x, int y, String texto) {
		this(dib, x, y, 60, 20, texto);
	}

	/**
	 * Crea una instancia de la clase.
	 * 
	 * @param dib
	 *            La superficie sobre la que se debe dibujar este objeto.
	 * @param x
	 *            Posici�n x de la esquina superior izquierda de la caja.
	 * @param y
	 *            Posici�n y de la esquina superior izquierda de la caja.
	 * @param ancho
	 *            El ancho de la caja de texto.
	 * @param alto
	 *            El alto de la caja de texto.
	 * @param texto
	 *            Texto a mostrar dentro de la caja.
	 */
	public CajaRegistro(InterfaceDibujo dib, int x, int y, int ancho, int alto, String texto) {
		super(dib);
		this.x = x;
		this.y = y;
		this.ancho = ancho;
		this.alto = alto;
		this.texto = texto;
	}

	/**
	 * Pinta la caja de Registro activa
	 */
	@Override
	protected void PintarActivo() {
		this.dibujo.clean(this.x, this.y, this.ancho, this.alto);
		this.dibujo.dibujarRectangulo(Color.RED, this.x, this.y, this.ancho, this.alto);
		this.dibujo.dibujarTexto(Color.BLACK, this.x + 4, this.y + 12, this.texto);
	}

	/**
	 * Pinta la caja de Registro inactiva
	 */
	@Override
	protected void PintarInactivo() {
		this.dibujo.clean(this.x, this.y, this.ancho, this.alto);
		this.dibujo.dibujarRectangulo(Color.BLACK, this.x, this.y, this.ancho, this.alto);
		this.dibujo.dibujarTexto(Color.BLACK, this.x + 4, this.y + 12, this.texto);
	}

	/**
	 * Escribe el texto dentro de la caja de Registro
	 * 
	 * @param texto
	 *            El texto que se va a escribir
	 */
	@Override
	public void setText(String texto) {
		this.texto = texto;
		this.Repintar();
	}

	/**
	 * Obntener el texto de la caja de Registro
	 * 
	 * @return El texto que nos devuelve
	 */
	@Override
	public String getText() {
		return this.texto;
	}
}
