/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;


public class Planta extends Entidad implements Reproducible {

    // Probabilidad base de reproducirse por turno; el clima la multiplica.
    private static final double PROBABILIDAD_BASE = 0.90;
    private static final double ENERGIA_MINIMA_REPRODUCCION = 25;
    private static final int CAPACIDAD_MAXIMA = 80; // con tantas plantas ya no hay lugar para más
    private static final double ENERGIA_BASE = 1;   // las plantas gastan menos que los animales (5)

    private int tamanio;

    public Planta(String nombre, double energia, int edad, boolean viva,
                  int tamanio) {
        super(nombre, energia, edad, viva);
        this.setTamanio(tamanio);
    }

    /** El ecosistema no llama a actuar() en las plantas; su única acción es reproducirse. */
    @Override
    public void actuar(Ecosistema eco) {
        intentarReproduccion(eco);
    }

    /** Sobrescribe el envejecimiento de Entidad: una planta produce su energía, así que gasta menos. */
    @Override
    public void envejecer() {
        setEdad(getEdad() + 1);
        setEnergia(getEnergia() - ENERGIA_BASE);
    }

    @Override
    public void mostrarEstado() {
        System.out.println("Planta: " + getNombre()
                + " | Tamaño: " + tamanio
                + " | Energía: " + getEnergia());
    }

    /** Solo mira la energía propia; el clima se evalúa en reproducirse(), donde hay acceso al ecosistema. */
    @Override
    public boolean puedeReproducirse() {
        return isViva() && getEnergia() >= ENERGIA_MINIMA_REPRODUCCION;
    }

    /**
     * La probabilidad depende del multiplicador del clima (en Invierno vale 0 y
     * la planta nunca se reproduce; en Lluvioso se duplica) y baja a medida que
     * el ecosistema se llena de plantas, para que no crezcan sin límite.
     */
    @Override
    public void reproducirse(Ecosistema eco) {
        double espacioLibre = 1.0 - (double) eco.getPlantas().size() / CAPACIDAD_MAXIMA;
        double probabilidad = PROBABILIDAD_BASE
                * eco.getClimaActual().getMultiplicadorPlantas()
                * Math.max(0, espacioLibre);
        if (Math.random() >= probabilidad) {
            return;
        }
        eco.nacerPlanta(this);
    }

    /**
     * La planta se marca como muerta al ser comida, así el ecosistema la
     * saca de la lista sin registrarla como "marchita".
     */
    public int serComida() {
        setEnergia(0);
        setViva(false);
        return tamanio * 10;
    }

    public int getTamanio() {
        return tamanio;
    }

    public void setTamanio(int tamanio) {
        this.tamanio = Math.max(1, Math.min(5, tamanio));
    }

}
