package dao;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import modelo.Clima;
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
                // Las líneas que empiezan con '#' son comentarios: solo ordenan el archivo y cargar() las ignora.
                out.write("# SIMULADOR DE ECOSISTEMA - estado guardado (formato ETIQUETA;campo;campo...)");
                out.newLine();

                escribirSeccion(out, "CONFIGURACION", "CONFIG: turnoActual;turnosTotales;clima;ultimoTurnoIntervenido");
                escribirLinea(out, "CONFIG", eco.getTurnoActual(), eco.getTurnosTotales(),
                        eco.getClimaActual().name(), eco.getUltimoTurnoIntervenido());

                escribirSeccion(out, "CONTADORES", "CONTADORES: plantasCreadas;conejosCreados;lobosCreados");
                escribirLinea(out, "CONTADORES", eco.getContadorPlantas(), eco.getContadorConejos(),
                        eco.getLobosCreados());

                escribirSeccion(out, "ESTADISTICAS",
                        "ESTADISTICAS: nacPlantas;nacConejos;muertesPlantas;muertesConejos;muertesLobos");
                escribirLinea(out, "ESTADISTICAS", eco.getNacimientosPlantas(), eco.getNacimientosConejos(),
                        eco.getMuertesPlantas(), eco.getMuertesConejos(), eco.getMuertesLobos());

                // Se guardan TODAS las entidades (también las muertas): el reporte las necesita.
                escribirSeccion(out, "PLANTAS", "PLANTA: nombre;energia;edad;viva;tamanio");
                for (Planta p : eco.getTodasLasPlantas()) {
                    escribirLinea(out, "PLANTA", p.getNombre(), p.getEnergia(), p.getEdad(),
                            p.isViva(), p.getTamanio());
                }
                escribirSeccion(out, "CONEJOS", "CONEJO: nombre;energia;edad;viva;velocidad;peso");
                for (Conejo c : eco.getTodosLosConejos()) {
                    escribirLinea(out, "CONEJO", c.getNombre(), c.getEnergia(), c.getEdad(),
                            c.isViva(), c.getVelocidad(), c.getPeso());
                }
                escribirSeccion(out, "LOBOS", "LOBO: nombre;energia;edad;viva;velocidad;peso;exitosCaza");
                for (Lobo l : eco.getTodosLosLobos()) {
                    escribirLinea(out, "LOBO", l.getNombre(), l.getEnergia(), l.getEdad(),
                            l.isViva(), l.getVelocidad(), l.getPeso(), l.getExitosCaza());
                }

                escribirSeccion(out, "HISTORIAL (un valor por turno jugado)", "ACTIVIDAD = eventos ocurridos en cada turno");
                escribirLista(out, "ACTIVIDAD", eco.getEventosPorTurno());
                escribirLista(out, "HIST_PLANTAS", eco.getHistorialPlantas());
                escribirLista(out, "HIST_CONEJOS", eco.getHistorialConejos());
                escribirLista(out, "HIST_LOBOS", eco.getHistorialLobos());

                escribirSeccion(out, "EVENTOS DEL ULTIMO TURNO", "EVENTO: texto");
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
        Path archivo = Paths.get(RUTA);
        try {
            if (!Files.exists(archivo)) {
                return null;
            }
            List<String> lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
            return reconstruir(lineas);
        } catch (IOException e) {
            return null;
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            // IllegalArgumentException cubre clima inexistente, NumberFormatException
            // (es su subclase) y los rangos inválidos que lanza invalido().
            return null;
        }
    }

    /** true si el archivo existe y no está vacío. */
    public boolean existeGuardado() {
        File archivo = new File(RUTA);
        return archivo.isFile() && archivo.length() > 0;
    }

    /** Borra el archivo (al empezar una simulación nueva). true si quedó borrado o no existía. */
    public boolean borrar() {
        Path archivo = Paths.get(RUTA);
        try {
            Files.deleteIfExists(archivo);
            return true;
        } catch (IOException | SecurityException e) {
            return false;
        }
    }

    // ---- Reconstrucción ----

    /**
     * Arma el Ecosistema a partir de las líneas. Lanza IllegalArgumentException ante
     * cualquier dato inválido; cargar() la atrapa y devuelve null.
     */
    private Ecosistema reconstruir(List<String> lineas) {
        // 1) Primero CONFIG: hace falta para crear el Ecosistema (sin inicializar(), que crearía entidades nuevas).
        String[] cfg = null;
        for (String linea : lineas) {
            if (linea.startsWith("CONFIG" + SEP)) {
                if (cfg != null) {
                    throw invalido("CONFIG repetido");
                }
                cfg = campos(linea, 5);
            }
        }
        if (cfg == null) {
            throw invalido("falta CONFIG");
        }
        int turnosTotales = entero(cfg[2], 10, 50);
        int turnoActual = entero(cfg[1], 0, turnosTotales);
        Clima clima = Clima.valueOf(cfg[3]);
        int ultimoIntervenido = entero(cfg[4], -1, turnoActual);

        Ecosistema eco = new Ecosistema(clima, turnosTotales);
        eco.setTurnoActual(turnoActual);
        eco.setUltimoTurnoIntervenido(ultimoIntervenido);

        // 2) El resto de las líneas.
        int[] contadores = null;
        int[] estadisticas = null;
        List<Integer> actividad = null;
        List<Integer> histPlantas = null;
        List<Integer> histConejos = null;
        List<Integer> histLobos = null;
        List<String> eventos = new ArrayList<>();

        for (String linea : lineas) {
            if (linea.trim().isEmpty() || linea.startsWith("#")) {
                continue; // líneas en blanco y comentarios
            }
            int corte = linea.indexOf(SEP);
            String etiqueta = corte < 0 ? linea : linea.substring(0, corte);

            switch (etiqueta) {
                case "CONFIG":
                    break; // ya procesada
                case "CONTADORES": {
                    sinRepetir(contadores, etiqueta);
                    String[] f = campos(linea, 4);
                    contadores = new int[]{entero(f[1], 0, Integer.MAX_VALUE),
                        entero(f[2], 0, Integer.MAX_VALUE),
                        entero(f[3], 0, Ecosistema.MAX_LOBOS)};
                    break;
                }
                case "ESTADISTICAS": {
                    sinRepetir(estadisticas, etiqueta);
                    String[] f = campos(linea, 6);
                    estadisticas = new int[5];
                    for (int i = 0; i < 5; i++) {
                        estadisticas[i] = entero(f[i + 1], 0, Integer.MAX_VALUE);
                    }
                    break;
                }
                case "PLANTA": {
                    String[] f = campos(linea, 6);
                    eco.restaurarEntidad(new Planta(nombre(f[1]), noNegativo(f[2]),
                            entero(f[3], 0, Integer.MAX_VALUE), viva(f[4]), entero(f[5], 1, 5)));
                    break;
                }
                case "CONEJO": {
                    String[] f = campos(linea, 7);
                    eco.restaurarEntidad(new Conejo(nombre(f[1]), noNegativo(f[2]),
                            entero(f[3], 0, Integer.MAX_VALUE), viva(f[4]),
                            entero(f[5], 0, Integer.MAX_VALUE), noNegativo(f[6])));
                    break;
                }
                case "LOBO": {
                    String[] f = campos(linea, 8);
                    Lobo lobo = new Lobo(nombre(f[1]), noNegativo(f[2]),
                            entero(f[3], 0, Integer.MAX_VALUE), viva(f[4]),
                            entero(f[5], 0, Integer.MAX_VALUE), noNegativo(f[6]));
                    lobo.setExitosCaza(entero(f[7], 0, Integer.MAX_VALUE));
                    eco.restaurarEntidad(lobo);
                    break;
                }
                case "ACTIVIDAD":
                    sinRepetir(actividad, etiqueta);
                    actividad = lista(linea);
                    break;
                case "HIST_PLANTAS":
                    sinRepetir(histPlantas, etiqueta);
                    histPlantas = lista(linea);
                    break;
                case "HIST_CONEJOS":
                    sinRepetir(histConejos, etiqueta);
                    histConejos = lista(linea);
                    break;
                case "HIST_LOBOS":
                    sinRepetir(histLobos, etiqueta);
                    histLobos = lista(linea);
                    break;
                case "EVENTO":
                    // Todo lo que viene después del primer ';' (el texto puede contener ';').
                    eventos.add(linea.split(SEP, 2)[1]);
                    break;
                default:
                    throw invalido("etiqueta desconocida: " + etiqueta);
            }
        }

        // 3) Tienen que estar todos los bloques y las listas deben tener un valor por turno jugado.
        if (contadores == null || estadisticas == null || actividad == null
                || histPlantas == null || histConejos == null || histLobos == null) {
            throw invalido("faltan líneas obligatorias");
        }
        if (actividad.size() != turnoActual || histPlantas.size() != turnoActual
                || histConejos.size() != turnoActual || histLobos.size() != turnoActual) {
            throw invalido("el historial no coincide con el turno actual");
        }

        eco.setContadores(contadores[0], contadores[1], contadores[2]);
        eco.setEstadisticas(estadisticas[0], estadisticas[1], estadisticas[2], estadisticas[3], estadisticas[4]);
        eco.setEventosPorTurno(actividad);
        eco.setHistorial(histPlantas, histConejos, histLobos);
        eco.setEventosTurno(eventos);
        return eco;
    }

    // ---- Auxiliares de lectura y validación ----

    private IllegalArgumentException invalido(String motivo) {
        return new IllegalArgumentException("Archivo inválido: " + motivo);
    }

    /** Divide la línea conservando campos vacíos y exige la cantidad exacta de campos. */
    private String[] campos(String linea, int esperados) {
        String[] f = linea.split(SEP, -1);
        if (f.length != esperados) {
            throw invalido("cantidad de campos incorrecta en " + f[0]);
        }
        return f;
    }

    private void sinRepetir(Object yaLeido, String etiqueta) {
        if (yaLeido != null) {
            throw invalido(etiqueta + " repetida");
        }
    }

    /** Entero dentro de [min, max]; NumberFormatException si no es un número. */
    private int entero(String texto, int min, int max) {
        int valor = Integer.parseInt(texto.trim());
        if (valor < min || valor > max) {
            throw invalido("valor fuera de rango: " + valor);
        }
        return valor;
    }

    /** Decimal finito y mayor o igual a 0 (rechaza NaN e Infinity, que parseDouble sí acepta). */
    private double noNegativo(String texto) {
        double valor = Double.parseDouble(texto.trim());
        if (Double.isNaN(valor) || Double.isInfinite(valor) || valor < 0) {
            throw invalido("decimal inválido: " + texto);
        }
        return valor;
    }

    /** Solo acepta "true" o "false" (Boolean.parseBoolean tomaría cualquier otra cosa como false). */
    private boolean viva(String texto) {
        if ("true".equals(texto)) {
            return true;
        }
        if ("false".equals(texto)) {
            return false;
        }
        throw invalido("booleano inválido: " + texto);
    }

    private String nombre(String texto) {
        if (texto.trim().isEmpty()) {
            throw invalido("nombre vacío");
        }
        return texto;
    }

    /** Lee "ETIQUETA;1;2;3" como lista de enteros no negativos; "ETIQUETA;" es una lista vacía. */
    private List<Integer> lista(String linea) {
        String[] f = linea.split(SEP, -1);
        List<Integer> valores = new ArrayList<>();
        if (f.length == 2 && f[1].isEmpty()) {
            return valores;
        }
        for (int i = 1; i < f.length; i++) {
            valores.add(entero(f[i], 0, Integer.MAX_VALUE));
        }
        return valores;
    }

    // ---- Auxiliares de escritura ----

    /** Escribe una línea en blanco y un encabezado comentado (#) que abre una sección del archivo. */
    private void escribirSeccion(BufferedWriter out, String titulo, String columnas) throws IOException {
        out.newLine();
        out.write("# === " + titulo + " ===");
        out.newLine();
        out.write("# " + columnas);
        out.newLine();
    }

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
