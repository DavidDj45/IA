/*
 *@file   M18E07a.java
 *@title  M18E07a.
 *@author David Díaz Jiménez
 *@author Andrés Rojas Ortega
 *@email  ddj00003@red.ujaen.es
 *@email  aro00015@red.ujaen.es
 *@date   2018/02/14
 *
 * NoobMouse. A intelligent agent for MouseRun.
 * Copyright (C) 2018  David Díaz Jiménez,
 *                     Andrés Rojas Ortega.
 * 
 * NoobMouse is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NoobMouse is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NoobMouse. If not, see <http://www.gnu.org/licenses/>.
 */

package mouserun.mouse;	
import static java.lang.Math.sqrt;
import mouserun.game.*;		
import java.util.*;

/*
* NoobMouse es un agente inteligente que implementa una fase de exploracion y una fase de molestia a otros ratones
*
*/
public class M18E07a extends Mouse {
    private int                fila;             // Coordenada x del ratón en el mapa
    private int                columna;          // Coordenada y del ratón en el mapa
    private int                cheesex;          // Coordenada x del queso en el mapa
    private int                cheesey;          // Coordenada y del queso en el mapa
    boolean                    newcheese;        // Indica si ha aparecido un nuevo queso en el mapa
    Grid                       lastGrid;         // Última casilla que visitó el ratón
    int                        bombas;           // Número de bombas disponibles
    boolean                    quesoEncontrado;  // Indica si se ha encontrado el queso
    Stack<Integer>             prohibidos;       // Almacena las casillas prohibidas
    HashMap<Integer, Casilla>  Tablero;          // Estructura que contiene las casillas exploradas del mapa
    float                      rate= 0.65f;      // Factor de carga del mapa hash
    
    // Busqueda no informada
    private final Stack<Integer> camino;           // Para la busqueda no informada, contiene la pila de movientos hacia el queso
    private int                  backtrack;        // 
    private boolean              caminoEncontrado; // 
    
    public M18E07a(){
        super("NoobMouse");
        Tablero          = new HashMap<Integer, Casilla>(1900,rate);
        camino           = new Stack<Integer>();
        prohibidos       = new Stack();
        newcheese        =false;
        bombas           =5;
        quesoEncontrado  =false;
	}
    
    
    /*
    *@brief Calcula el valor hash de la casilla
    *@param x Entero que representa el valor de la coordenada x en el mapa
    *@param y Entero que representa el valor de la coordenada y en el mapa
    *@return El valor hash de la casilla
    */
    private int hash(int x, int y){ return x*10001 + y; }
    
    
    /*
    *@brief Inserta la nueva Casilla en el mapa hash
    *@param currentGrid Casilla en la que estamos actualmente
    */
    public void nuevaCasilla(Grid currentGrid){ 
            if(!Tablero.containsKey(hash(fila, columna))){
                Casilla value = new Casilla();
                Tablero.put(hash(fila, columna), value);
                this.incExploredGrids(); // Se incrementa el contador de las casillas exploradas
      
                Tablero.get(hash(fila, columna)).down = currentGrid.canGoDown();
                Tablero.get(hash(fila, columna)).up   = currentGrid.canGoUp();
                Tablero.get(hash(fila, columna)).left = currentGrid.canGoLeft();
                Tablero.get(hash(fila, columna)).down = currentGrid.canGoRight();
            }
        }
    
    
    public void reseteoProfundidad(){
            Iterator it = Tablero.entrySet().iterator();
            while(it.hasNext()){
                HashMap.Entry<Integer, Casilla> pair =  (HashMap.Entry<Integer, Casilla>)it.next();
                pair.getValue().hashPadre = -1;
                pair.getValue().setNoVisitado();
            }
        }
    
    
    public void llamaProfundidad(){
            //Resetea los caminos marcados como prohibidos en el caso de que el queso caiga en uno
            borrarProhibidos();
            //Vaciamos el camino
            camino.clear();
            
            //Si el queso y el ratón están en casilla conocida, llama a la búsqueda en anchura
            if(Tablero.containsKey(hash(cheesex, cheesey)) && Tablero.containsKey(hash(fila, columna))){
                
                //Anchura(hash(fila, columna), hash(cheesex, cheesey));
                caminoEncontrado = false;
            }
            
            //Si el ratón está en casilla conocida, pero no el queso, comprueba si puede acercarlo a 
            //una casilla explorada cercana al queso
            if(!Tablero.containsKey(hash(cheesex, cheesey)) && Tablero.containsKey(hash(fila, columna))){
                Stack<Integer> casillasProximas;
                casillasProximas = new Stack<>();
                     
                //Comprueba si en un radio de 2 casillas alrededor de la del queso hay alguna visitada
                boolean casillaAñadida = false;
                int x = cheesex - 2;
                int y = cheesey - 2;
                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 5; j++) {
                        if(Tablero.containsKey(hash(x+i,y+j))){
                            if(casillaAñadida == false && Tablero.get(hash(x+i, y+j)).up && !Tablero.containsKey(hash(x+i, y+j+1))){
                                casillasProximas.add(hash(x+i,y+j));
                                casillaAñadida = true;
                            }

                            if(casillaAñadida == false && Tablero.get(hash(x+i, y+j)).down && !Tablero.containsKey(hash(x+i, y+j-1))){
                                casillasProximas.add(hash(x+i,y+j));
                                casillaAñadida = true;
                            }

                            if(casillaAñadida == false && Tablero.get(hash(x+i, y+j)).right && !Tablero.containsKey(hash(x+i+1, y+j))){
                                casillasProximas.add(hash(x+i,y+j));
                                casillaAñadida = true;
                            }

                            if(casillaAñadida == false && Tablero.get(hash(x+i, y+j)).left && !Tablero.containsKey(hash(x+i-1, y+j))){
                                casillasProximas.add(hash(x+i,y+j));
                                casillaAñadida = true;
                            }
                        }
                        casillaAñadida = false;
                    }
                }
                
                /**
                 * Si hay una o varias casillas, las ordena de menor a mayor hash y
                 * llama a la busqueda en anchura con el hash de en medio como hashObjetivo
                 */
                if(!casillasProximas.empty()){
                    casillasProximas.sort(null);
                    //Anchura(hash(fila, columna), casillasProximas.get(casillasProximas.size()/2));
                }
            } 

        }
    
    
    /*
    *@brief Función encargada de desbloquear los caminos prohibidos
    */
    public void borrarProhibidos(){
        if( (Tablero.containsKey(hash(cheesex,cheesey)) && Tablero.get(hash(cheesex,cheesey)).prohibido==true) || 
                (Tablero.containsKey(hash(fila,columna)) && Tablero.get(hash(fila,columna)).prohibido==true)){
            for(int a=0;a<prohibidos.size();a++){
                Tablero.get(prohibidos.get(a)).Reset();
                
            }
            newcheese=false;    
        }
        
    }
    
    
  
    @Override
    public int move(Grid currentGrid, Cheese cheese){
        
            fila = currentGrid.getX();
            columna = currentGrid.getY();
            cheesex= cheese.getX();
            cheesey= cheese.getY();
               
            if (newcheese == true){ borrarProhibidos(); }
            
                nuevaCasilla(currentGrid);
                Tablero.get(hash(fila,columna)).setCont();//Incrementamos el número de veces que hemos pasado por la casilla
             
                ArrayList<Integer> explorado = new ArrayList<Integer>();
                Prohibido(currentGrid,explorado);
                    
                int queso = radarQueso(currentGrid); // Comprobamos si el queso
                if ( queso!=-1 ){ return queso; }
                
                 if( bombas>0  &&  explorado.size()>=3 ){
                     int a =bomb();
                     if ( a != -1  &&  Tablero.get(hash(fila,columna)).getCont()>5 ){ bombas --; return a; }
                     if ( explorado.size()==4 && a!=-1 ){  bombas--; return a;  }
                    }   
                                         
		ArrayList<Integer> possibleMoves = new ArrayList<Integer>();
		if ( currentGrid.canGoUp()    &&   !Tablero.containsKey(hash(fila,columna+1)) ) possibleMoves.add(Mouse.UP);
		if ( currentGrid.canGoLeft()  &&   !Tablero.containsKey(hash(fila-1,columna)) ) possibleMoves.add(Mouse.LEFT);
		if ( currentGrid.canGoRight() &&   !Tablero.containsKey(hash(fila+1,columna)) ) possibleMoves.add(Mouse.RIGHT);  
                if ( currentGrid.canGoDown()  &&   !Tablero.containsKey(hash(fila,columna-1)) ) possibleMoves.add(Mouse.DOWN);
                   
		if ( possibleMoves.size() >= 1 ){        
                         lastGrid=currentGrid;       
                         return possibleMoves.get(0);
		}else{
                    
                    int aux=0;
                    aux = this.unlock(currentGrid);
                    
                    if ( aux!=-1 ){ lastGrid=currentGrid; return aux;  }
                    
                    if ( explorado.size()==1 ){ lastGrid=currentGrid; return explorado.get(0); }
                    
                    for( int a=0; a< explorado.size();a++ ){ //Llama a la función testGrid para eleiminar el camino por el que hemos llegado
                        if ( testGrid(explorado.get(a) )==true){ explorado.remove(explorado.get(a)); }
                     }
                       
                    switch( explorado.size() ){    
                        case 1: 
                            lastGrid=currentGrid;
                            return explorado.get(0);   
                        case 2:
                            lastGrid=currentGrid;
                            return ShortMove(explorado.get(0), explorado.get(1));
                        case 3: 
                            lastGrid=currentGrid;
                            return ShortMove(ShortMove(explorado.get(0), explorado.get(1)), explorado.get(2));   
                        case 4:
                            lastGrid=currentGrid;
                            return ShortMove(ShortMove(ShortMove(explorado.get(0), explorado.get(1)), explorado.get(2)),explorado.get(4));
                    }
                }     
                return Mouse.BOMB;
        }

    @Override
    public void newCheese() { //Se activa el evento de newCheese en el main para determinar si el queso está presente en uno de
                                //los caminos prohibidos para desbloquearlo                         
      newcheese= true;
     
    }
    
    
    @Override
    public void respawned() {
        
        newcheese=true;
          
    }
    
    
    /*
    *@brief Funcion que se encarga de eliminar el ultimo movimiento realizado
    *@param direction Movimiento a evaluar
    */ 
    private boolean testGrid(int direction){ //Elimina el ultimo movimiento realizado para no repertirlo
            if ( lastGrid == null ){ return false; }    
            int x = fila;
            int y = columna;
            switch ( direction ){
                    case Mouse.UP:
                            y += 1;
                            break;

                    case Mouse.DOWN:
                            y -= 1;
                            break;

                    case Mouse.LEFT:
                            x -= 1;
                        break;

                    case Mouse.RIGHT:
                            x += 1;
                            break;
            }
            if( lastGrid.getX() == x && lastGrid.getY() == y ) return true;
            return false;

	}
    
    
   /*
   *@brief Funcion que comprueba si se puede colocar una bomba en la posicion actual
   */
   private int bomb(){
      if( Tablero.get(hash(fila,columna)).bomba!=true ){
          Tablero.get(hash(fila,columna)).bomba=true;
          return Mouse.BOMB; }
       return -1;
    }
   
            
   /*
   *@brief Funcion que comprueba si el contador=1 para algun movimiento
   *@param currentGrid Posicion actual del ratón en el mapa
   *@return Devuelve el movimiento en caso de encontralo o -1
   */
    private int unlock(Grid currentGrid){
        
        ArrayList<Integer> caminoelegido= new ArrayList();
        int contador=0;
        
        if ( currentGrid.canGoUp()   && Tablero.get(hash(fila,columna+1)).isProhibido()!=true && Tablero.get(hash(fila,columna+1)).getCont()==1 ){
            caminoelegido.add(Mouse.UP);
            contador+=1; }
        if ( currentGrid.canGoRight()&& Tablero.get(hash(fila+1,columna)).isProhibido()!=true && Tablero.get(hash(fila+1,columna)).getCont()==1 ){      
            caminoelegido.add(Mouse.RIGHT);
            contador+=1; }
        if ( currentGrid.canGoLeft() && Tablero.get(hash(fila-1,columna)).isProhibido()!=true && Tablero.get(hash(fila-1,columna)).getCont()==1 ){
            caminoelegido.add(Mouse.LEFT); 
            contador+=1; }
        if ( currentGrid.canGoDown() && Tablero.get(hash(fila,columna-1)).isProhibido()!=true && Tablero.get(hash(fila,columna-1)).getCont()==1 ){ 
            caminoelegido.add(Mouse.DOWN);
            contador+=1; }
        
        if ( contador==1 ){ 
            lastGrid=currentGrid;
            return caminoelegido.get(0);
          
         }else if ( contador>=1 ){
             
            for( int a=0; a< caminoelegido.size();a++ ){ //Llama a la función testGrid para eleiminar el camino por el que hemos llegado
                if ( testGrid(caminoelegido.get(a))==true ){  
                    caminoelegido.remove(caminoelegido.get(a)); 
                    }
                }
            
            lastGrid=currentGrid;
            return caminoelegido.get(0); 
            
         }  
        
        return -1; 
    }
    
    
   /*
   *@brief Determina si la casilla actual debe ser prohibida 
   *@param currentGrid Posicion actual del ratón en el mapa
   *@param explorado ArraList que contiene los movientos 
   */
    private void Prohibido(Grid currentGrid,ArrayList<Integer> explorado){//Determina si es un camino prohibido
        if ( currentGrid.canGoUp() ){ 
            if (Tablero.containsKey(hash(fila,columna+1))){
                    if(!Tablero.get(hash(fila,columna+1)).isProhibido())
                        explorado.add(Mouse.UP);
            } else { explorado.add(Mouse.UP); }
        }
        
        if ( currentGrid.canGoLeft() ){
           if ( Tablero.containsKey(hash(fila-1,columna)) ){
                    if ( !Tablero.get(hash(fila-1,columna)).isProhibido() )
                        explorado.add(Mouse.LEFT);
            } else { explorado.add(Mouse.LEFT); }
        }

        if ( currentGrid.canGoRight() ){
            if (Tablero.containsKey(hash(fila+1,columna))){
                if (!Tablero.get(hash(fila+1,columna)).isProhibido())
                    explorado.add(Mouse.RIGHT);
            } else { explorado.add(Mouse.RIGHT); }
        }
        
        if ( currentGrid.canGoDown() ){ 
            if ( Tablero.containsKey(hash(fila,columna-1)) ){
                if ( !Tablero.get(hash(fila,columna-1)).isProhibido() )
                   explorado.add(Mouse.DOWN);
            } else { explorado.add(Mouse.DOWN); }
        }
        
        if ( explorado.size()==1 ){ 
            this.Tablero.get(hash(fila,columna)).setProhibido();  
            prohibidos.add(hash(fila, columna)); 
        }  
        //System.out.printf("El camino está prohibido "+this.Tablero[fila][columna].isProhibido()+" Valor de x "+x+"\n" );
    }
    
    
    /*
    *@brief Funcion que determina si el queso ha caido en una casilla explorada
    */
    private void comprobarQueso(){  if ( Tablero.containsKey(hash(cheesex, cheesey)) )    quesoEncontrado=true ; }
    
    
    /*
    *@brief Funcion que realiza una serie de evaluaciones para determinar el moviento a realizar  
    *@param mov1 Posible moviento
    *@param mov2 Posible moviento
    *@return Devuelve el moviento a realizar
    */
    private int ShortMove(int mov1, int mov2){ //Determina que movimiento realizar
            int fila1 = fila, fila2 = fila, col1 = columna, col2 = columna;
            switch( mov1 ){
                case Mouse.UP:
                    col1++;
                    break;
                case Mouse.DOWN:
                    col1--;
                    break;
                case Mouse.LEFT:
                    fila1--;
                    break;
                case Mouse.RIGHT:
                    fila1++;
                    break;
            }//Posicion del ratón tras hacer el supuesto movimiento
            switch( mov2 ){
                case Mouse.UP:
                    col2++;
                    break;
                case Mouse.DOWN:
                    col2--;
                    break;
                case Mouse.LEFT:
                    fila2--;
                    break;
                case Mouse.RIGHT:
                    fila2++;
                    break;
            }//Posicion del ratón tras hacer el supuesto movimiento
           
            if( Tablero.get(hash(fila1,col1)).getCont() >= Tablero.get(hash(fila2,col2)).getCont()+2 ){
                return mov2;
                
            } else {
                if ( Tablero.get(hash(fila1,col1)).getCont() <= Tablero.get(hash(fila2,col2)).getCont()-2 ){
                    return mov1;
                    
                } else {
                    if ( sqrt((cheesex-fila1)^2 + (cheesey-col1)^2) < sqrt((cheesex-fila2)^2 + (cheesey-col2)^2) ){
                        return mov1;
                        
                    } else {
                        if ( sqrt((cheesex-fila1)^2 + (cheesey-col1)^2) > sqrt((cheesex-fila2)^2 + (cheesey-col2)^2) ){
                            return mov2;
                            
                        }else{
                            Random random = new Random();
                            if ( random.nextInt()%2 == 1 ){
                                return mov1;
                                
                            } else {
                                
                                return mov2;
                            }
                        }
                    }           
                }
            }
        }
    
    
    /*
    *@brief Funcion que compueba si el queso se encuentra en uno de los movientos posibles
    *@param currentGrid Posicion actual del ratón en el mapa
    *@return Devuelve el moviento para alcanzar el queso en caso de existir o -1
    */
    private int radarQueso(Grid currentGrid){ //Comprueba si alguno de los movimientos disponibles es la ubicacion del queso
        if ( currentGrid.canGoUp() && (currentGrid.getX()==cheesex && currentGrid.getY()+1==cheesey) )   return Mouse.UP;
        if ( currentGrid.canGoRight()&& (currentGrid.getX()+1==cheesex && currentGrid.getY()==cheesey) ) return Mouse.RIGHT;
        if ( currentGrid.canGoLeft()&& (currentGrid.getX()-1==cheesex && currentGrid.getY()==cheesey) )  return Mouse.LEFT;
        if ( currentGrid.canGoDown()&& (currentGrid.getX()==cheesex && currentGrid.getY()-1==cheesey) )  return Mouse.DOWN;
        
        return -1;
    }
    
    
    /*
    *@brief Clase que contiene la informacion que vamos recogiendo de la exploracion del laberinto
    */
    public class Casilla{
        private int      cont;       // Número de veces que se ha visitado la casilla
        private boolean  prohibido;  // Indica si una casilla es prohibida( Número de movientos = 1 )
        private boolean  bomba;      // Indica si se ha colocado una bomba en la casilla
        boolean          up;         // Indica si se puede acceder a la casilla superior desde la actual
        boolean          right;      // Indica si se puede acceder a la casilla de la derecha desde la actual
        boolean          left;       // Indica si se puede acceder a la casilla de la izquierda desde la actual
        boolean          down;       // Indica si se puede acceder a la casilla inferior desde la actual
        
        // Para la busqueda no informada
        boolean          visitado;   // Indica si la casilla ya ha sido explorada en la busqueda
        int              hashPadre;  // Contiene el valor hash de la casilla desde la que hemos accedido en la busqueda en la actual 
        
        public Casilla(){
            cont       =0;
            prohibido  =false;
            bomba      =false;
            up         =false;
            right      =false;
            left       =false;
            down       =false; 
            
            //Para la busqueda no informada

            boolean vistado =false;
            hashPadre       =-1;
             }
        
        public int  getCont() { return this.cont; }

        public void setCont() {  this.cont+=1; }
   
        public boolean isProhibido() { return prohibido; }

        public void setProhibido() { this.prohibido =true; }
        
        public void Reset(){ prohibido = false; }
        
        public void setVisitado(){ this.visitado = true; }
            
        public void setNoVisitado(){ this.visitado = false; }
        
        public boolean getVisitado(){ return this.visitado; }
    }
    
}
