/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semantico;

/**
 *
 * @author Ale
 */
public class Error {
    private int codigo;
    private int linea;
    private String cadena;
    private String lexemaAnterior;
    
    public Error (int e, int l, String lex, String lexA){
        codigo=e;
        linea=l;
        cadena=lex;
        lexemaAnterior = lexA;
    }
    
    public int getCodigo(){
       return codigo;
    }
    
    public int getLinea(){
        return linea;
    }
    
    public String getCadena(){
        return cadena;
    }
    
    public void mostrarError(){
        String des=null;
        switch (codigo){
            // Errores Nivel 0
            case 0: des="Archivo no Encontrado"; break;          
            case 1: des="Error de Lectura"; break;
            case 2: des="Error al cerrar archivo"; break;    
            //Errores lexicos
            case 10: des="Linea "+linea+": Error Lexico! Falta simbolo de fin de comentario '}'"; break;          
            case 11: des="COMPILACION EXITOSA! \nLinea "+linea+": Advertencia! Caracteres existentes luego del fin de programa"; break;
            case 12: des="Linea "+linea+": Error Lexico! Simbolo '"+cadena+"' no pertenece al alfabeto"; break; 
            case 13: des="Linea "+linea+": Error Lexico! Identificador '"+cadena+"' no valido, debe comenzar con letra o \"_\""; break;
            case 14: des="Linea "+linea+": Error Lexico! Numero '"+cadena+"' excede los 5 digitos"; break; 
            //Errores sintacticos 
            case 20: des="Linea "+linea+": Error sintactico! Se espera palabra reservada '"+cadena+"'"; break; 
            case 21: des="Linea "+linea+": Error sintactico! Expresion invalida: \""+lexemaAnterior+" "+cadena+"\""; break;  
            case 22: des="Linea "+linea+": Error sintactico! Se espera caracter de fin de instruccion: '"+cadena+"' despues de: '"+lexemaAnterior+"'"; break;
            case 23: des="Linea "+linea+": Error sintactico! Se espera un nombre de subprograma, despues de: '"+lexemaAnterior+"'";break;
            case 24: des="Linea "+linea+": Error sintactico! Declaracion de tipo de dato no valida: '"+cadena+"'"; break;            
            case 25: des="Linea "+linea+": Error sintactico! Se espera el caracter '"+cadena+"' para cerrar expresion, despues de: '"+lexemaAnterior+"'"; break;    
            case 26: des="Linea "+linea+": Error sintactico! Se espera el caracter '"+cadena+"' en la sentencia \"pred()\""; break;
            case 27: des="Linea "+linea+": Error sintactico! Se espera el caracter '"+cadena+"' en la sentencia \"succ()\""; break;
            case 28: des="Linea "+linea+": Error sintactico! Se espera el caracter '"+cadena+"' para cerrar acceso al arreglo, depues de: '"+lexemaAnterior+"'"; break;
            case 29: des="Linea "+linea+": Error sintactico! La instruccion previa a la palabra clave 'END' no deberia finalizar con '"+cadena+"'"; break;            
            case 30: des="Linea "+linea+": Error sintactico! Se espera un identificador despues de: '"+lexemaAnterior+"'"; break; 
            case 31: des="Linea "+linea+": Error sintactico! Se espera el caracter '"+cadena+"' de asignacion despues de: '"+lexemaAnterior+"'"; break;            
            case 32: des="Linea "+linea+": Error sintactico! Indice de arreglo invalido: \""+lexemaAnterior+" "+cadena+"\""; break;
            case 33: des="Linea "+linea+": Error sintactico! Se espera el caracter '"+cadena+"' de cierre de llamada a subprograma, despues de '"+lexemaAnterior+"'"; break;
            case 34: des="Linea "+linea+": Error sintactico! Se espera palabra clave: '"+cadena+"' en sentencia alternativa, despues de: '"+lexemaAnterior+"'"; break;
            case 35: des="Linea "+linea+": Error sintactico! Se espera palabra clave: '"+cadena+"' en sentencia repetitiva, despues de: '"+lexemaAnterior+"'"; break;
            case 36: des="Linea "+linea+": Error sintactico! Se espera palabra clave: '"+cadena+"' en sentencia case, despues de: '"+lexemaAnterior+"'"; break;
            case 37: des="Linea "+linea+": Error sintactico! Se espera caracter: '"+cadena+"' en sentencia READ, despues de: '"+lexemaAnterior+"'"; break;
            case 38: des="Linea "+linea+": Error sintactico! Se espera caracter: '"+cadena+"' en sentencia WRITE, despues de: '"+lexemaAnterior+"'"; break;        
            case 39: des="Linea "+linea+": Error sintactico! Se espera el caracter '"+cadena+"' de fin de programa despues de: '"+lexemaAnterior+"'"; break;
            case 40: des="Error sintactico! Revisar ultima linea del archivo fuente, se espera un fin de programa"; break;
            
            //Errores semanticos
            //.......
        }
        
        System.out.println(/*"Error "+codigo+"!\n  "+*/des);
    }
}
