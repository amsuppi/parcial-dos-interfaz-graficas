package modelo;


/**
 * Contrato para las entidades que pueden reproducirse (Planta y Conejo).
 * El Ecosistema las guarda juntas en un ArrayList<Reproducible> y las
 * recorre en un solo for, sin importar si son plantas o conejos (polimorfismo).
 */
public interface Reproducible {

    /** Crea la nueva entidad. Cada clase decide cómo (Planta y Conejo lo hacen distinto). */
    void reproducirse(Ecosistema eco);

    /** Devuelve true si la entidad está en condiciones de reproducirse. */
    boolean puedeReproducirse();

    /**
     * Método default: ya tiene código dentro de la interfaz, así que
     * Planta y Conejo lo heredan sin tener que escribirlo.
     * Primero pregunta si puede y, solo si puede, se reproduce.
     */
    default void intentarReproduccion(Ecosistema eco) {
        if (puedeReproducirse()) {
            reproducirse(eco);
        }
    }
}
