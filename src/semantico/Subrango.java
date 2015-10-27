package semantico;


public class Subrango extends Identificador{

	private int min;
	private int max;
        
        public Subrango(String nombre, int min, int max){
        this.setNombre(nombre);
        this.setTipo("integer");
        this.min = min;
        this.max = max;
        
    }
        
        public boolean enRango(int elto){
            return elto<=this.max && elto>=this.min;
        }
}
