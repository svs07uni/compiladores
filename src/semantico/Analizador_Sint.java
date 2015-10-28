/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semantico;


import java.util.LinkedList;
import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ale
 */
public class Analizador_Sint {
	private ManejoDeErrores errores = new ManejoDeErrores();
	private SynchronousQueue buffer = new SynchronousQueue();//contiene dos tokens
	
	private String archivo;
	
	private Lexico lex;
	private Sintactico sint;
	private Thread sint2;
	
	private HashMap<String,TablaDeSimbolos> tabla_ts;
        
	public Analizador_Sint(String arch, HashMap<String,TablaDeSimbolos> t){
		this.archivo = arch;
        this.tabla_ts = t;      
	}
	
	public void comenzar(){
        // se crean los hilos del lexico y sintactico
        this.lex = new Lexico(this.archivo, buffer, errores);
        
        //Sintactico sint = new Sintactico(buffer, errores,lex);
        this.sint = new Sintactico(buffer, errores,lex, tabla_ts);
        this.sint2 = new Thread(sint);
        
        
        lex.start();     
        sint2.start();
        
        // se espera la finalizacion del lexico
        try {
            lex.join();    
        } catch (InterruptedException ex) {
        }
        
            // Significa que se acabaron los tokens y el sintactico se quedó bloqueado esperando...
        if (sint2.isAlive() && !sint.exito()){
            //pasa cuando el archivo fuente no tiene el fin de programa
            errores.agregarError(new Error(40,0,"",""));
        }
        

        // mostrar resutlados
        errores.mostrar();
        
        sint2.stop();
        
          
      
         // en caso de que el sintactico quedé bloqueado esperando mas tokens  
    }
	
	public void terminar(){
		this.sint2.stop();
	}
}
