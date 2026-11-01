/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;


/**
 *
 * @author msuppi
 */
public abstract class Entidad {
    
    private String nombre;
    private double energia;
    private int edad;
    private boolean viva;

    public Entidad(String nombre, double energia, int edad, boolean viva) {
        this.nombre = nombre;
        this.setEnergia(energia);
        this.setEdad(edad);
        this.viva = viva;
    }

    public abstract void actuar(Ecosistema eco);

    public abstract void mostrarEstado();

    public void envejecer() {
        edad++;
        setEnergia(energia - 5);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre != null && !nombre.trim().isEmpty()) {
            this.nombre = nombre;
        }
    }

    public double getEnergia() {
        return energia;
    }

    public void setEnergia(double energia) {
        this.energia = Math.max(0, energia);
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = Math.max(0, edad);
    }

    public boolean isViva() {
        return viva;
    }

    public void setViva(boolean viva) {
        this.viva = viva;
    }
    
}
