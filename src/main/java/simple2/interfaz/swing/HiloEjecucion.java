package simple2.interfaz.swing;
/*
 * Created on 11-ago-2003
 *
 */

import simple2.rutadedatos.SecuenciadorMicroprograma;
import simple2.rutadedatos.SimulacionFinalizadaException;
/**
 * @author Montserrat Sotomayor Gonzalez
 *
 */
/**
 * 
 * Clase utilzada para encapsular los hilos
 */
public class HiloEjecucion extends Thread {

	/**
	 * Simulador de la ruta de datos.
	 */	
	private SecuenciadorMicroprograma mic = null;
		
	/**
	 * Indica si se debe terminar la simulacion. 
	 */	
	private boolean terminar = false;
		
	/**
	 * Indica si la simulaci�n est� pausada.
	 */	
	private boolean pausado = false;
		
	/**
	 * Indica el tiempo que esperar� entre subciclos.
	 */	
	private int tSubciclo = 1000;
		
	/**
	 * Crea una instancia de la clase.
	 * @param mic El simulador de la ruta de datos a utilizar.
	 */	
	public HiloEjecucion (SecuenciadorMicroprograma mic)
	{
		super();
		this.mic = mic;
	}
		
	/**
	 * M�todo de ejecuci�n del hilo.
	 */	
	public void run ()
	{
		int acumulado = 0;
		int paso = 100;
		try
		{
			mic.EjecutarSubciclo();
			while (!terminar)
			{
				try{
					Thread.sleep (paso);
					}
				catch(InterruptedException ie){
				}
				if (!pausado)
				{
					acumulado += paso;
					if (acumulado >= tSubciclo)
					{
						acumulado=0;
						mic.EjecutarSubciclo();
					}
				}
			}
			mic.Detener();
		}
		catch (SimulacionFinalizadaException e)
		{
			mic.Detener();
			return;
		}
	}
		
	/**
	 * Detiene la ejecucion del hilo.
	 */	
	public void detener ()
	{
		terminar = true;
	}
		
	/**
	 * Nos indica si el hilo est� pausado.
	 * @return 	True: si el hilo est� pausado
	 * 			False:en otro caso
	 */
	public boolean GetPausado()
	{
		return this.pausado;
	}
		
	/**
	 * Detiene y reanuda la ejecucion del hilo.
	 */	
	public void CambiarPausado ()
	{
		pausado = !pausado;
	}
		
	/**
	 * Establece el tiempo que durar� cada subciclo.
	 * @param valor El tiempo en ms que durar� cada subciclo. 
	 */	
	public void SetTSubciclo (int valor)
	{
		this.tSubciclo = valor;
	}
}