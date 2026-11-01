/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;


public class Lobo extends Animal implements Mortal {

    private static final double PROBABILIDAD_BASE = 0.10;
    private static final double PROBABILIDAD_POR_ENERGIA = 0.003; // cada punto de energía suma 0.3%
    private static final double PROBABILIDAD_MINIMA = 0.05;
    private static final double PROBABILIDAD_MAXIMA = 0.95;
    private static final double ENERGIA_POR_CACERIA = 30;

    private int exitosCaza;

    public Lobo(String nombre, double energia, int edad, boolean viva,
                int velocidad, double peso) {
        super(nombre, energia, edad, viva, velocidad, peso);
        this.exitosCaza = 0;
    }

    @Override
    public void actuar(Ecosistema eco) {
        comer(eco);
    }

    /**
     * Elige un conejo vivo al azar e intenta cazarlo. Cuanta más energía tiene
     * el lobo, más probable es el éxito; el clima puede sumar un bonus (Invierno).
     */
    @Override
    public void comer(Ecosistema eco) {
        Conejo presa = eco.buscarConejoVivo();
        if (presa == null) {
            return;
        }

        if (Math.random() < probabilidadDeExito(eco)) {
            presa.morir();
            setEnergia(getEnergia() + ENERGIA_POR_CACERIA);
            exitosCaza++;
            eco.registrarEvento("Lobo '" + getNombre() + "' cazó a Conejo '"
                    + presa.getNombre() + "' (+" + (int) ENERGIA_POR_CACERIA
                    + " energia) [cacerías: " + exitosCaza + "]");
        } else {
            eco.registrarEvento("Lobo '" + getNombre() + "' falló la caza");
        }
    }

    private double probabilidadDeExito(Ecosistema eco) {
        double probabilidad = PROBABILIDAD_BASE
                + PROBABILIDAD_POR_ENERGIA * getEnergia()
                + eco.getClimaActual().getBonusCaza();
        return Math.max(PROBABILIDAD_MINIMA, Math.min(PROBABILIDAD_MAXIMA, probabilidad));
    }

    @Override
    public void mostrarEstado() {
        System.out.println("Lobo: " + getNombre()
                + " | Energía: " + getEnergia()
                + " | Cacerías exitosas: " + exitosCaza);
    }

    @Override
    public boolean estaVivo() {
        return isViva();
    }

    @Override
    public void morir() {
        setViva(false);
        setEnergia(0);
    }

    public int getExitosCaza() {
        return exitosCaza;
    }

    public void setExitosCaza(int exitosCaza) {
        this.exitosCaza = Math.max(0, exitosCaza);
    }

}
