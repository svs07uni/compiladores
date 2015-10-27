
package semantico;
 

import java.io.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

 
public class Lexico extends Thread{
    private static final String regex_simbolos = "=|:|;|.|,|(|)|..|\\[|\\]";
    
    private static final String regex_asignacion = ":=";
    
    private static final String regex_operadores = "<|>|<=|>=|<>|+|\\-|\\*|/";
    
    private static final String regex_numeros = "\\d+";
    
    private static final String regex_palabras_reservadas = "program|const|maxint|true|record|false|integer|boolean|"
            + "type|array|of|case|var|procedure|begin|end|function|read|write|if|then|else|while|do|succ|pred|not|and|or";
    
    private static final String regex_identificadores = "[a-zA-Z_]+\\w*";
 
    //Se inicializa con la linea de inicio de comentario, cuando se cierra se vuelve a poner -1
    private static int lineaComentario = -1;
    private static int[] comentarios = {0,0};//[lineaInicio,lineaFin]
    
    private static final int longIdentificador = 20;//Limitacion
    
    private static final int longNumero = 5;//Rango integer -32768-32768
    
    private Queue errores = new LinkedList();//Cola de errores de tipo (tipo, numeroLinea, lexemaError)
    
    private Queue tokens = new LinkedList();
    
    private static int numeroLinea = 1;
    private static boolean comentario = false;//Para reconocer cuando estamos en comentarios y no se debe analizar
    private static boolean advertencia = false;//Para no advertir dos veces
    private static BufferedReader archivoPas = null;
    private ManejoDeErrores manejo;
    private static String linea;
    
    private SynchronousQueue buffer;
    private boolean error;
    private boolean fin;
   // private Sintactico sintactico;
    
    public Lexico (String archivo, SynchronousQueue bf, ManejoDeErrores e){
        this.manejo = e;
    	this.buffer = bf;
        error=false;
        fin=false;
        //sintactico=null;
        try {
    		archivoPas = new BufferedReader(new FileReader(archivo));
    		linea = archivoPas.readLine();
    	} catch (FileNotFoundException ex) {
            manejo.agregarError(new Error(0, 0, null,null));//System.out.println("Archivo no encontrado");
           // sintactico.finLexico();
    	} catch (IOException ex) {
            manejo.agregarError(new Error(1, 0, null,null));//System.out.println("Error en lectura");
            //sintactico.terminarAnalisis();
        }
    }

    public boolean error(){
        return error;
    }
    
//    public void setSintactico(Sintactico s){
//        sintactico=s;
//    }
    
    public boolean fin(){
        return fin;
    }
    
    public void cerrarArchivo(){
    	try {
			archivoPas.close();
		} catch (IOException e) {
			manejo.agregarError(new Error(2, 0, null,null));//System.out.println("No se pudo cerrar el archivo correctamente");
                        //sintactico.terminarAnalisis();
		}
    }
    
    public void run(){
    	String lineaUtil, luegoFin="";
    	try{
           // boolean fin = false;
            while(linea!=null) {
                    lineaUtil = linea.toLowerCase();// Pascal no distingue entre minusculas y mayusculas
                    /*if(fin){//Estamos fuera del programa, luego del end.
                    	luegoFin += analizarComentarios(lineaUtil);
                    	linea = archivoPas.readLine();
                    }
                    else{//Estamos dentro del programa
                    	
                    if(!comentario && lineaUtil.matches(".*\\s*end\\s*\\..*")){//Tiene el fin de programa
                            lineaUtil = analizarFin(lineaUtil);
                            fin = true;
                    	}*/
                    	if(lineaUtil.matches(".*[{|}].*")){//Tiene inicio y/o fin de comentarios
                        	lineaUtil = analizarComentarios(lineaUtil.trim());
                        	analizar(lineaUtil, numeroLinea);//Analizo lo que este fuera del comentario en esta linea
                    	}
                    	else{
                    		if(!comentario)//no es comentario, se debe analizar
                    			analizar(lineaUtil, numeroLinea);
                    	}
                		linea = archivoPas.readLine();//Siguiente linea del archivo
                        numeroLinea++;
                    //}                    
            }
        } catch (IOException e) {
            manejo.agregarError(new Error(1, 0, null,null));//System.out.println("Error en lectura");
            //sintactico.terminarAnalisis();
        }catch(ThreadDeath e){
        	
        }
            
            if(comentarios[0] != 0 && comentarios[1] == 0){//Inicio un comentario sin terminar
            	//Si o si necesita el espacio entre la coma y el ), sino tira error al imprimirlo
                //errores.add("(2,"+comentarios[0]+", )");
                manejo.agregarError(new Error(10,numeroLinea,null,null));
                //sintactico.terminarAnalisis();
            }
            
            //Mostrar advertencias
            //luegoFin = analizarComentarios(luegoFin);
           // if(!advertencia && luegoFin.matches("[ \t\n]*[^\t\n ]+")){//tiene algun caracter
           //     manejo.agregarError(new Error(11, numeroLinea, null,null));//
                //sintactico.terminarAnalisis();
            	//System.out.println("\n////////////////////// ADVERTENCIA //////////////////////");
            	//System.out.println("Hay caracteres despu�s del fin de programa. (linea: "+numeroLinea+")\n");
            //}  
            //Mostrar todos los tokens
            String token = "";
            if(tokens.size()>0)
            	System.out.println("\n/////////////////////// TOKENS //////////////////////");
            while(!tokens.isEmpty()){
                token = (String)tokens.poll();
                imprimirToken(token);
            }
            if(errores.size()>0)
            	System.out.println("\n////////////////////// ERRORES //////////////////////");
            //Mostrar todos los errores
            while(!errores.isEmpty()){
                token = (String)errores.poll();
                imprimirError(token);
            }
            //System.out.println("\n FIN LEXICO! \n");
            //sintactico.finLexico(); // notifico al sintactico que se acabaron los tokens
        try {
            Thread.sleep(10);
        } catch (InterruptedException ex) {
        }
                    
    }
    

    
    //Obtiene instrucciones antes del end.
    public String analizarFin(String linea){
        String util = "";
        linea = analizarComentarios(linea);
        for(int i=0; i<linea.length()+1; i++){
            util = linea.substring(0, i);
            if(util.matches("end\\s*\\.$")){//Encontre el fin de programa
                
            	if(linea.substring(i).matches("[\t ]*[^\t ]+")){//tiene algun caracter
                        manejo.agregarError(new Error(11,numeroLinea,null,null));
                        //sintactico.terminarAnalisis();
//                	System.out.println("\n////////////////////// ADVERTENCIA //////////////////////");
//                	System.out.println("Hay caracteres seguidos al fin de programa. (linea: "+numeroLinea+")");
                	advertencia = true;
                } 
                
                return util;
            }
        }
        return linea;
    }
    
    public String analizarComentarios(String lexema){
    	String nlexema = "";
    	int i,finComentario=-1;
    	for(i=0; i<lexema.length(); i++){
    		if(!comentario && lexema.charAt(i)=='{'){//Comienzo de comentario, hay que rescatar lo anterior a este simbolo
    			nlexema += lexema.substring(finComentario+1, i);
    			comentarios[0] = numeroLinea;
    			comentario = true;
    		}
    		else
    			if(lexema.charAt(i)=='}'){//Fin de comentario, hay que rescatar lo posterior a este simbolo
    				if(comentarios[0]==0){//No hay inicio de comentarios para este cierre
    					//Si o si necesita el espacio entre la coma y el ), sino tira error al imprimirlo
    					//Finalizo un comentario sin iniciar, se tomar� como s�mbolo desconocido
    				}
    				else{
    					comentarios[0] = 0;//Si esta el inicio ent agrego este fin de linea
    					comentario = false;//Fin de comentarios sin errores
    					finComentario = i;
    				}
    			}
    	}
    	if(comentarios[0]==0)
    		nlexema += lexema.substring(finComentario+1, i);
    	return nlexema;
    }
 
    // Se separa el parametro cadena en sus lexemas y se los analizan
    public void analizar(String cadena, int numeroLinea) {
        //por defecto separa los lexemas separados por espacio
        StringTokenizer linea = new StringTokenizer(cadena);
        while (linea.hasMoreTokens()) {//Analizo cada lexema obtenido
            analizarLexema(linea.nextToken());
        }
    }
    
    public void analizarLexema(String lexema){
    	while(lexema.length()>0){
    		if(lexema.matches("^["+regex_simbolos+"].*"))
    			lexema = analizarSimbolos(lexema);
    		else
    			if(lexema.matches("^["+regex_operadores+"].*"))
    				lexema = analizarOperadores(lexema);
    			else
    				if(lexema.matches("\\d.*"))//digito y algo
    					lexema = analizarNumero(lexema);
    				else
    					if(lexema.matches("^\\w.*"))//letra, digito o guion bajo
    						lexema = analizarPalabra(lexema);
    					else{
    						//errores.add("(12,"+numeroLinea+","+lexema+")");//No pertenece al alfabeto
                                                manejo.agregarError(new Error(12,numeroLinea,lexema,null));
                                                //sintactico.terminarAnalisis();
    						lexema = lexema.substring(1);//Ignoro solo el simbolo extra�o
    					}
    	}
    }
    
    public String analizarSimbolos(String lexema){//lexema tiene algun caracter
        if(lexema.length()==1){//Solo tiene un simbolos y ya fue corroborado en el metodo anterior que tiene un simbolo aceptado
            armarToken(lexema, 2);//simbolo
            lexema = "";
        }
        else{//Tiene mas de un caracter
            if(lexema.charAt(0) == '.' && lexema.charAt(1) == '.'){//Simbolo ..
                    armarToken("..",2);
                    lexema = lexema.substring(2);//Devolver el resto
                }
            else
                if(lexema.charAt(0) == ':' && lexema.charAt(1) == '='){//Asignacion :=
                    armarToken(":=",1);
                    lexema = lexema.substring(2);//Devolver el resto
                }
                else{
                    armarToken(lexema.substring(0, 1),2);//S�mbolo
                	lexema = lexema.substring(1);//Devuelve el resto, que se vuelva a analizar
                }
        }
        return lexema;
    }  
    
    public String analizarOperadores(String lexema){

        if(lexema.matches("^(<=|>=|<>).*")){//Es un operador de precedencia 3
                armarToken(lexema.substring(0, 2),6);
                lexema = lexema.substring(2);
        }
        else{
            if(lexema.matches("^[=|>|<].*")){//Es un operador de precedencia 3
                armarToken(lexema.substring(0, 1),6);
                lexema = lexema.substring(1);
            }
            else{
                if(lexema.matches("^[+|\\-].*")){
                    armarToken(lexema.substring(0, 1),7);
                    lexema = lexema.substring(1);
                }
                else{
                    if(lexema.matches("^[\\*|/].*")){
                        armarToken(lexema.substring(0, 1),0);
                        lexema = lexema.substring(1);
                    }
                }
                    
            }
        }
        
                    
        return lexema;
    }
    
    public String analizarNumero(String lexema){
        if(lexema.matches(regex_numeros)){//Es solo un numero
            armarToken(lexema,3);
            lexema = "";
        }
        else{
            if(lexema.matches(regex_numeros+"\\w+")){//Es un numero y una letra,digito,_ eliminando solo digitos xq ya lo analice
                //Es error, ej. 33m o 34_
                //errores.add("(4,"+numeroLinea+","+lexema+")");
                manejo.agregarError(new Error(13,numeroLinea,lexema,null));
                //sintactico.terminarAnalisis();
                lexema = "";
            }
            else{
                String token="";
                for(int i=2; i<lexema.length()+1; i++){//separo el lexema en sus partes
                    token = lexema.substring(0, i);//Obtengo los primeros i caracteres del lexema para revisar si sigue siendo un numero
                    if(!token.matches(regex_numeros)){//obtuve la division entre el numero y el extra
                        armarToken(lexema.substring(0, i-1),3);
                        return lexema.substring(i-1);//corto el recorrido 
                    }
                }
            }
        }
        return lexema;
    }
    
    public String analizarPalabra(String lexema){
        if(lexema.matches(regex_palabras_reservadas)){//Es una palabra reservada
            armarToken(lexema,5);
            lexema = "";
        }
        else{
            if(lexema.matches("^["+regex_palabras_reservadas+"]\\w+" )){//palabra reservada con letra,digito,_ seria un identificador
                armarToken(lexema,4);//identificador
                lexema = "";
            }
            else{//palabra reservada + algo
                String token="";
                for(int i=2; i<lexema.length()+1; i++){//separo el lexema en sus partes, hasta 10 maximo que es la pal.reserv. mas larga
                    token = lexema.substring(0, i);//Obtengo los primeros i caracteres del lexema para revisar si es la palabra reservada
                    if(!token.matches(regex_palabras_reservadas)){//Si no es pal reserv puede seguir siendo un identificador
                    	if(!token.matches(regex_identificadores)){//Si no es un identificador ent encontr� un simbolo extra�o
                    		token = lexema.substring(0, i-1);//Lo anterior puede ser pal reserv o id
                    		if(token.matches(regex_palabras_reservadas))//Es una palabra reservada
                    			armarToken(lexema.substring(0, i-1),5);
                    		else//Es identificador
                    			armarToken(lexema.substring(0, i-1),4);
                    		return lexema.substring(i-1);//Corto el recorrido
                    	}
                    }
                }
                //Si llegue hasta aca entonces es todo un identificador
                armarToken(lexema,4);
                lexema = "";
            }
        }
        return lexema;
    }
 
    public void armarToken(String t, int cod) {
        String nombre=null, atributo=null, lexema=null;
        
        switch (cod) {
        case 0://Operador * / 
            switch(t.charAt(0)){
            case '*': 
                nombre = "op_precedencia_1";
                atributo = "mult";
                lexema="*";
                //tokens.add("(  "+t+"  ,  op_precedencia_1  ,  mult  )");
                break;
            case '/': nombre = "op_precedencia_1"; atributo= "div";lexema="/";// tokens.add("(  "+t+"  ,  op_precedencia_1  ,  div  )");
                break;
            }
        	break;
        case 1: //Asignacion 
            nombre = "asignacion"; lexema=":=";//tokens.add("(  "+t+"  ,  asignacion  )");
            break;
        case 2: //Simbolos como : .. . , ; ( ) [ ]
            switch (t.charAt(0)){
                case ':': nombre = "dos_puntos";lexema=":";//tokens.add("(  "+t+"  ,  dos_puntos  )");
                    break;                	
                case '.': //. o ..
                	if(t.length() == 2){//Tiene dos caracteres y la �nica opci�n es ..
                		nombre="punto_punto";lexema="..";//tokens.add("(  "+t+"  ,  punto_punto  )");
                        }else
                		nombre="punto";lexema=".";//tokens.add("(  "+t+"  ,  punto  )");
                break;
                case ',': nombre="coma";lexema=",";//tokens.add("(  "+t+"  ,  coma  )");
                break;
                case ';': nombre="punto_coma";lexema=";";//tokens.add("(  "+t+"  ,  punto_coma  )");
                break;
                case '(': nombre="parentesis_izq";lexema="(";//tokens.add("(  "+t+"  ,  parentesis_izq  )");
                break;
                case ')': nombre="parentesis_der";lexema=")";//tokens.add("(  "+t+"  ,  parentesis_der  )");
                break;
                case '[': nombre="corchete_izq";lexema="[";//tokens.add("(  "+t+"  ,  corchete_izq  )");
                break;
                case ']': nombre="corchete_der";lexema="]";//tokens.add("(  "+t+"  ,  corchete_der  )");
                break;
                case '=': nombre="igual";lexema="=";//tokens.add("(  "+t+"  ,  igual  )");
                break;
            };
            break;
        case 3://numero
            if(t.length()>longNumero){//supera el limite de longitud de digitos
                manejo.agregarError(new Error(14,numeroLinea,t,null));//errores.add("(5,"+numeroLinea+","+t+")");
                //sintactico.terminarAnalisis();
            }else
                nombre="numero";atributo=t;lexema=t;//tokens.add("(  "+t+"  ,  numero  ,  "+t+"  )");
            break;
        case 4://identificador
            nombre="id";atributo=t;lexema=t;//tokens.add("(  "+t+"  ,  id  ,  "+t+"  )")
            ;break;
        case 5://palabra reservada incluyendo el or y and, ac� lo separo entre palabra_reservada y op_precedencia
        	if(t.compareToIgnoreCase("or")==0){//Son iguales
        		nombre="op_precedencia_2";atributo="or";lexema=t;//tokens.add("(  "+t+"  ,  op_precedencia_2  ,  or  )");
                }else{
        		if(t.compareToIgnoreCase("and")==0){//Son iguales
                            nombre="op_precedencia_1";atributo="and";lexema=t;//tokens.add("(  "+t+"  ,  op_precedencia_1  ,  and  )");
                        }else
                            nombre=t;lexema=t;//tokens.add("(  "+t+"  ,  "+t+"  )");
        	}
        	break;
        case 6://Operador relacional < <= > >= <>
        	switch (t.charAt(0)){
            case '<': 
            	if(t.length() == 2){//Tiene dos caracteres y la �nica opci�n es <= o <>
            		if(t.compareToIgnoreCase("<=")==0){//Es <=
            			nombre="op_precedencia_3";atributo="LE";lexema="<=";//tokens.add("(  "+t+"  ,  op_precedencia_3  ,  LE)");
                        }else{//es <>
            			nombre="op_precedencia_3";atributo="NE";lexema="<>";//tokens.add("(  "+t+"  ,  op_precedencia_3  ,  NE)");
                        }
            	}
                else{// < estricto
            		nombre="op_precedencia_3";atributo="LT";lexema="<";//tokens.add("(  "+t+"  ,  op_precedencia_3  ,  LT)");
                }break;
            case '>': 
            	if(t.length() == 2){//Tiene dos caracteres y la �nica opci�n es >=
            		nombre="op_precedencia_3";atributo="GE";lexema=">=";//tokens.add("(  "+t+"  ,  op_precedencia_3  ,  GE)");
                }else{//> estricto
            		nombre="op_precedencia_3";atributo="GT";lexema=">";//tokens.add("(  "+t+"  ,  op_precedencia_3  ,  GT)");
                }
            }
            break;
        case 7: // + - 
        	switch(t.charAt(0)){
            case '+': nombre="op_precedencia_2";atributo="suma";lexema="+";//tokens.add("(  "+t+"  ,  op_precedencia_2  ,  suma  )")
            break;
            case '-': nombre="op_precedencia_2";atributo="resta";lexema="-";//tokens.add("(  "+t+"  ,  op_precedencia_2  ,  resta  )")
            break;
            }
        	break;     
        }
        
        if (nombre != null){
            try {
                //System.out.println("BUFFER: AL --> '"+nombre+"'"); 
                buffer.put(new Token(nombre, atributo, lexema, numeroLinea));
                //Thread.sleep(4);
            } catch (InterruptedException ex) {
            }
        }
    }
    
    public void imprimirToken(String t) {
        System.out.println(t);
    }
    
    private void imprimirError(String error) {
    	StringTokenizer err = new StringTokenizer(error, "(,)");
        char campoTipo = (err.nextToken()).charAt(0);
        String campoLinea = err.nextToken();
        String campoError = err.nextToken();
    	switch(campoTipo){
    	case '1': //Cierre de comentario sin inicio
    		System.out.println("C�digo de error "+campoTipo+"\tLinea "+campoLinea+": Finaliz� un comentario sin s�mbolo de inicio '{'");
    		break;
    	case '2': //Inicio de comentario sin cierre
    		System.out.println("C�digo de error "+campoTipo+"\tLinea "+campoLinea+": Se inici� un comentario sin finalizar");
    		break;
    	case '3': //Simbolo desconocido
    		System.out.println("C�digo de error "+campoTipo+"\tLinea "+campoLinea+": S�mbolo "+campoError.charAt(0)+" no pertenece al alfabeto en la expresi�n '"+campoError+"'");
    		break;
    	case '4': //Palabra no reconocida, ej. 33m, 54kjl
    		System.out.println("C�digo de error "+campoTipo+"\tLinea "+campoLinea+": Expresi�n no permitida "+campoError);
    		break;
    	case '5': //Numero mayor a 5 digitos
    		System.out.println("C�digo de error "+campoTipo+"\tLinea "+campoLinea+": N�mero "+campoError+" demasiado largo (s�lo se aceptan 5 d�gitos)");
    		break;
    	}
	}
}