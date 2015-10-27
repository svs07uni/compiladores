package semantico;

import java.util.Stack;

public class Analizador_Sem {

	public static void main(String[] args) {
		
            
                //Analizador_Sint AS = new Analizador_Sint(args[0]);
		Analizador_Sint as = new Analizador_Sint("//home//sam//NetBeansProjects//Compiladores//src//semantico//prueba.pas");
		
		as.comenzar();
		
		//as.terminar();
	}

}
