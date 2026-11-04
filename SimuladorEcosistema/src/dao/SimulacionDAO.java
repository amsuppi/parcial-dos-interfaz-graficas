package dao;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import modelo.Conejo;
import modelo.Ecosistema;
import modelo.Lobo;
import modelo.Planta;

/**
 * Guarda y recupera el estado de la simulación en un archivo de texto plano.
 * No usa Swing ni muestra mensajes: informa el resultado con lo que devuelve
 * y el controlador decide qué hacer.
 *
 * Formato: un registro por línea, "ETIQUETA;campo1;campo2;...".
 * Los eventos van al final de la línea EVENTO, así que pueden contener ';'.
 *
 * TODO (Etapa 2): implementar cargar, existeGuardado y borrar.
 */
public class SimulacionDAO {

    private static final String RUTA = "datos/simulacion.txt"; // relativa a la carpeta del proyecto
    private static final String SEP = ";";

    /** Escribe el estado completo. true si pudo guardar, false si hubo error de E/S. */
    public boolean guardar(Ecosistema eco) {
        if (eco == null) {
            return false;
        }
        Path archivo = Paths.get(RUTA);
        try {
            Path carpeta = archivo.getParent();
            if (carpeta != null) {
                Files.createDirectories(carpeta);
            }
            try (BufferedWriter out = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8)) {
                escribirLinea(out, "CONFIG", eco.getTurnoActual(), eco.getTurnosTotales(),
                        eco.getClimaActual().name(), eco.getUltimoTurnoIntervenido());
                escribirLinea(out, "CONTADORES", eco.getContadorPlantas(), eco.getContadorConejos(),
                        eco.getLobosCreados());
                escribirLinea(out, "ESTADISTICAS", eco.getNacimientosPlantas(), eco.getNacimientosConejos(),
                        eco.getMuertesPlantas(), eco.getMuertesConejos(), eco.getMuertesLobos());

                // Se guardan TODAS las entidades (también las muertas): el reporte las necesita.
                for (Planta p : eco.getTodasLasPlantas()) {
                    escribirLinea(out, "PLANTA", p.getNombre(), p.getEnergia(), p.getEdad(),
                            p.isViva(), p.getTamanio());
                }
                for (Conejo c : eco.getTodosLosConejos()) {
                    escribirLinea(out, "CONEJO", c.getNombre(), c.getEnergia(), c.getEdad(),
                            c.isViva(), c.getVelocidad(), c.getPeso());
                }
                for (Lobo l : eco.getTodosLosLobos()) {
                    escribirLinea(out, "LOBO", l.getNombre(), l.getEnergia(), l.getEdad(),
                            l.isViva(), l.getVelocidad(), l.getPeso(), l.getExitosCaza());
                }

                escribirLista(out, "ACTIVIDAD", eco.getEventosPorTurno());
                escribirLista(out, "HIST_PLANTAS", eco.getHistorialPlantas());
                escribirLista(out, "HIST_CONEJOS", eco.getHistorialConejos());
                escribirLista(out, "HIST_LOBOS", eco.getHistorialLobos());

                for (String evento : eco.getEventosTurno()) {
                    // Una línea por evento: se quitan saltos de línea por si el texto trajera alguno.
                    out.write("EVENTO" + SEP + evento.replace("\r", " ").replace("\n", " "));
                    out.newLine();
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /** Lee el archivo y reconstruye el Ecosistema. null si no existe, está vacío o tiene datos inválidos. */
    public Ecosistema cargar() {
        // TODO Etapa 2
        return null;
    }

    /** true si el archivo existe y no está vacío. */
    public boolean existeGuardado() {
        // TODO Etapa 2
        return false;
    }

    /** Borra el archivo (al empezar una simulación nueva). true si quedó borrado o no existía. */
    public boolean borrar() {
        // TODO Etapa 2
        return true;
    }

    // ---- Auxiliares de escritura ----

    /**
     * Escribe "ETIQUETA;v1;v2;...". String.valueOf deja los double con punto decimal
     * sin depender del idioma de la PC (String.format sí dependería).
     */
    private void escribirLinea(BufferedWriter out, String etiqueta, Object... valores) throws IOException {
        StringBuilder linea = new StringBuilder(etiqueta);
        for (Object valor : valores) {
            linea.append(SEP).append(String.valueOf(valor));
        }
        out.write(linea.toString());
        out.newLine();
    }

    /** Escribe una lista de enteros; si está vacía queda solo "ETIQUETA;". */
    private void escribirLista(BufferedWriter out, String etiqueta, List<Integer> valores) throws IOException {
        StringBuilder linea = new StringBuilder(etiqueta).append(SEP);
        for (int i = 0; i < valores.size(); i++) {
            if (i > 0) {
                linea.append(SEP);
            }
            linea.append(valores.get(i));
        }
        out.write(linea.toString());
        out.newLine();
    }
}
