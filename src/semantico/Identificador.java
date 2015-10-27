package semantico;

public class Identificador {

	private String nombre;
	private String tipo;
	
	public Identificador (String nom){
	    nombre=nom;
	}
	
	public String toString(){
		return "Nombre:"+this.nombre+"\nTipo:"+this.tipo;
		
	}
        
        public String getTipo(){
            return this.tipo;
        }
        
        public void setTipo(String tipo){
            this.tipo = tipo;
        }
        
        public void setNombre(String nom){
            this.nombre = nom;
        }
        
        public boolean esCompatible(Identificador id){
            return this.getTipo().equalsIgnoreCase(id.getTipo());
        }  
        
}
