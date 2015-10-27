package semantico;

import java.util.Stack;

public class Semantico {
    private Stack<TablaDeSimbolos> pila ;
    
    public Semantico(Stack<TablaDeSimbolos> p){
        this.pila = new Stack<TablaDeSimbolos>();
    }
    
    public void agregarElemento(String nombre, Identificador id){
        this.pila.firstElement().agregar(nombre, id);
    }
    
    public void nuevoAmbiente(String nombre){
        TablaDeSimbolos padre = this.pila.peek();//Obtengo el tope como padre del nuevo
        this.pila.add(new TablaDeSimbolos(nombre,padre));
    }
}
