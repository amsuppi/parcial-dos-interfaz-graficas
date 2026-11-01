/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;


/**
 *
 * @author msuppi
 */
public abstract class Animal extends Entidad {
    
    private int velocidad;
    private double peso;
    
    
    public Animal(String nombre, double energia, int edad, boolean viva,
                  int velocidad, double peso) {
        super(nombre, energia, edad, viva);
        this.velocidad = velocidad;
        this.peso = peso;
    }
    
    public abstract void comer(Ecosistema eco);

    public void moverse() {
        System.out.println(getNombre() + " se desplazó.");
    }

    public int getVelocidad() {
        return velocidad;
    }

    public void setVelocidad(int velocidad) {
        this.velocidad = Math.max(0, velocidad);
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = Math.max(0, peso);
    }
    
}
