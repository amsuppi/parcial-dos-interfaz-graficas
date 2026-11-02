package modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Calcula los datos del reporte final a partir del Ecosistema.
 * No imprime ni usa Swing: devuelve los datos para que el controlador se los pase a la vista.
 *
 * La lógica sale del método generarReporteFinal() de la IE1, pero en lugar de
 * imprimir por consola devuelve objetos y filas de texto listas para mostrar.
 */
public class Reporte {

    private final Ecosistema eco;

    public Reporte(Ecosistema eco) {
        this.eco = eco;
    }

    /** "Se completaron los 20 turnos" o "Colapso: se extinguieron conejos, lobos". */
    public String getCausaFin() {
        if (eco.ecosistemaColapsado()) {
            List<String> extintas = new ArrayList<>();
            if (eco.getPlantas().isEmpty()) {
                extintas.add("plantas");
            }
            if (eco.getConejos().isEmpty()) {
                extintas.add("conejos");
            }
            if (eco.getLobos().isEmpty()) {
                extintas.add("lobos");
            }
            return "Colapso: se extinguieron " + String.join(", ", extintas);
        }
        return "Se completaron los " + eco.getTurnoActual() + " turnos";
    }

    public int getTurnosJugados() {
        return eco.getTurnoActual();
    }

    public Clima getClimaFinal() {
        return eco.getClimaActual();
    }

    /** Número de turno (1..n) con más eventos; 0 si no hubo turnos. */
    public int getTurnoMayorActividad() {
        List<Integer> eventos = eco.getEventosPorTurno();
        if (eventos.isEmpty()) {
            return 0;
        }
        int posicionMax = 0;
        for (int i = 1; i < eventos.size(); i++) {
            if (eventos.get(i) > eventos.get(posicionMax)) {
                posicionMax = i;
            }
        }
        return posicionMax + 1; // la posición 0 es el turno 1
    }

    public int getEventosTurnoMayorActividad() {
        int turno = getTurnoMayorActividad();
        if (turno == 0) {
            return 0;
        }
        return eco.getEventosPorTurno().get(turno - 1);
    }

    /** null si nunca hubo plantas. */
    public Planta getPlantaMasLongeva() {
        return masLongeva(eco.getTodasLasPlantas());
    }

    public Conejo getConejoMasLongevo() {
        return masLongeva(eco.getTodosLosConejos());
    }

    public Lobo getLoboMasLongevo() {
        return masLongeva(eco.getTodosLosLobos());
    }

    /** Lobo con más cacerías exitosas (entre todos los que existieron). */
    public Lobo getMejorCazador() {
        Lobo mejor = null;
        for (Lobo l : eco.getTodosLosLobos()) {
            if (mejor == null || l.getExitosCaza() > mejor.getExitosCaza()) {
                mejor = l;
            }
        }
        return mejor;
    }

    // ---- Datos listos para mostrar ----

    /** Filas {etiqueta, valor}: Causa de fin, Turnos jugados, Clima final, Turno de mayor actividad, Lobo con más cacerías. */
    public String[][] getResumen() {
        return new String[][]{
            {"Causa de fin", getCausaFin()},
            {"Turnos jugados", String.valueOf(getTurnosJugados())},
            {"Clima final", getClimaFinal().getNombre()},
            {"Turno de mayor actividad", textoTurnoMayorActividad()},
            {"Lobo con más cacerías exitosas", textoMejorCazador()}
        };
    }

    /** Filas {tipo, nombre, edad (turnos), estado ("Viva"/"Murió")} para Planta, Conejo y Lobo. */
    public String[][] getFilasLongevas() {
        List<String[]> filas = new ArrayList<>();
        agregarFilaLongeva(filas, "Planta", getPlantaMasLongeva());
        agregarFilaLongeva(filas, "Conejo", getConejoMasLongevo());
        agregarFilaLongeva(filas, "Lobo", getLoboMasLongevo());
        return filas.toArray(new String[0][]);
    }

    /** Filas {tipo, nacimientos, muertes} para Plantas, Conejos y Lobos (los lobos no se reproducen). */
    public String[][] getFilasNacimientosMuertes() {
        return new String[][]{
            {"Plantas", String.valueOf(eco.getNacimientosPlantas()), String.valueOf(eco.getMuertesPlantas())},
            {"Conejos", String.valueOf(eco.getNacimientosConejos()), String.valueOf(eco.getMuertesConejos())},
            {"Lobos", "0", String.valueOf(eco.getMuertesLobos())}
        };
    }

    /** Bonus. Filas {tipo, máximo, turno del máximo, mínimo, turno del mínimo}. */
    public String[][] getFilasPoblacion() {
        return new String[][]{
            filaMaxMin("Plantas", eco.getHistorialPlantas()),
            filaMaxMin("Conejos", eco.getHistorialConejos()),
            filaMaxMin("Lobos", eco.getHistorialLobos())
        };
    }

    // ---- Auxiliares privados ----

    /** Devuelve la entidad de mayor edad de la lista, o null si está vacía. */
    private <T extends Entidad> T masLongeva(List<T> lista) {
        T mayor = null;
        for (T e : lista) {
            if (mayor == null || e.getEdad() > mayor.getEdad()) {
                mayor = e;
            }
        }
        return mayor;
    }

    private void agregarFilaLongeva(List<String[]> filas, String tipo, Entidad e) {
        if (e == null) {
            filas.add(new String[]{tipo, "-", "-", "-"});
            return;
        }
        filas.add(new String[]{
            tipo,
            e.getNombre(),
            e.getEdad() + (e.getEdad() == 1 ? " turno" : " turnos"),
            e.isViva() ? "Viva" : "Murió"
        });
    }

    private String textoTurnoMayorActividad() {
        int turno = getTurnoMayorActividad();
        if (turno == 0) {
            return "Sin turnos jugados";
        }
        int eventos = getEventosTurnoMayorActividad();
        return "Turno " + turno + " (" + eventos + (eventos == 1 ? " evento)" : " eventos)");
    }

    private String textoMejorCazador() {
        Lobo mejor = getMejorCazador();
        if (mejor == null) {
            return "No hubo lobos";
        }
        int cacerias = mejor.getExitosCaza();
        if (cacerias == 0) {
            return "Ninguno (0 cacerías)";
        }
        return mejor.getNombre() + " (" + cacerias + (cacerias == 1 ? " cacería)" : " cacerías)");
    }

    private String[] filaMaxMin(String tipo, List<Integer> historial) {
        if (historial.isEmpty()) {
            return new String[]{tipo, "-", "-", "-", "-"};
        }
        int posMax = 0;
        int posMin = 0;
        for (int i = 1; i < historial.size(); i++) {
            if (historial.get(i) > historial.get(posMax)) {
                posMax = i;
            }
            if (historial.get(i) < historial.get(posMin)) {
                posMin = i;
            }
        }
        return new String[]{
            tipo,
            String.valueOf(historial.get(posMax)), "Turno " + (posMax + 1),
            String.valueOf(historial.get(posMin)), "Turno " + (posMin + 1)
        };
    }
}
