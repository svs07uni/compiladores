package semantico;

import java.util.concurrent.Semaphore;

public class Token {
	private String nombre;
	private String atributos;
        private String lexema;
        private int linea;
        
	public Token(String nombre, String atributos, String lexema, int nlinea){
		this.nombre = nombre;
		this.atributos = atributos;
                this.linea = nlinea;
                this.lexema=lexema;
                
	}
        
        public Token(){
            
        }
	
	public String getNombre(){
		return this.nombre;
	}
	
	public String getAtributo(){
		return atributos;
	}
        
        public int getLinea(){
		return linea;
	}
        
        public String getLexema(){
            return lexema;
        }
}
