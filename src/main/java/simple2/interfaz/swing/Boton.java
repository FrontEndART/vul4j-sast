/*
 * Created on 05-jul-2003
 *
 */

package simple2.interfaz.swing;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @author Montserrat Sotomayor Gonzalez
 *
 */

/**
 * Esta clase es utilizada para crear objetos "bot�n" que cambian de color
 * cuando el rat�n pasa sobre ellos.
 */
public class Boton extends JToggleButton{

	/**
	 * Almacena el color del bot�n en estado normal.
	 */
	private Color colorNormal;
	
	/**
	 * Almacena el color del bot�n cuando el rat�n est� sobre �l.
	 */
	private Color colorSobre = new Color (192, 192, 210);
	
	/**
	 * Crea una instancia de la clase con el color de fondo por defecto. 
	 */
	public Boton() {
		super();
		colorNormal = getBackground ();
		addMouseListener (new ManejadorRaton ());
	}
	
	/**
	* Crea una instancia de la clase con la acci�n asociada 
	* a El color de fondo es por defecto.
	*
	* @param a Acci�n que realiza el bot�n cuando es presionado.
	*/
	public Boton (Action a)
	{
		super (a);
		colorNormal = getBackground();
		addMouseListener (new ManejadorRaton ());
	}
	
	/**
	* Crea una instancia de la clase con una etiqueta que se le pasa como argumento.
	* @param etiqueta Cadena de texto.
	*/
	public Boton (String etiqueta)
	{
		super (etiqueta);
		colorNormal = getBackground ();
		addMouseListener (new ManejadorRaton ());
	}
	
	/**
	* Crea una instancia de la clase con el icono que se le pasa como argumento.
	* @param icono Imagen que posee el bot�n.
	*/	
	public Boton (Icon icono)
	{
		super (icono);
		colorNormal = getBackground ();
		addMouseListener (new ManejadorRaton ());
	}
	
	/**
	* Crea una instancia de la clase con una etiqueta y un icono que son 
	* pasados como argumentos.
	* @param etiqueta El texto que se mostrar� en el bot�n.
	* @param icono El icono que se mostrar� en el bot�n.
	*/
	public Boton (String etiqueta, Icon icono)
	{
		super (etiqueta, icono);
		colorNormal = getBackground ();
		addMouseListener (new ManejadorRaton ());
	}
	
	
    /**
	* Esta clase permite que cuando el cursor del rat�n pase por encima
	* del bot�n cambie el color de fondo del mismo.
	* Cuando el cursor sale se vuelve a colocar el color que tenia inicialmente.
	*/
	class ManejadorRaton extends MouseAdapter
	{
		/**
		 * Constructor de la clase.
		 */
		public ManejadorRaton()
		{
			 super();
		}
		
		/**
		 * Cambia el color de fondo del bot�n.
		 * @param me Evento del rat�n.
		 */
		public void mouseEntered (MouseEvent me)
		{
			if (isEnabled ())
				setBackground (colorSobre);
			else 
				setBackground (colorNormal);
		}
		
		/**
		 * Restaura el color del fondo del bot�n que tenia inicialmente.
		 * @param me Evento del rat�n.
		 */
		public void mouseExited (MouseEvent me)
		{
			setBackground (colorNormal);
		}
	}

}
