package semantico;

import java.util.*;

public class TablaDeSimbolos {

	private String nombre;
	private HashMap<String, Identificador> tabla;
	private TablaDeSimbolos padre;
	
	public TablaDeSimbolos(String nnom, TablaDeSimbolos nPadre){
		this.nombre = nnom;
		this.tabla = new HashMap<String, Identificador>();
		this.padre = nPadre;
	}
	
	public void tablaResultante(){
		//String result = "";
		Iterator<String> clavesAlmacenadas = this.tabla.keySet().iterator();
		while(clavesAlmacenadas.hasNext()){ 
			String clave = clavesAlmacenadas.next(); 
			System.out.println("key: " + clave + " value: " + this.tabla.get(clave).toString());
		}
	
		//return result;
	}
	
	public void agregar(String nombre, Identificador objeto){
		this.tabla.put(nombre, objeto);
	}
	
	public Identificador obtener(String nombre){
		return this.tabla.get(nombre);
	}
}
