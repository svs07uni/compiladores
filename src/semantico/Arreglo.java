package semantico;

public class Arreglo extends Identificador{

	private int dimension; //1 o 2
	private int max1;//para primer dimensi�n
	private int min1;
	private int max2;//para segunda dimensi�n
	private int min2;
        
        public Arreglo(String nombre){
            this.setNombre(nombre);
        }
        
        public void setDimension(int min, int max, int dim){
            if(dim == 1){
                this.max1 = max;
                this.min1 = min;
                
            }
            else{
                this.max2 = max;
                this.min2 = min;
            }   
            this.dimension = dim;
        }
        
        public boolean enRango (int indice1, int indice2){
            if(this.dimension == 2){
                return indice1<=this.max1 && indice1>=this.min1 && indice2<=this.max2 && indice2>=this.min2;
            }
             //else larga error de dimensiones   
            return false;
        }
        
        public boolean enRango(int indice){
            if(this.dimension == 1){
                return indice<=this.max1 && indice>=this.min1;
            }
             //else larga error de dimensiones   
            return false;
        }
        
        
}
