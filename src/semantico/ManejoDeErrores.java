package semantico;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

public class ManejoDeErrores {

	private LinkedList errores;
        
        public ManejoDeErrores(){
            errores = new LinkedList();
        }
	
	public void agregarError(Error e){
		errores.addFirst(e);
	}
        
        public boolean esVacio(){
            return errores.isEmpty();
        }
        
        public void mostrar(){
            if (errores.isEmpty())
                System.out.println("COMPILACION EXITOSA!");
            else{
    
                Error e1 = (Error) errores.removeLast();
                e1.mostrarError();


            }
        }
}
