package semantico;

import java.util.Stack;

public class Analizador_Sem {

	public static void main(String[] args) {
		
            
                //Analizador_Sint AS = new Analizador_Sint(args[0]);
        HashMap<String,TablaDeSimbolos> tabla_ts = new HashMap<String,TablaDeSimbolos>();
		Analizador_Sint as = new Analizador_Sint("//home//sam//NetBeansProjects//Compiladores//src//semantico//prueba.pas", tabla_ts);
		
		as.comenzar();
		
		//as.terminar();
	}

}
