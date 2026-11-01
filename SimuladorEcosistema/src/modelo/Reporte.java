package modelo;

/**
 * Calcula los datos del reporte final a partir del Ecosistema.
 * No imprime ni usa Swing: devuelve los datos para que el controlador se los pase a la vista.
 *
 * TODO (Etapa 3): implementar los cálculos.
 * La lógica de referencia es la del método generarReporteFinal() de la IE1
 * (se puede ver con: git show a8b57ac:SimuladorEcosistema/src/modelo/Ecosistema.java).
 */
public class Reporte {

    private final Ecosistema eco;

    public Reporte(Ecosistema eco) {
        this.eco = eco;
    }

    /** "Se completaron los 20 turnos" o "Colapso: se extinguieron conejos, lobos". */
    public String getCausaFin() {
        // TODO Etapa 3
        return "";
    }

    public int getTurnosJugados() {
        return eco.getTurnoActual();
    }

    public Clima getClimaFinal() {
        return eco.getClimaActual();
    }

    /** Número de turno (1..n) con más eventos; 0 si no hubo turnos. */
    public int getTurnoMayorActividad() {
        // TODO Etapa 3
        return 0;
    }

    public int getEventosTurnoMayorActividad() {
        // TODO Etapa 3
        return 0;
    }

    /** null si nunca hubo plantas. */
    public Planta getPlantaMasLongeva() {
        // TODO Etapa 3
        return null;
    }

    public Conejo getConejoMasLongevo() {
        // TODO Etapa 3
        return null;
    }

    public Lobo getLoboMasLongevo() {
        // TODO Etapa 3
        return null;
    }

    /** Lobo con más cacerías exitosas (entre todos los que existieron). */
    public Lobo getMejorCazador() {
        // TODO Etapa 3
        return null;
    }

    // ---- Datos listos para mostrar ----

    /** Filas {etiqueta, valor}: Causa de fin, Turnos jugados, Clima final, Turno de mayor actividad, Lobo con más cacerías. */
    public String[][] getResumen() {
        // TODO Etapa 3
        return new String[0][];
    }

    /** Filas {tipo, nombre, edad (turnos), estado ("Viva"/"Murió")} para Planta, Conejo y Lobo. */
    public String[][] getFilasLongevas() {
        // TODO Etapa 3
        return new String[0][];
    }

    /** Filas {tipo, nacimientos, muertes} para Plantas, Conejos y Lobos (los lobos no se reproducen). */
    public String[][] getFilasNacimientosMuertes() {
        // TODO Etapa 3
        return new String[0][];
    }

    /** Bonus. Filas {tipo, máximo, turno del máximo, mínimo, turno del mínimo}. */
    public String[][] getFilasPoblacion() {
        // TODO Etapa 3
        return new String[0][];
    }
}
