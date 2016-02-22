/*
 * Created on 28-jul-2003
 *
 */

package simple2.ensamblador;
import java.util.Hashtable;

/**
 * @author Montserrat Sotomayor Gonzalez
 *
 */

/**
 * Esta clase es la encargada de verificar si las intrucciones aritm�ticas
 * tienen el formato correcto y las codifica.
 */
public class InstruccionAritmetica extends InstruccionGeneral{

	/**
	* Contiene las instrucciones que maneja la clase con su c�digo de
	* operaci�n.
	*/
	private Hashtable<String, Integer> tabla;

	/**
	* Crea una instancia de la clase e inicializa el atributo 'tabla' con las
	* instrucciones .
	*/
	public InstruccionAritmetica() {
		this.tabla = new Hashtable<> ();

		this.tabla.put ("LODD", new Integer(0x01));
		this.tabla.put ("LODI", new Integer(0x02));
		this.tabla.put ("STOD", new Integer(0x03));
		this.tabla.put ("ADDD", new Integer(0x04));
		this.tabla.put ("ADDI", new Integer(0x05));
		this.tabla.put ("SUBD", new Integer(0x06));
		this.tabla.put ("SUBI", new Integer(0x07));
	}
	
	/**
	 * Traduce la instrucci�n aritm�tica.
	 * La instrucion debe estar validada previamente con el metodo 
	 *
	 * @param instruccion Instrucci�n con su nombre y parametros.
	 * @param linea Linea en la que aparece la instrucci�n.
	 *
	 * @return La codificaci�n de la instrucci�n.
	 */
	@Override
	public short codificar (String instruccion, int linea)
		{		
			
		int codigo;
		String[] cadena = separarOperandos (instruccion);
		int inmediato=Integer.parseInt(cadena[1]);
		Object c = this.tabla.get (cadena[0]);
		codigo=((Integer) c).intValue();
		codigo=codigo<<11;	
		return ((short) (codigo+inmediato));
		
		}
		
	/**
	 * Comprueba que la instrucci�n aritm�tica que se
	 * va a codificar tenga el formato correcto.
	 *
	 * @return 	Cadena vacia si no se han producido errores.
	 * 			Cadena con un mensaje que indica el motivo del error en la sintaxis.
	 * @param instruccion Instrucci�n con su nombre y parametros.
	 * @param linea Linea en la que aparece la instrucci�n.
	 * @throws ErrorCodigoException   si ocurre algun error en el c�digo, la excepcion
	 * contiene el mensaje de error
	 */	
	@Override
	public String validar (String instruccion, int linea) throws ErrorCodigoException
		{
		String[]cadena = separarOperandos (instruccion);
	
		if (contieneCaracteresNoValidos (instruccion)){
			return "Linea: " + linea + ". " + CONTIENE_CARACTERES_INVALIDOS;
			}
		Object c = this.tabla.get (cadena[0]);
		if (c == null){
			return "Linea: " + linea +". No se reconoce la instruccion " + cadena[0] + "\n";
		}	
		if (cadena.length != 2){
			return "Linea: " + linea + ". El n�mero de par�metros no es correcto\n";		
		}
		try{
			Integer.parseInt(cadena[1]);
		}
		catch(Exception e){
			return "Linea: "+linea+". El segundo parametro tiene que ser un n�mero\n";
		}
			
		return "";
	}

}
