package semantico;


import java.util.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Sintactico implements Runnable{

	private Lexico lex;
	private Token un_token;
        private SynchronousQueue buffer;
	private ManejoDeErrores manejo;
        private String lexemaAnterior;
        private Token tk;
        private boolean lambda;
        private Lexico lexico;
        private boolean finLexico;
        private boolean exito;
        
        private HashMap<String,TablaDeSimbolos> tabla_ts ;
        private TablaDeSimbolos ts;
        
	public Sintactico(SynchronousQueue nbuffer, ManejoDeErrores e2, Lexico l, HashMap<String,TablaDeSimbolos> t){
            this.buffer = nbuffer;
            manejo=e2;
            this.lexemaAnterior="";
            lambda=false;
            lexico=l;
            finLexico=false;
            exito=false;
            
            this.tabla_ts = t;
	}
        
        public boolean exito(){
            return exito;
        }
        public void terminarAnalisis(){
            
        	lexico.stop();
        	
            
            //lexico.stop();

            //System.exit(0);
            //this.stop();
        }
        
        public void run(){
          try{
        	program();
                
            exito=true;
  
            // en caso de que exitan cosas despues del fin del programa
            lambda=false;
            tk=this.obtenerSiguienteToken();
            if (tk != null){
                manejo.agregarError(new Error(11,tk.getLinea(),"",""));
                terminarAnalisis();
            }
          }catch(ThreadDeath e){
        	  
          }
        }
	
        private boolean match(Token t, String m){
            boolean band;
            if(!t.getNombre().equalsIgnoreCase(m)){
                band=false;
            }else{
                band=true;
                //System.out.println("match con exito:"+m);
            }  
            return band;
        }
        
	private void program(){
                        
            tk = this.obtenerSiguienteToken();
            if ( !match(tk, "program")){
                manejo.agregarError(new Error(20,tk.getLinea(),"program",null));
                terminarAnalisis();//MATAR HILO
            }
            lexemaAnterior=tk.getLexema();
            
            tk = this.obtenerSiguienteToken();
            if ( !match(tk, "id")){
                manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                terminarAnalisis();//MATAR HILO
            }
            
            // crear nueva TS y agregarla a la tabla_ts
            ts = new TablaDeSimbolos(tk.getLexema(), null);
            tabla_ts.put(tk.getLexema(), ts);
            //
            
            
            lexemaAnterior=tk.getLexema();
           
            tk = this.obtenerSiguienteToken();
            if ( !match(tk, "punto_coma")){
                manejo.agregarError(new Error(22,tk.getLinea(),";",lexemaAnterior));
                terminarAnalisis();//MATAR HILO
            }
            lexemaAnterior=tk.getLexema();
            
            declaraciones();
            
            System.out.println("Fin de carga de las TS");
            
            sentenciaCompuesta();
            
            
            tk = this.obtenerSiguienteToken();

            if (!match(tk, "punto")){
                manejo.agregarError(new Error(39,tk.getLinea(),".",lexemaAnterior));
                terminarAnalisis();//MATAR HILO
                lexemaAnterior = tk.getLexema();
            }
            lexemaAnterior = tk.getLexema();
            
	}
        
        private Token obtenerSiguienteToken(){ 
            Token t=null;
            if (!lambda){
                try {
                    t = (Token) buffer.take();
                    //System.out.println("BUFFER: AS <-- '"+t.getNombre()+"'");
                } catch (InterruptedException ex) {
                }
            }else{
                lambda=false;
                t=tk;
            }
            return t;
        }
        
	
        private void declaraciones(){
                    declaracion_constante();
                    declaracion_tipo();
                    declaracion_var();
                    declaracion_procedimiento();
                    declaracion_funcion();
        }
        
        private void declaracion_procedimiento(){
            tk = this.obtenerSiguienteToken();
            if (match(tk,"procedure")){
                lexemaAnterior=tk.getLexema();
                tk = this.obtenerSiguienteToken();
                if (!match(tk,"id")){
                    manejo.agregarError(new Error(23,tk.getLinea(),"",lexemaAnterior));
                    terminarAnalisis();
                }
                lexemaAnterior=tk.getLexema();
                
                argumentos();
                
                tk = this.obtenerSiguienteToken();
                if (!match(tk,"punto_coma")){
                    manejo.agregarError(new Error(22,tk.getLinea(),";",lexemaAnterior));
                    terminarAnalisis();
                }
                lexemaAnterior=tk.getLexema();
                
                declaraciones();
                sentenciaCompuesta();
                
                tk = this.obtenerSiguienteToken();
                if (!match(tk,"punto_coma")){
                    manejo.agregarError(new Error(22,tk.getLinea(),";",lexemaAnterior));
                    terminarAnalisis();
                }
                lexemaAnterior=tk.getLexema();
                
                declaracion_procedimiento();
                
            }else
                lambda=true;
        }
        
        
        private void declaracion_funcion(){
            tk = this.obtenerSiguienteToken();
            if (match(tk,"function")){
                lexemaAnterior=tk.getLexema();
                tk = this.obtenerSiguienteToken();
                if (!match(tk,"id")){
                    manejo.agregarError(new Error(23,tk.getLinea(),"",lexemaAnterior));
                    terminarAnalisis();
                }
                lexemaAnterior=tk.getLexema();
                
                argumentos();
                
                tk = this.obtenerSiguienteToken();
                if (!match(tk,"dos_puntos")){
                    manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                    terminarAnalisis();
                }
                lexemaAnterior=tk.getLexema();
                
                tipo_simple();
                
                tk = this.obtenerSiguienteToken();
                if (!match(tk,"punto_coma")){
                    manejo.agregarError(new Error(22,tk.getLinea(),";",lexemaAnterior));
                    terminarAnalisis();
                }
                lexemaAnterior=tk.getLexema();
                
                declaraciones();
                sentenciaCompuesta();
                
                tk = this.obtenerSiguienteToken();
                if (!match(tk,"punto_coma")){
                    manejo.agregarError(new Error(22,tk.getLinea(),";",lexemaAnterior));
                    terminarAnalisis();
                }
                lexemaAnterior=tk.getLexema();
                
                declaracion_funcion();
                
            }else
                lambda=true;
        }
        
        private void argumentos(){
            tk = this.obtenerSiguienteToken();
            if (match(tk,"parentesis_izq")){
                lexemaAnterior=tk.getLexema();
                
                lista_argumentos();
                
                tk = this.obtenerSiguienteToken();
                if (!match(tk,"parentesis_der")){
                    manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                    terminarAnalisis();
                }
                lexemaAnterior=tk.getLexema();
            }else
                lambda=true;
        }
        
        private void lista_argumentos(){
            lista_id();
            
            tk = this.obtenerSiguienteToken();
            if (!match(tk,"dos_puntos")){
                manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                terminarAnalisis();
            } 
            lexemaAnterior=tk.getLexema();
            
            tipo_simple();
            lista_argumentos2();
        }
        
        private void lista_argumentos2(){
            tk = this.obtenerSiguienteToken();
            if (match(tk,"punto_coma")){
                lexemaAnterior=tk.getLexema();
                lista_argumentos();
                lista_argumentos2();
            }else
                lambda=true;         
        }
        
        private void declaracion_var(){
            tk = this.obtenerSiguienteToken();
            if (match(tk,"var")){
                lexemaAnterior=tk.getLexema();
                lista_id();
                tk = this.obtenerSiguienteToken();
                if (!match(tk,"dos_puntos")){
                    manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                    terminarAnalisis();
                }
                lexemaAnterior=tk.getLexema();
                
                tipo_simple();
                
                tk = this.obtenerSiguienteToken();
                if (!match(tk,"punto_coma")){
                    manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                    terminarAnalisis();
                }
                lexemaAnterior=tk.getLexema();
                declaracion_var2();
            }else
                lambda=true;
        }
        
        private void declaracion_var2(){
            tk = this.obtenerSiguienteToken();
            if (match(tk,"id")){
                lambda=true;
                lista_id();
                tk = this.obtenerSiguienteToken();
                if (!match(tk,"dos_puntos")){
                    manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                    terminarAnalisis();
                }
                lexemaAnterior=tk.getLexema();
                
                tipo_simple();
                
                tk = this.obtenerSiguienteToken();
                if (!match(tk,"punto_coma")){
                    manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                    terminarAnalisis();
                }
                lexemaAnterior=tk.getLexema();
                declaracion_var2();
            }else
                lambda=true;
        }
        
        private void declaracion_tipo(){
            tk=this.obtenerSiguienteToken();
            if (match(tk,"type")){
                lexemaAnterior=tk.getLexema(); 
                lista_id();
                tk=this.obtenerSiguienteToken();
                if (!match(tk,"igual")){
                    manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                    terminarAnalisis();
                }
                lexemaAnterior=tk.getLexema();
           
                lista_tipo();
                
                tk=this.obtenerSiguienteToken();
                if (!match(tk,"punto_coma")){
                    manejo.agregarError(new Error(22,tk.getLinea(),";",lexemaAnterior));
                    terminarAnalisis();
                }
                lexemaAnterior=tk.getLexema();
                
                declaracion_tipo2();
            }else
                lambda=true;
        }
        
        private void declaracion_tipo2(){
            tk=this.obtenerSiguienteToken();
            lambda=true;
            if (match(tk,"id")){                
                lista_id();
                tk=this.obtenerSiguienteToken();
                if (!match(tk,"igual")){
                    manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                    terminarAnalisis();
                }
                lexemaAnterior=tk.getLexema();
           
                lista_tipo();
                
                tk=this.obtenerSiguienteToken();
                if (!match(tk,"punto_coma")){
                    manejo.agregarError(new Error(22,tk.getLinea(),";",lexemaAnterior));
                    terminarAnalisis();
                }
                lexemaAnterior=tk.getLexema();
                declaracion_tipo2();
            }
        }
        
        private void lista_tipo(){
            tk=this.obtenerSiguienteToken();
            if(tk.getNombre().equals("array")){
            	lambda=true;
                declaracion_array();                
            }
            else{
            	if(tk.getNombre().equals("parentesis_izq")){
            		lambda=true;
            		declaracion_enum();
            	}
            	else{
            		if(tk.getNombre().equals("record")){
            			lambda=true;
            			declaracion_registro();
            		}
            		else{
            			String n = tk.getNombre();
            			String a = tk.getLexema();
            			if (n.equalsIgnoreCase("numero") || a.equalsIgnoreCase("+") || a.equalsIgnoreCase("-")){
            				lambda=true;
            				declaracion_subrango();
            			}else{
                                        if (n.equalsIgnoreCase("id") ){
                                            lexemaAnterior = tk.getLexema();
                                            tk = this.obtenerSiguienteToken();
                                            if (match(tk,"punto_punto")){
                                                lexemaAnterior = tk.getLexema();
                                                indice();
                                            }else
                                                lambda=true;
                                            
                                        }else{
                                            if (n.equalsIgnoreCase("integer") || n.equalsIgnoreCase("boolean")){
                                                lexemaAnterior = tk.getLexema();
                                            }else{
                                                manejo.agregarError(new Error(24,tk.getLinea(),tk.getNombre(),lexemaAnterior));
                                                terminarAnalisis(); 
                                            }
                                        }
            				
            			}
            		}
            	}
            }
            	
            /*switch (tk.getNombre()){
                case "array":
                    lambda=true;
                    declaracion_array();
                    break;
                case "parentesis_izq":
                    lambda=true;
                    declaracion_enum();
                    break;
                case "record":
                    lambda=true;
                    declaracion_registro();
                    break;
                default:{
                    String n = tk.getNombre();
                    String a = tk.getLexema();
                    if (n.equalsIgnoreCase("numero") || n.equalsIgnoreCase("id") || a.equalsIgnoreCase("+") || a.equalsIgnoreCase("-")){
                        lambda=true;
                        declaracion_subrango();
                    }else{
                        manejo.agregarError(new Error(24,tk.getLinea(),tk.getNombre(),lexemaAnterior));
                        terminarAnalisis();
                    }
                    
                }
            }*/
        }
        
        private void declaracion_subrango(){
            tk=this.obtenerSiguienteToken();
            if (tk.getLexema().equalsIgnoreCase("-") || tk.getLexema().equalsIgnoreCase("+") || 
                     tk.getNombre().equalsIgnoreCase("numero") ){
               // System.out.println("jhgjgjhgjg");
                lambda=true;
                indice();
                tk=this.obtenerSiguienteToken();
                if (!match(tk,"punto_punto")){
                    manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                    terminarAnalisis();
                }
                lexemaAnterior=tk.getLexema();
                indice();    
            }else{
                if (match(tk,"id")){
                    lexemaAnterior = tk.getLexema();
                    tk = this.obtenerSiguienteToken();
                    if (match(tk,"punto_punto")){
                        lexemaAnterior = tk.getLexema();
                        indice();
                    }else// Se puede tratar de un subrango definido, es decir un ID
                        lambda=true;
                    
                }else{
                    manejo.agregarError(new Error(32,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                    this.terminarAnalisis();// kill thread
                }
            }
//            

        }
        
        private void declaracion_registro(){
            tk=this.obtenerSiguienteToken();
            if (!match(tk,"record")){
                manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                terminarAnalisis();
            }
            lexemaAnterior=tk.getLexema();
            
            lista_id();
            
            tk=this.obtenerSiguienteToken();
            if (!match(tk,"dos_puntos")){
                manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                terminarAnalisis();
            }
            lexemaAnterior=tk.getLexema();
            
            tipo_dato();
            declaracion_registro2();
            
//            tk=this.obtenerSiguienteToken();
//            if (match(tk,"punto_coma")){
//                manejo.agregarError(new Error(40,tk.getLinea(),";",lexemaAnterior));
//                terminarAnalisis();
//            }
//            lexemaAnterior=tk.getLexema();
            
            tk=this.obtenerSiguienteToken();
            if (!match(tk,"end")){
                manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                terminarAnalisis();
            }
            lexemaAnterior=tk.getLexema();
            
        }
        
        private void end(){
            tk = this.obtenerSiguienteToken();
            if (match(tk,"end")){
                manejo.agregarError(new Error(29,tk.getLinea(),";",lexemaAnterior));
                terminarAnalisis();
            }else
                lambda=true;
        }
        
        private void declaracion_registro2(){
            tk=this.obtenerSiguienteToken();
            if (match(tk,"punto_coma")){
                lexemaAnterior=tk.getLexema();
                end();
                lista_id();

                tk=this.obtenerSiguienteToken();
                if (!match(tk,"dos_puntos")){
                    manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                    terminarAnalisis();
                }
                lexemaAnterior=tk.getLexema();

                tipo_dato();
                declaracion_registro2();
            }else
                lambda=true;
        }
        
        private void declaracion_enum(){
            tk=this.obtenerSiguienteToken();
            if (!match(tk,"parentesis_izq")){
                manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                terminarAnalisis();
            }
            lexemaAnterior=tk.getLexema();
            
            tk=this.obtenerSiguienteToken();
            if (!match(tk,"id")){
                manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                terminarAnalisis();
            }
            lexemaAnterior=tk.getLexema();
            
            declaracion_enum2();
            
            tk=this.obtenerSiguienteToken();
            if (!match(tk,"parentesis_der")){
                manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                terminarAnalisis();
            }
            lexemaAnterior=tk.getLexema();
        }
        
        private void declaracion_enum2(){
            tk=this.obtenerSiguienteToken();
            if (match(tk,"coma")){
                lexemaAnterior=tk.getLexema();
                tk=this.obtenerSiguienteToken();
                if (!match(tk,"id")){
                    manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                    terminarAnalisis();    
                }
                lexemaAnterior=tk.getLexema();
                declaracion_enum2();
            }else
                lambda=true;            
        }
        
        private void declaracion_array(){
            tk=this.obtenerSiguienteToken();
            if (!match(tk,"array")){
                manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                terminarAnalisis();
            }
            lexemaAnterior=tk.getLexema();
            
            tk=this.obtenerSiguienteToken();
            if (!match(tk,"corchete_izq")){
                manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                terminarAnalisis();
            }
            lexemaAnterior=tk.getLexema();
            
            declaracion_subrango();
            declaracion_array2();
            
            tk=this.obtenerSiguienteToken();
            if (!match(tk,"corchete_der")){
                manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                terminarAnalisis();
            }
            lexemaAnterior=tk.getLexema();
            
            tk=this.obtenerSiguienteToken();
            if (!match(tk,"of")){
                manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                terminarAnalisis();
            }
            lexemaAnterior=tk.getLexema();
            
            tipo_simple();
       
        }
        
        private void declaracion_array2(){
            tk=this.obtenerSiguienteToken();
            if (match(tk,"coma")){
                lexemaAnterior=tk.getLexema();
                declaracion_subrango();
            }else
                lambda=true;
        }
        
        private void tipo_simple(){
            tk=this.obtenerSiguienteToken();
            if(tk.getNombre().equals("integer") || 
            		tk.getNombre().equals("boolean") ||
            		tk.getNombre().equals("id")){
            	lexemaAnterior=tk.getLexema();
            }
            else{
            	manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                terminarAnalisis();
            }
           /* switch (tk.getNombre()){
                case "integer":
                case "boolean":
                case "id":
                    lexemaAnterior=tk.getLexema();
                    break;
                default:{
                    manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                    terminarAnalisis();    
                }
            } */  
        }
        
        private void tipo_dato(){
            tk=this.obtenerSiguienteToken();
            if(tk.getNombre().equals("integer") || 
            		tk.getNombre().equals("boolean")){
            	lexemaAnterior=tk.getLexema();
            }
            else{
            	manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                terminarAnalisis();
            }
            /*switch (tk.getNombre()){
                case "integer":
                case "boolean":
                    lexemaAnterior=tk.getLexema();
                    break;
                default:{
                    manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                    terminarAnalisis();    
                }
            }*/   
        }
        
        private void lista_id(){
            tk=this.obtenerSiguienteToken();
            if (!match(tk,"id")){
                manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                terminarAnalisis();
            }
            lexemaAnterior=tk.getLexema();
            lista_id2();
        }
        private void lista_id2(){
            tk=this.obtenerSiguienteToken();
            if (match(tk,"coma")){
                lexemaAnterior=tk.getLexema();
                tk=this.obtenerSiguienteToken();
                if (!match(tk,"id")){
                    manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                    terminarAnalisis();
                }
                lexemaAnterior=tk.getLexema();
                lista_id2();
            }else
                lambda=true;
        }
        
        private void declaracion_constante(){
            tk=this.obtenerSiguienteToken();
            if (match(tk,"const")){
                lexemaAnterior=tk.getLexema();
                constante();
                tk=this.obtenerSiguienteToken();
                if (!match(tk,"punto_coma")){
                    manejo.agregarError(new Error(22,tk.getLinea(),";",lexemaAnterior));
                    terminarAnalisis();
                }
                lexemaAnterior=tk.getLexema();
                declaracion_constante2(); 
            }else
                lambda=true;
        }
        
        private void declaracion_constante2(){
            tk=this.obtenerSiguienteToken();
            lambda=true;
            if (match(tk,"id")){
                constante();
                tk=this.obtenerSiguienteToken();
                if (!match(tk,"punto_coma")){
                    manejo.agregarError(new Error(22,tk.getLinea(),";",lexemaAnterior));
                    terminarAnalisis();
                }
                declaracion_constante2();
            }
        }
        
        private void constante(){
            tk=this.obtenerSiguienteToken();
            if (!match(tk,"id")){
                manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                terminarAnalisis();
            }
            lexemaAnterior=tk.getLexema();
            
            tk=this.obtenerSiguienteToken();
            if (!match(tk,"igual")){
                manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                terminarAnalisis();
            }
            lexemaAnterior=tk.getLexema();
            
            valor();
        }
        
        public void valor(){
            tk=this.obtenerSiguienteToken();
            String s = tk.getNombre();
            if(s.equals("maxint") || s.equals("true") || s.equals("false") || s.equals("numero"))
            		lexemaAnterior=tk.getLexema();
            else{
            	if(s.equals("op_precedencia_2")){
            		if (tk.getAtributo().equalsIgnoreCase("suma") || tk.getAtributo().equalsIgnoreCase("resta")){
            			lexemaAnterior=tk.getLexema();
            			tk=this.obtenerSiguienteToken();
            			if (!match(tk,"numero")){
            				manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
            				terminarAnalisis();
            			}
            			lexemaAnterior=tk.getLexema();
            		}else{
            			manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
            			terminarAnalisis();
            		}
            		lexemaAnterior=tk.getLexema();
            	}            		
            	else{
            		manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
            		terminarAnalisis();
            	}
            }
            /*switch (s){
                case "maxint":
                    lexemaAnterior=tk.getLexema();
                    break;
                case "true":
                    lexemaAnterior=tk.getLexema();
                    break;
                case "false":
                    lexemaAnterior=tk.getLexema();
                    break;
                case "numero":
                    lexemaAnterior=tk.getLexema();
                    break;
                case "op_precedencia_2":
                    if (tk.getAtributo().equalsIgnoreCase("suma") || tk.getAtributo().equalsIgnoreCase("resta")){
                        lexemaAnterior=tk.getLexema();
                        tk=this.obtenerSiguienteToken();
                        if (!match(tk,"numero")){
                            manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                            terminarAnalisis();
                        }
                        lexemaAnterior=tk.getLexema();
                    }else{
                        manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                        terminarAnalisis();
                    }
                    lexemaAnterior=tk.getLexema();
                    break;
                default:{
                    manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                    terminarAnalisis();
                }
            }*/
        }
        
        private void sentenciaCompuesta(){
            
            tk=this.obtenerSiguienteToken();
            
            
            if ( !match(tk, "begin")){
                //System.out.println("deberia ser RECORD pero es: "+tk.getNombre());
                manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                terminarAnalisis();//MATAR HILO
            }
            lexemaAnterior=tk.getLexema();
            
            sentencia();
            sentencia_compuesta2();
            
            tk = this.obtenerSiguienteToken();
//            
//            if (match(tk, "punto_coma")){
//                lexemaAnterior=tk.getLexema();
//                tk = this.obtenerSiguienteToken();
//            }
            
            if ( !match(tk, "end")){
                manejo.agregarError(new Error(22,tk.getLinea(),";",lexemaAnterior));
                terminarAnalisis();//MATAR HILO
            }
            lexemaAnterior=tk.getLexema();
        }
        
        private void sentencia_compuesta2(){
            tk=this.obtenerSiguienteToken();
            if (match(tk,"punto_coma")){
                lexemaAnterior=tk.getLexema();
                
                tk=this.obtenerSiguienteToken();
                if (match(tk,"end")){
                    lambda=true;
                }else{
                    lambda=true;
                    sentencia();
                    sentencia_compuesta2();
                }
            }else{
                lambda=true;
            }
//            tk=this.obtenerSiguienteToken();
//            if (match(tk,"punto_coma")){
//                lexemaAnterior=tk.getLexema();
//                
//                tk=this.obtenerSiguienteToken();
//                if (match(tk,"end")){
//                    manejo.agregarError(new Error(29,tk.getLinea(),";",lexemaAnterior));
//                    terminarAnalisis();//MATAR HILO
//                }else
//                    lambda=true;
//                
//                sentencia();
//                sentencia_compuesta2();
//            }else{
//                lambda=true;
//            }
        }
        
        private void sentencia(){
            String s;
            tk = this.obtenerSiguienteToken();
            s = tk.getNombre();
            if(s.equals("id")){
            	lexemaAnterior=tk.getLexema();
                tk = this.obtenerSiguienteToken();
                s = tk.getLexema();
                if(s.equals(":=")){
                	lexemaAnterior=tk.getLexema();
                    expresion();
                }
                else{
                	if(s.equals("[")){
                		lexemaAnterior=tk.getLexema();
                		arreglo();
                		tk = this.obtenerSiguienteToken();
                		if (!match(tk,"asignacion")){
                			manejo.agregarError(new Error(31,tk.getLinea(),":=",lexemaAnterior));
                			terminarAnalisis();
                		}
                		lexemaAnterior=tk.getLexema();
                		expresion();
                	}
                	else{
                		if(s.equals(".")){
                			lexemaAnterior=tk.getLexema();
                			id();
                			tk = this.obtenerSiguienteToken();
                			if (!match(tk,"asignacion")){
                				manejo.agregarError(new Error(31,tk.getLinea(),":=",lexemaAnterior));
                				terminarAnalisis();
                			}
                			lexemaAnterior=tk.getLexema();
                			expresion();
                		}
                		else{
                			if(s.equals("(")){
                				lexemaAnterior=tk.getLexema();
                				listaParametros();
                				tk = this.obtenerSiguienteToken();
                				if (!match(tk,"parentesis_der")){
                					manejo.agregarError(new Error(33,tk.getLinea(),")",lexemaAnterior));
                					terminarAnalisis();
                				}
                				lexemaAnterior=tk.getLexema();
                			}
                			else{
                                                //manejo.agregarError(new Error(24,tk.getLinea(),null,lexemaAnterior));
                				manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                				terminarAnalisis();//MATAR HILO 
                			}
                		}
                	}
                }
            }
            else{
                	if(s.equals("begin")){
                                //lexemaAnterior=tk.getLexema();
                		lambda=true; // para aprovechar el metodo sentenciaCompuesta
                		sentenciaCompuesta();
                	}
                	else{
                		if(s.equals("if")){
                			lambda=true;
                			sentencia_alternativa();
                		}
                		else{
                			if(s.equals("while")){
                				lambda=true;
                				sentencia_while();
                			}
                			else{
                				if(s.equals("case")){
                					lambda=true;
                					sentencia_case();
                				}
                				else{
                					if(s.equals("read")){
                						lambda=true;
                						read();
                					}
                					else{
                						if(s.equals("write")){
                							lambda=true;
                							write();
                						}
                						else{
                                                                        manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                							//manejo.agregarError(new Error(24,tk.getLinea(),null,lexemaAnterior));
                							terminarAnalisis();//MATAR HILO 
                						}
                					}
                				}
                			}
                		}
                	}
              }
            /*switch (s){
                case "id":{
                    lexemaAnterior=tk.getLexema();
                    tk = this.obtenerSiguienteToken();
                    s = tk.getLexema();
                    switch (s){
                        case ":=":
                            lexemaAnterior=tk.getLexema();
                            expresion();
                            break;
                        case "[":
                            lexemaAnterior=tk.getLexema();
                            arreglo();
                            tk = this.obtenerSiguienteToken();
                            if (!match(tk,"asignacion")){
                                manejo.agregarError(new Error(31,tk.getLinea(),":=",lexemaAnterior));
                                terminarAnalisis();
                            }
                            lexemaAnterior=tk.getLexema();
                            expresion();
                            break;
                        case ".":
                            lexemaAnterior=tk.getLexema();
                            id();
                            tk = this.obtenerSiguienteToken();
                            if (!match(tk,"asignacion")){
                                manejo.agregarError(new Error(31,tk.getLinea(),":=",lexemaAnterior));
                                terminarAnalisis();
                            }
                            lexemaAnterior=tk.getLexema();
                            expresion();
                            break;
                        case "("://es una llamada a funcion o procedimiento con parametros
                            lexemaAnterior=tk.getLexema();
                            listaParametros();
                            tk = this.obtenerSiguienteToken();
                            if (!match(tk,"parentesis_der")){
                                manejo.agregarError(new Error(33,tk.getLinea(),")",lexemaAnterior));
                                terminarAnalisis();
                            }
                            lexemaAnterior=tk.getLexema();
                            break;
                        default: {
                            manejo.agregarError(new Error(24,tk.getLinea(),null,lexemaAnterior));
                            terminarAnalisis();//MATAR HILO    
                        }
                    }  
                };break;
                case "begin":
                    lambda=true; // para aprovechar el metodo sentenciaCompuesta
                    sentenciaCompuesta();
                    break;
                case "if":
                    lambda=true;
                    sentencia_alternativa();
                    break;
                case "while":
                    lambda=true;
                    sentencia_while();
                    break;
                case "case":
                    lambda=true;
                    sentencia_case();
                    break;
                case "read":
                    lambda=true;
                    read();
                    break;
                case "write":
                    lambda=true;
                    write();
                    break;
                default: {
                    manejo.agregarError(new Error(24,tk.getLinea(),null,lexemaAnterior));
                    terminarAnalisis();//MATAR HILO    
                }
            }*/
            
        }
        
        private void write(){
            tk=this.obtenerSiguienteToken();
            if (!match(tk,"write")){
                manejo.agregarError(new Error(38,tk.getLinea(),"write",lexemaAnterior));
                terminarAnalisis();//MATAR HILO
            }
            lexemaAnterior=tk.getLexema();
            
            tk=this.obtenerSiguienteToken();
            if (!match(tk,"parentesis_izq")){
                manejo.agregarError(new Error(38,tk.getLinea(),"(",lexemaAnterior));
                terminarAnalisis();//MATAR HILO
            }
            lexemaAnterior=tk.getLexema();
            
            expresion();
            
            tk=this.obtenerSiguienteToken();
            if (!match(tk,"parentesis_der")){
                manejo.agregarError(new Error(38,tk.getLinea(),")",lexemaAnterior));
                terminarAnalisis();//MATAR HILO
            }
            lexemaAnterior=tk.getLexema();
        }
        
        private void read(){
            tk=this.obtenerSiguienteToken();
            if (!match(tk,"read")){
                manejo.agregarError(new Error(37,tk.getLinea(),"read",lexemaAnterior));
                terminarAnalisis();//MATAR HILO
            }
            lexemaAnterior=tk.getLexema();
            
            tk=this.obtenerSiguienteToken();
            if (!match(tk,"parentesis_izq")){
                manejo.agregarError(new Error(37,tk.getLinea(),"(",lexemaAnterior));
                terminarAnalisis();//MATAR HILO
            }
            lexemaAnterior=tk.getLexema();
            
            sentencia2();
            
            tk=this.obtenerSiguienteToken();
            if (!match(tk,"parentesis_der")){
                manejo.agregarError(new Error(37,tk.getLinea(),")",lexemaAnterior));
                terminarAnalisis();//MATAR HILO
            }
            lexemaAnterior=tk.getLexema();
            
        }
        
        private void sentencia2(){
            tk=this.obtenerSiguienteToken();
            if (!match(tk,"id")){
                manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                terminarAnalisis();//MATAR HILO
            }
            lexemaAnterior=tk.getLexema();
            
            tk=this.obtenerSiguienteToken();
            if(tk.getNombre().equals("corchete_izq")){
            	lexemaAnterior=tk.getLexema();
                arreglo();
            }
            else{
            	if(tk.getNombre().equals("punto")){
            		lexemaAnterior=tk.getLexema();
            		id();
            	}
            	else{
            		lambda=true;
            	}
            }
           /* switch (tk.getNombre()){
                case "corchete_izq":
                    lexemaAnterior=tk.getLexema();
                    arreglo();
                    break;
                case "punto":
                    lexemaAnterior=tk.getLexema();
                    id();
                    break;
                default:
                    lambda=true;
            }*/
        }
        
        private void sentencia_case(){
            tk=this.obtenerSiguienteToken();
            if (!match(tk,"case")){
                manejo.agregarError(new Error(36,tk.getLinea(),"case",lexemaAnterior));
                terminarAnalisis();//MATAR HILO
            }
            lexemaAnterior=tk.getLexema();
            
            tk=this.obtenerSiguienteToken();
            if (!match(tk,"id")){
                manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                terminarAnalisis();//MATAR HILO
            }
            lexemaAnterior=tk.getLexema();
            
            tk=this.obtenerSiguienteToken();
            if (!match(tk,"of")){
                manejo.agregarError(new Error(36,tk.getLinea(),"OF",lexemaAnterior));
                terminarAnalisis();//MATAR HILO
            }
            lexemaAnterior=tk.getLexema();
            
            case2();
            
            tk=this.obtenerSiguienteToken();
            if (!match(tk,"dos_puntos")){
                manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                terminarAnalisis();//MATAR HILO
            }
            lexemaAnterior=tk.getLexema();
            
            sentencia();
            sentencia_case2();
            sentencia_case3();
            
            tk=this.obtenerSiguienteToken();
            if (!match(tk,"end")){
                manejo.agregarError(new Error(36,tk.getLinea(),"END",lexemaAnterior));
                terminarAnalisis();//MATAR HILO
            }
            lexemaAnterior=tk.getLexema();
        }
        
        private void case2(){
            tk=this.obtenerSiguienteToken();
            if(tk.getNombre().equals("maxint") || tk.getNombre().equals("true") || tk.getNombre().equals("false")){
            	lexemaAnterior=tk.getLexema();
            }
            else{
            	if(tk.getNombre().equals("id") || tk.getNombre().equals("numero")){
            		lexemaAnterior=tk.getLexema();
            		tk=this.obtenerSiguienteToken();
            		if (match(tk,"punto_punto")){
            			lexemaAnterior=tk.getLexema();
            			indice();
            		}else
            			lambda=true;
            	}
            	else{
            		if(tk.getNombre().equals("op_precedencia_2")){
            			if (tk.getLexema().equalsIgnoreCase("+") || tk.getLexema().equalsIgnoreCase("-")){
            				lexemaAnterior=tk.getLexema();
            				tk=this.obtenerSiguienteToken();
            				if (!match(tk,"numero")){
            					manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
            					terminarAnalisis();//MATAR HILO    
            				}
            				lexemaAnterior=tk.getLexema();
                    
            				tk=this.obtenerSiguienteToken();
            				if (match(tk,"punto_punto")){
            					lexemaAnterior=tk.getLexema();
            					indice();
            				}else
            					lambda=true;    
            			}
            		}
            		else{
            			manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
            			terminarAnalisis();//MATAR HILO 
            		}
            		
            	}
            }
            /*switch (tk.getNombre()){
                case "maxint":
                case "true":
                case "false":
                    lexemaAnterior=tk.getLexema();
                    break;
                case "id":
                case "numero":
                    lexemaAnterior=tk.getLexema();
                    tk=this.obtenerSiguienteToken();
                    if (match(tk,"punto_punto")){
                        lexemaAnterior=tk.getLexema();
                        indice();
                    }else
                        lambda=true;
                    break;
                case "op_precedencia_2":{
                    if (tk.getLexema().equalsIgnoreCase("+") || tk.getLexema().equalsIgnoreCase("-")){
                        lexemaAnterior=tk.getLexema();
                        tk=this.obtenerSiguienteToken();
                        if (!match(tk,"numero")){
                            manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                            terminarAnalisis();//MATAR HILO    
                        }
                        lexemaAnterior=tk.getLexema();
                        
                        tk=this.obtenerSiguienteToken();
                        if (match(tk,"punto_punto")){
                            lexemaAnterior=tk.getLexema();
                            indice();
                        }else
                            lambda=true;    
                    }
                };break;
                default:{
                    manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                    terminarAnalisis();//MATAR HILO 
                }
            }*/
            
        }
        
        private void sentencia_case2(){
            tk=this.obtenerSiguienteToken();
            if (match(tk,"punto_coma")){
                lexemaAnterior=tk.getLexema();

                case2();

                tk=this.obtenerSiguienteToken();
                if (!match(tk,"dos_puntos")){
                    manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                    terminarAnalisis();//MATAR HILO
                }
                lexemaAnterior=tk.getLexema();
                
                sentencia();
                sentencia_case2();
            }else
                lambda=true;
            
        }
        
        private void sentencia_case3(){
            tk=this.obtenerSiguienteToken();
            if (match(tk,"else")){
                lexemaAnterior=tk.getLexema();
                sentencia();
            }else
                lambda=true;
        }
        
        private void sentencia_while(){
            tk=this.obtenerSiguienteToken();
            if (!match(tk,"while")){
                manejo.agregarError(new Error(35,tk.getLinea(),"WHILE",lexemaAnterior));
                terminarAnalisis();//MATAR HILO
            }
            lexemaAnterior=tk.getLexema();
            
            expresion();
            
            tk=this.obtenerSiguienteToken();
            if (!match(tk,"do")){
                manejo.agregarError(new Error(35,tk.getLinea(),"DO",lexemaAnterior));
                terminarAnalisis();//MATAR HILO
            }
            lexemaAnterior=tk.getLexema();
            
            sentencia();
        }
        
        private void sentencia_alternativa(){
            tk=this.obtenerSiguienteToken();
            if (!match(tk,"if")){
                manejo.agregarError(new Error(34,tk.getLinea(),"IF",lexemaAnterior));
                terminarAnalisis();//MATAR HILO
            }
            lexemaAnterior=tk.getLexema();
            
            expresion();
            
            tk=this.obtenerSiguienteToken();
            if (!match(tk,"then")){
                manejo.agregarError(new Error(34,tk.getLinea(),"THEN",lexemaAnterior));
                terminarAnalisis();//MATAR HILO
            }
            lexemaAnterior=tk.getLexema();
            
            sentencia();
            sentencia_alternativa2();     
        }
        
        private void sentencia_alternativa2(){
            tk=this.obtenerSiguienteToken();
            if (match(tk,"else")){
                lexemaAnterior=tk.getLexema();
                sentencia();
            }else
                lambda=true;
        }
        
        private void expresion(){
            expresion_simple();
            expresion2();
        }
        
        private void expresion2(){
            tk=this.obtenerSiguienteToken();
            String s = tk.getNombre();
            if(s.equals("op_precedencia_3")){
            	lexemaAnterior=tk.getLexema();
                expresion_simple();
            }
            else{
            	lambda=true;
            }
            /*switch (s){
                case "op_precedencia_3":
                    lexemaAnterior=tk.getLexema();
                    expresion_simple();
                    break;
                default:// puede derivar en lambda
                    lambda=true;
            }*/
        }
        
        private void expresion_simple(){
            signo();
            termino();
            expresion_simple_2();

        }
        
        private void signo(){
            tk=this.obtenerSiguienteToken();
            String s = tk.getLexema(); 
            if(s.equals("+") || s.equals("-")){
            	lexemaAnterior=tk.getLexema();
            }
            else{
            	lambda=true;
            }
            /*switch (s){
                case "+":
                    lexemaAnterior=tk.getLexema();
                    break;
                case "-":
                    lexemaAnterior=tk.getLexema();
                    break;
                default:// puede derivar en lambda
                    lambda=true;
            }*/
        }
        
        private void expresion_simple_2(){
            tk=this.obtenerSiguienteToken();
            String s = tk.getNombre(); 
            if(s.equals("op_precedencia_2")){
            	lexemaAnterior=tk.getLexema();
                termino();
                expresion_simple_2();
            }
            else{
            	lambda=true;
            }
           /* switch (s){
                case "op_precedencia_2":
                    lexemaAnterior=tk.getLexema();
                    termino();
                    expresion_simple_2();
                    break;
                default:// puede derivar en lambda
                    lambda=true;
            }*/
        }
        
        private void termino(){
            factor();
            termino2();
        }
        
        private void termino2(){
            tk = this.obtenerSiguienteToken();
            String s = tk.getNombre();
            if(s.equals("op_precedencia_1")){
            	lexemaAnterior=tk.getLexema();
                factor();
                termino2();
            }
            else{
            	lambda=true;
            }
           /* switch (s){
                case "op_precedencia_1": // tiene operadores...
                    lexemaAnterior=tk.getLexema();
                    factor();
                    termino2();
                    break;
                default: // derivacion en lambda
                    lambda=true;
                    // puede que sea vacio
                    // no consume el token y lo deja pasar...
            }*/
        }
        
        private void factor(){
            tk=this.obtenerSiguienteToken();
            String s = tk.getNombre();
            if(s.equals("numero") || s.equals("true") || s.equals("false")){
            	lexemaAnterior=tk.getLexema();
            }
            else{
            	if(s.equals("not")){
            		lexemaAnterior=tk.getLexema(); factor();
            	}
            	else{
            		if(s.equals("parentesis_izq")){
            			lexemaAnterior=tk.getLexema(); // (expresion)
            			expresion();
            			tk=this.obtenerSiguienteToken();
            			if ( !match(tk, "parentesis_der")){
            				manejo.agregarError(new Error(25,tk.getLinea(),")",lexemaAnterior));
            				this.terminarAnalisis();//MATAR HILO
            			}
            			lexemaAnterior=tk.getLexema(); 
            		}
            		else{
            			if(s.equals("pred")){
            				lexemaAnterior=tk.getLexema(); //pred(expresion)
            				tk=this.obtenerSiguienteToken();
            				if ( !match(tk, "parentesis_izq")){
            					manejo.agregarError(new Error(26,tk.getLinea(),"(",lexemaAnterior));
            					this.terminarAnalisis();//MATAR HILO
            				}
            				lexemaAnterior=tk.getLexema(); 
            				expresion();
            				tk=this.obtenerSiguienteToken();
            				if ( !match(tk, "parentesis_der")){
            					manejo.agregarError(new Error(26,tk.getLinea(),")",lexemaAnterior));
            					this.terminarAnalisis();//MATAR HILO
            				}
            				lexemaAnterior=tk.getLexema(); 
            			}
            			else{
            				if(s.equals("succ")){
            					lexemaAnterior=tk.getLexema(); //succ(expresion)
            					tk=this.obtenerSiguienteToken();
            					if ( !match(tk, "parentesis_izq")){
            						manejo.agregarError(new Error(27,tk.getLinea(),"(",lexemaAnterior));
            						this.terminarAnalisis();//MATAR HILO
            					}
            					lexemaAnterior=tk.getLexema(); 
            					expresion();
            					tk=this.obtenerSiguienteToken();
            					if ( !match(tk, "parentesis_der")){
            						manejo.agregarError(new Error(27,tk.getLinea(),")",lexemaAnterior));
            						this.terminarAnalisis();//MATAR HILO
            					}
            					lexemaAnterior=tk.getLexema();
            				}
            				else{
            					if(s.equals("id")){
            						lexemaAnterior=tk.getLexema();
            						tk=this.obtenerSiguienteToken();
            						String s2 = tk.getLexema();
            						if(s2.equals(".")){
            							lexemaAnterior=tk.getLexema();
            							id();
            						}
            						else{
            							if(s2.equals("[")){
            								lexemaAnterior=tk.getLexema();
            								arreglo();
            							}
            							else{
            								if(s2.equals("(")){
            									lexemaAnterior=tk.getLexema();
            									listaParametros();
            									tk=this.obtenerSiguienteToken();
            									if ( !match(tk, "parentesis_der")){
            										manejo.agregarError(new Error(33,tk.getLinea(),")",lexemaAnterior));
            										this.terminarAnalisis();//MATAR HILO
            									};
            									lexemaAnterior=tk.getLexema();
            								}
                                                                        else{
                                                                            //System.out.println("lexanterior "+lexemaAnterior);
                                                                            
            									lambda=true;
                                                                        }
            							}
            						}
            					}
            					else{
            						manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
            						this.terminarAnalisis();//MATAR HILO
            					}
            				}
            			}
            		}
            	}
            }
            /*switch (s){
                case "numero": 
                    lexemaAnterior=tk.getLexema();break;
                case "true": 
                    lexemaAnterior=tk.getLexema();break;
                case "false": 
                    lexemaAnterior=tk.getLexema();break;    
                case "not": 
                    lexemaAnterior=tk.getLexema(); factor(); break;
                case "parentesis_izq": 
                        lexemaAnterior=tk.getLexema(); // (expresion)
                        expresion();
                        tk=this.obtenerSiguienteToken();
                        if ( !match(tk, "parentesis_der")){
                            manejo.agregarError(new Error(25,tk.getLinea(),")",lexemaAnterior));
                            this.terminarAnalisis();//MATAR HILO
                        };
                        lexemaAnterior=tk.getLexema(); 
                        break;
                case "pred": lexemaAnterior=tk.getLexema(); //pred(expresion)
                        tk=this.obtenerSiguienteToken();
                        if ( !match(tk, "parentesis_izq")){
                            manejo.agregarError(new Error(26,tk.getLinea(),"(",lexemaAnterior));
                            this.terminarAnalisis();//MATAR HILO
                        };
                        lexemaAnterior=tk.getLexema(); 
                        expresion();
                        tk=this.obtenerSiguienteToken();
                        if ( !match(tk, "parentesis_der")){
                            manejo.agregarError(new Error(26,tk.getLinea(),")",lexemaAnterior));
                            this.terminarAnalisis();//MATAR HILO
                        };
                        lexemaAnterior=tk.getLexema(); 
                        break;
               case "succ": 
                        lexemaAnterior=tk.getLexema(); //succ(expresion)
                        tk=this.obtenerSiguienteToken();
                        if ( !match(tk, "parentesis_izq")){
                            manejo.agregarError(new Error(27,tk.getLinea(),"(",lexemaAnterior));
                            this.terminarAnalisis();//MATAR HILO
                        };
                        lexemaAnterior=tk.getLexema(); 
                        expresion();
                        tk=this.obtenerSiguienteToken();
                        if ( !match(tk, "parentesis_der")){
                            manejo.agregarError(new Error(27,tk.getLinea(),")",lexemaAnterior));
                            this.terminarAnalisis();//MATAR HILO
                        };
                        lexemaAnterior=tk.getLexema(); 
                        break;    
                case "id":{//puede ser un registro o un arreglo
                        lexemaAnterior=tk.getLexema();
                        tk=this.obtenerSiguienteToken();
                        String s2 = tk.getLexema();
                        switch (s2){
                            case ".": 
                                lexemaAnterior=tk.getLexema();
                                id();
                                break;// registro... falta "id"
                            case "[": //arreglo... falta "expresion]" or "expresion,expresion]"
                                lexemaAnterior=tk.getLexema();
                                arreglo();
                                break;
                            case "("://llamada con argumentos
                                lexemaAnterior=tk.getLexema();
                                listaParametros();
                                tk=this.obtenerSiguienteToken();
                                if ( !match(tk, "parentesis_der")){
                                    manejo.agregarError(new Error(33,tk.getLinea(),")",lexemaAnterior));
                                    this.terminarAnalisis();//MATAR HILO
                                };
                                lexemaAnterior=tk.getLexema();
                                break;
                            
                            default:
                                lambda=true; 
                            
                        }
                }break;
                default:{
                        manejo.agregarError(new Error(21,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                        this.terminarAnalisis();//MATAR HILO 
                }    
            }*/
        }
        
        private void listaParametros(){
            expresion();
            tk=this.obtenerSiguienteToken();
            if (match(tk,"coma")){
                lexemaAnterior=tk.getLexema();
                listaParametros();
            }else
                lambda=true;
            
        }
        
        public void llamada(){
            // lista de identificadores
            // ")"
            // ";"
        }
        
        public void id(){
            tk=this.obtenerSiguienteToken();
            if ( !match(tk, "id")){
                manejo.agregarError(new Error(30,tk.getLinea(),"id",lexemaAnterior));
                this.terminarAnalisis();//MATAR HILO
            };
            lexemaAnterior=tk.getLexema();
        }
        
        public void arreglo(){
            expresion();
            tk=this.obtenerSiguienteToken();
            String s = tk.getNombre();
            if(s.equals("coma")){
            	lexemaAnterior=tk.getLexema();
                expresion();
                tk=this.obtenerSiguienteToken();
                if ( !match(tk, "corchete_der")){
                        manejo.agregarError(new Error(28,tk.getLinea(),"]",lexemaAnterior));
                        this.terminarAnalisis();//MATAR HILO
                };
                lexemaAnterior=tk.getLexema();
                tk=this.obtenerSiguienteToken();
                if (match(tk, "punto")){// posible acceso a registro
                    lexemaAnterior=tk.getLexema();
                    id();
                }else// termina encabezado del arreglo
                    lambda=true;
            }
            else{
            	if(s.equals("corchete_der")){
            		lexemaAnterior=tk.getLexema();// fin del arreglo
            		tk=this.obtenerSiguienteToken();
            		if (match(tk, "punto")){// posible acceso a registro
            			lexemaAnterior=tk.getLexema();
            			id();
            		}else// termina encabezado del arreglo
            			lambda=true;
            	}
            	else{
            		manejo.agregarError(new Error(28,tk.getLinea(),"]",lexemaAnterior));
            		this.terminarAnalisis();//MATAR HILO
            	}
            }
           /* switch (s){
                
                case "coma":
                    lexemaAnterior=tk.getLexema();
                    expresion();
                    tk=this.obtenerSiguienteToken();
                    if ( !match(tk, "corchete_der")){
                            manejo.agregarError(new Error(28,tk.getLinea(),"]",lexemaAnterior));
                            this.terminarAnalisis();//MATAR HILO
                    };
                    lexemaAnterior=tk.getLexema();
                    tk=this.obtenerSiguienteToken();
                    if (match(tk, "punto")){// posible acceso a registro
                        lexemaAnterior=tk.getLexema();
                        id();
                    }else// termina encabezado del arreglo
                        lambda=true;
                    break;
                case "corchete_der": 
                    lexemaAnterior=tk.getLexema();// fin del arreglo
                    tk=this.obtenerSiguienteToken();
                    if (match(tk, "punto")){// posible acceso a registro
                        lexemaAnterior=tk.getLexema();
                        id();
                    }else// termina encabezado del arreglo
                        lambda=true;
                    break;
                default:{
                    manejo.agregarError(new Error(28,tk.getLinea(),"]",lexemaAnterior));
                    this.terminarAnalisis();//MATAR HILO
                } 
            }*/
        }
        private void indice(){
            signo();
            indice2();
        }
        private void indice2(){
            tk=this.obtenerSiguienteToken();
            if(tk.getNombre().equals("numero") || tk.getNombre().equals("id")){
            	lexemaAnterior=tk.getLexema();
            }
            else{
            	 manejo.agregarError(new Error(32,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                 this.terminarAnalisis();// kill thread
            }
           /* switch (tk.getNombre()){
                case "numero": 
                    lexemaAnterior=tk.getLexema();
                    break;
                case "id":
                    lexemaAnterior=tk.getLexema();
                    break;
                default:
                    manejo.agregarError(new Error(32,tk.getLinea(),tk.getLexema(),lexemaAnterior));
                    this.terminarAnalisis();// kill thread
            }*/
        }
}