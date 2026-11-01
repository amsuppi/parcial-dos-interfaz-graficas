/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

public interface Mortal {
    
    boolean estaVivo();

    void morir();

    /**
     * Si la entidad se quedó sin energía, muere. Devuelve true si murió en esta
     * verificación, así el Ecosistema registra el evento (el modelo no imprime).
     */
    default boolean verificarMuerte() {
        if (this instanceof Entidad) {
            Entidad entidad = (Entidad) this;
            if (entidad.getEnergia() <= 0 && entidad.isViva()) {
                morir();
                return true;
            }
        }
        return false;
    }
    
}
