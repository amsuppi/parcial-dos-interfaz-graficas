package modelo;

/**
 * Enum con los 4 climas posibles. Cada clima guarda sus efectos,
 * así el resto del código solo le pregunta al clima actual y no
 * necesita un if/switch por cada clima.
 */
public enum Clima {

    //        nombre      plantas  conejos  lobos  bonusCaza
    SOLEADO  ("Soleado",   1.5,     5,       0,    0.0),
    LLUVIOSO ("Lluvioso",  2.0,     3,      -5,    0.0),
    SEQUIA   ("Sequía",    0.5,    -5,       0,    0.0),
    INVIERNO ("Invierno",  0.0,    -8,       0,    0.20);

    private final String nombre;
    private final double multiplicadorPlantas; // multiplica la probabilidad de reproducción de las plantas
    private final int efectoConejos;           // energía que suman/restan los conejos por turno
    private final int efectoLobos;             // energía que suman/restan los lobos por turno
    private final double bonusCaza;            // se suma a la probabilidad de caza del lobo (0.20 = +20%)

    Clima(String nombre, double multiplicadorPlantas, int efectoConejos,
          int efectoLobos, double bonusCaza) {
        this.nombre = nombre;
        this.multiplicadorPlantas = multiplicadorPlantas;
        this.efectoConejos = efectoConejos;
        this.efectoLobos = efectoLobos;
        this.bonusCaza = bonusCaza;
    }

    public String getNombre() {
        return nombre;
    }

    public double getMultiplicadorPlantas() {
        return multiplicadorPlantas;
    }

    public int getEfectoConejos() {
        return efectoConejos;
    }

    public int getEfectoLobos() {
        return efectoLobos;
    }

    public double getBonusCaza() {
        return bonusCaza;
    }

    /** Para elegir el clima desde un menú: 1=Soleado, 2=Lluvioso, 3=Sequía, 4=Invierno. */
    public static Clima desdeOpcion(int opcion) {
        return values()[opcion - 1];
    }

    @Override
    public String toString() {
        return nombre;
    }
}
