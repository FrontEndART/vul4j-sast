/*
 * Created on 07-ago-2003
 *
 */
package simple2.rutadedatos;
/**
 * @author Montserrat Sotomayor Gonzalez
 *
 */

/**
 * Excepci�n que se lanza cuando termina la simulaci�n.
 */
public class SimulacionFinalizadaException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3536272960457696642L;

	/**
	 * Crea una instancia de la clase
	 * @param mensaje El mensaje de la excepcion
	 */
	public SimulacionFinalizadaException(String mensaje)
	{
		super(mensaje);
	}
}
