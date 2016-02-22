/*
 * Created on 07-ago-2003
 *
 */
package simple2.representacionruta;

/**
 * @author Montserrat Sotomayor Gonzalez
 *
 */
/**
 * Esta clase representa a un cable bidireccional, con puntas de flecha en sus
 * dos extremos.
 * 
 */
public class CableBidireccional extends Cable {

	/**
	 * Crea una instancia de la clase.
	 * 
	 * @param dib
	 *            La superficie sobre la que se dibujar� este objeto.
	 * @param puntos
	 *            Las coordenadas de las rectas que componen este cable.
	 */
	public CableBidireccional(InterfaceDibujo dib, int[] puntos) {
		super(dib, puntos);
	}

	/**
	 * Pinta el cable inactivo
	 */
	@Override
	protected void pintarInactivo() {
		super.pintarInactivo();
		pintarFlechaFin();
		pintarFlechaInicio();
	}

	/**
	 * Pinta el cable activo
	 */
	@Override
	protected void pintarActivo() {
		super.pintarActivo();
		pintarFlechaFin();
		pintarFlechaInicio();
	}

}
