/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

public interface Mortal {
    
    boolean estaVivo();

    void morir();

    default void verificarMuerte() {
        if (this instanceof Entidad) {
            Entidad entidad =
                    (Entidad) this;

            if (entidad.getEnergia() <= 0 && entidad.isViva()) {
                morir();
                System.out.println(entidad.getClass().getSimpleName() + " '"
                        + entidad.getNombre() + "' murió de inanición");
            }
        }
    }
    
}
