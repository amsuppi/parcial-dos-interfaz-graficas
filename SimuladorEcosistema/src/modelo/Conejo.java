/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;


/**
 *
 * @author msuppi
 */
public class Conejo extends Animal implements Reproducible, Mortal {

    private static final double ENERGIA_MINIMA_REPRODUCCION = 60; // debe superarla
    private static final double COSTO_REPRODUCCION = 25;
    private static final double PROBABILIDAD_REPRODUCCION = 0.15;
    private static final double PENALIZACION_SIN_COMIDA = 15;
    private static final double ENERGIA_PELIGRO = 20;

    public Conejo(String nombre, double energia, int edad, boolean viva,
                  int velocidad, double peso) {
        super(nombre, energia, edad, viva, velocidad, peso);
    }

    /** Primero come y después intenta reproducirse. */
    @Override
    public void actuar(Ecosistema eco) {
        comer(eco);
        intentarReproduccion(eco);
    }

    /** Busca una planta viva y la come; si no hay, pierde energía. */
    @Override
    public void comer(Ecosistema eco) {
        Planta planta = eco.buscarPlantaViva();
        if (planta == null) {
            setEnergia(getEnergia() - PENALIZACION_SIN_COMIDA);
            eco.registrarEvento("Conejo '" + getNombre() + "' no encontró comida (-"
                    + (int) PENALIZACION_SIN_COMIDA + " energia)" + etiquetaPeligro());
            return;
        }
        int energiaGanada = planta.serComida();
        setEnergia(getEnergia() + energiaGanada);
        eco.registrarEvento("Conejo '" + getNombre() + "' comió '"
                + planta.getNombre() + "' (+" + energiaGanada + " energia)");
    }

    @Override
    public String describirEstado() {
        return enPeligro() ? "En peligro" : "Normal";
    }

    /** Solo mira la energía propia; que haya otro conejo se evalúa en reproducirse(), donde hay acceso al ecosistema. */
    @Override
    public boolean puedeReproducirse() {
        return isViva() && getEnergia() > ENERGIA_MINIMA_REPRODUCCION;
    }

    /** Necesita que exista al menos otro conejo vivo (el conteo incluye a este) y algo de suerte. */
    @Override
    public void reproducirse(Ecosistema eco) {
        if (eco.contarConejosVivos() < 2 || Math.random() >= PROBABILIDAD_REPRODUCCION) {
            return;
        }
        eco.nacerConejo(this);
        setEnergia(getEnergia() - COSTO_REPRODUCCION);
    }

    public boolean enPeligro() {
        return getEnergia() < ENERGIA_PELIGRO;
    }

    private String etiquetaPeligro() {
        return enPeligro() ? " [PELIGRO: energia=" + (int) getEnergia() + "]" : "";
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

}
