package semantico;

import java.util.HashMap;

public class Enumerado extends Identificador{
    private HashMap<Integer, String> correspondencia;//correspondencia entre enteros y el enumerado
    
    public Enumerado(String nombre){
        this.correspondencia = new HashMap<Integer, String>();
        this.setNombre(nombre);
        this.setTipo("integer");
    }
    
    public void agregar(String elto){
        this.correspondencia.put(this.correspondencia.size()+1, elto);
    }
    
    public boolean enRango(String elto){
        return this.correspondencia.containsValue(elto);
    }
}
