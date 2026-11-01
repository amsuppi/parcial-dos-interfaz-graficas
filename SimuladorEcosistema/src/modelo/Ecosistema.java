package modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Contiene todas las entidades y aplica las reglas de cada turno.
 * No interactúa con el usuario: los mensajes quedan guardados como eventos
 * para que el controlador se los pase a la vista.
 */
public class Ecosistema {

    public static final int MAX_LOBOS = 5; // límite de lobos en TODA la simulación
    public static final int TURNOS_ENTRE_INTERVENCIONES = 3;

    private static final String[] NOMBRES_PLANTAS = {"Helecho", "Trebol", "Musgo", "Ortiga", "Diente de leon"};
    private static final String[] NOMBRES_CONEJOS = {"Blas", "Luna", "Topo", "Rex", "Nube", "Copo", "Pipo", "Mora"};
    private static final String[] NOMBRES_LOBOS = {"Fang", "Sombra", "Colmillo", "Aullido", "Niebla"};

    private static final double ENERGIA_CRIA_PLANTA = 40;
    private static final double ENERGIA_CRIA_CONEJO = 40;

    // ---- Estado actual ----
    private ArrayList<Planta> plantas = new ArrayList<>();
    private ArrayList<Conejo> conejos = new ArrayList<>();
    private ArrayList<Lobo> lobos = new ArrayList<>();
    private Clima climaActual;
    private int turnoActual;
    private int turnosTotales;
    private int ultimoTurnoIntervenido = -1; // para permitir una sola intervención cada 3 turnos

    // ---- Historial completo (vivas y muertas) para el reporte final ----
    private ArrayList<Planta> todasLasPlantas = new ArrayList<>();
    private ArrayList<Conejo> todosLosConejos = new ArrayList<>();
    private ArrayList<Lobo> todosLosLobos = new ArrayList<>();

    // ---- Contadores ----
    private int contadorPlantas = 0;   // sirve para ponerle número a los nombres
    private int contadorConejos = 0;
    private int lobosCreados = 0;      // para validar el máximo de 5 lobos
    private int nacimientosPlantas = 0;
    private int nacimientosConejos = 0;
    private int muertesPlantas = 0;
    private int muertesConejos = 0;
    private int muertesLobos = 0;

    // ---- Eventos ----
    private ArrayList<String> eventosTurno = new ArrayList<>();       // eventos del último turno
    private ArrayList<Integer> eventosPorTurno = new ArrayList<>();   // posición 0 = turno 1

    // ---- Estadísticas de población (cantidad viva al final de cada turno) ----
    private ArrayList<Integer> historialPlantas = new ArrayList<>();
    private ArrayList<Integer> historialConejos = new ArrayList<>();
    private ArrayList<Integer> historialLobos = new ArrayList<>();

    private Random random = new Random();

    public Ecosistema(Clima climaInicial, int turnosTotales) {
        this.climaActual = climaInicial;
        this.turnosTotales = turnosTotales;
        this.turnoActual = 0;
    }

    /** Crea las entidades iniciales con energía aleatoria. No se usa al cargar desde archivo. */
    public void inicializar(int cantPlantas, int cantConejos, int cantLobos) {
        for (int i = 0; i < cantPlantas; i++) {
            crearPlanta(energiaAleatoria("planta"));
        }
        for (int i = 0; i < cantConejos; i++) {
            crearConejo(energiaAleatoria("conejo"));
        }
        for (int i = 0; i < cantLobos; i++) {
            crearLobo(energiaAleatoria("lobo"));
        }
    }

    //  TURNO

    /** Ejecuta un turno completo en el orden de la consigna y devuelve los eventos del turno. */
    public List<String> procesarTurno() {
        turnoActual++;
        eventosTurno.clear();

        // 1) REPRODUCCIÓN. Polimorfismo: plantas y conejos juntos en una
        //    lista de Reproducible, recorridos en un solo for. Cada uno
        //    ejecuta SU versión de reproducirse().
        ArrayList<Reproducible> reproducibles = new ArrayList<>();
        reproducibles.addAll(plantas);
        reproducibles.addAll(conejos);
        for (Reproducible r : reproducibles) {
            r.intentarReproduccion(this);
        }

        // 2) Los conejos comen.
        for (Conejo c : new ArrayList<>(conejos)) {
            if (c.isViva()) {
                c.comer(this);
            }
        }

        // 3) Los lobos intentan cazar.
        for (Lobo l : lobos) {
            if (l.isViva()) {
                l.actuar(this);
            }
        }

        // 4) Efecto del clima y envejecimiento.
        aplicarEfectoClima();
        ArrayList<Entidad> todas = new ArrayList<>();
        todas.addAll(plantas);
        todas.addAll(conejos);
        todas.addAll(lobos);
        for (Entidad e : todas) {
            if (e.isViva()) {
                e.envejecer(); // Planta usa su propia versión (gasta menos energía)
            }
        }

        // 5) Muertes por falta de energía.
        verificarMuertes();

        // 6) Sacar a los muertos de las listas y contarlos.
        muertesPlantas += eliminarMuertos(plantas);
        muertesConejos += eliminarMuertos(conejos);
        muertesLobos += eliminarMuertos(lobos);

        // Guardar estadísticas de este turno (antes de agregar el mensaje de "sin eventos").
        eventosPorTurno.add(eventosTurno.size());
        historialPlantas.add(plantas.size());
        historialConejos.add(conejos.size());
        historialLobos.add(lobos.size());

        if (eventosTurno.isEmpty()) {
            eventosTurno.add("Sin eventos este turno");
        }
        return getEventosTurno();
    }

    private void aplicarEfectoClima() {
        int efectoConejos = climaActual.getEfectoConejos();
        int efectoLobos = climaActual.getEfectoLobos();

        for (Conejo c : conejos) {
            if (c.isViva()) {
                c.setEnergia(c.getEnergia() + efectoConejos);
            }
        }
        for (Lobo l : lobos) {
            if (l.isViva()) {
                l.setEnergia(l.getEnergia() + efectoLobos);
            }
        }

        if (efectoConejos != 0 || efectoLobos != 0) {
            registrarEvento("Clima " + climaActual + ": conejos " + conSigno(efectoConejos)
                    + " energía, lobos " + conSigno(efectoLobos) + " energía");
        }
    }

    private void verificarMuertes() {
        // Animales: usan el método default verificarMuerte() de la interfaz Mortal.
        ArrayList<Mortal> mortales = new ArrayList<>();
        mortales.addAll(conejos);
        mortales.addAll(lobos);
        for (Mortal m : mortales) {
            if (m.verificarMuerte()) {
                Entidad e = (Entidad) m;
                registrarEvento(e.getTipo() + " '" + e.getNombre() + "' murió de inanición");
            }
        }

        // Plantas: no implementan Mortal, se revisan acá.
        for (Planta p : plantas) {
            if (p.isViva() && p.getEnergia() <= 0) {
                p.setViva(false);
                registrarEvento("Planta '" + p.getNombre() + "' se marchitó");
            }
        }
    }

    /** Recorre la lista de atrás para adelante para poder borrar sin saltearse elementos. */
    private int eliminarMuertos(ArrayList<? extends Entidad> lista) {
        int muertos = 0;
        for (int i = lista.size() - 1; i >= 0; i--) {
            if (!lista.get(i).isViva()) {
                lista.remove(i);
                muertos++;
            }
        }
        return muertos;
    }

    //  MÉTODOS QUE USAN Planta, Conejo y Lobo

    public void registrarEvento(String evento) {
        eventosTurno.add(evento);
    }

    public Planta buscarPlantaViva() {
        ArrayList<Planta> vivas = new ArrayList<>();
        for (Planta p : plantas) {
            if (p.isViva()) {
                vivas.add(p);
            }
        }
        if (vivas.isEmpty()) {
            return null;
        }
        return vivas.get(random.nextInt(vivas.size()));
    }

    public Conejo buscarConejoVivo() {
        ArrayList<Conejo> vivos = new ArrayList<>();
        for (Conejo c : conejos) {
            if (c.isViva()) {
                vivos.add(c);
            }
        }
        if (vivos.isEmpty()) {
            return null;
        }
        return vivos.get(random.nextInt(vivos.size()));
    }

    public int contarConejosVivos() {
        int vivos = 0;
        for (Conejo c : conejos) {
            if (c.isViva()) {
                vivos++;
            }
        }
        return vivos;
    }

    public void nacerPlanta(Planta madre) {
        // La cría conserva el nombre de la especie: 'Helecho-3' -> 'Helecho-9'
        String especie = madre.getNombre();
        int guion = especie.lastIndexOf('-');
        if (guion > 0) {
            especie = especie.substring(0, guion);
        }
        Planta cria = new Planta(especie + "-" + (++contadorPlantas),
                ENERGIA_CRIA_PLANTA, 0, true, 1 + random.nextInt(5));
        plantas.add(cria);
        todasLasPlantas.add(cria);
        nacimientosPlantas++;
        registrarEvento("Planta '" + madre.getNombre() + "' se reprodujo: nació '"
                + cria.getNombre() + "'");
    }

    public void nacerConejo(Conejo madre) {
        Conejo cria = new Conejo(siguienteNombre(NOMBRES_CONEJOS, contadorConejos++),
                ENERGIA_CRIA_CONEJO, 0, true, madre.getVelocidad(), 1.0);
        conejos.add(cria);
        todosLosConejos.add(cria);
        nacimientosConejos++;
        registrarEvento("Conejo '" + madre.getNombre() + "' tuvo una cría: nació '"
                + cria.getNombre() + "'");
    }

    //  INTERVENCIÓN DEL JUGADOR

    /** true si el jugador puede intervenir ahora: cada 3 turnos, una sola vez, y si la simulación sigue. */
    public boolean puedeIntervenir() {
        return turnoActual > 0
                && turnoActual % TURNOS_ENTRE_INTERVENCIONES == 0
                && ultimoTurnoIntervenido != turnoActual
                && !simulacionTerminada();
    }

    /** Versión 1 (sobrecarga): sin energía, se genera una aleatoria. */
    public boolean agregarEntidad(String tipo) {
        return agregarEntidad(tipo, energiaAleatoria(tipo));
    }

    /** Versión 2 (sobrecarga): con energía inicial indicada. Devuelve false si no se pudo agregar. */
    public boolean agregarEntidad(String tipo, double energiaInicial) {
        if (tipo == null || !puedeIntervenir()) {
            return false;
        }
        Entidad nueva;
        switch (tipo.trim().toLowerCase()) {
            case "planta":
                nueva = crearPlanta(energiaInicial);
                break;
            case "conejo":
                nueva = crearConejo(energiaInicial);
                break;
            case "lobo":
                if (!puedeAgregarLobo()) {
                    return false;
                }
                nueva = crearLobo(energiaInicial);
                break;
            default:
                return false;
        }
        ultimoTurnoIntervenido = turnoActual;
        registrarEvento("Intervención: se agregó " + nueva.getTipo() + " '" + nueva.getNombre() + "'");
        return true;
    }

    public boolean puedeAgregarLobo() {
        return lobosCreados < MAX_LOBOS;
    }

    /** Cambia el clima como intervención. Devuelve false si no se puede intervenir o es el mismo clima. */
    public boolean cambiarClima(Clima nuevo) {
        if (nuevo == null || nuevo == climaActual || !puedeIntervenir()) {
            return false;
        }
        registrarEvento("Intervención: el clima cambió de " + climaActual + " a " + nuevo);
        this.climaActual = nuevo;
        ultimoTurnoIntervenido = turnoActual;
        return true;
    }

    //  FIN

    public boolean ecosistemaColapsado() {
        return plantas.isEmpty() || conejos.isEmpty() || lobos.isEmpty();
    }

    /** La simulación termina por turnos completos o por colapso. */
    public boolean simulacionTerminada() {
        return turnoActual >= turnosTotales || ecosistemaColapsado();
    }

    //  AUXILIARES PRIVADOS

    private Planta crearPlanta(double energia) {
        String especie = NOMBRES_PLANTAS[random.nextInt(NOMBRES_PLANTAS.length)];
        Planta p = new Planta(especie + "-" + (++contadorPlantas), energia, 0, true, 1 + random.nextInt(5));
        plantas.add(p);
        todasLasPlantas.add(p);
        return p;
    }

    private Conejo crearConejo(double energia) {
        Conejo c = new Conejo(siguienteNombre(NOMBRES_CONEJOS, contadorConejos++),
                energia, 0, true, 3 + random.nextInt(6), 1 + random.nextDouble() * 2);
        conejos.add(c);
        todosLosConejos.add(c);
        return c;
    }

    private Lobo crearLobo(double energia) {
        Lobo l = new Lobo(siguienteNombre(NOMBRES_LOBOS, lobosCreados++),
                energia, 0, true, 5 + random.nextInt(6), 30 + random.nextDouble() * 20);
        lobos.add(l);
        todosLosLobos.add(l);
        return l;
    }

    /** Usa los nombres de la lista y, cuando se acaban, les agrega un número: Blas, Luna, ..., Blas-2 */
    private String siguienteNombre(String[] nombres, int indice) {
        String nombre = nombres[indice % nombres.length];
        int vuelta = indice / nombres.length;
        return vuelta == 0 ? nombre : nombre + "-" + (vuelta + 1);
    }

    /** Energía inicial aleatoria dentro de un rango razonable para cada tipo. */
    private double energiaAleatoria(String tipo) {
        if (tipo == null) {
            return 50;
        }
        switch (tipo.trim().toLowerCase()) {
            case "planta": return 30 + random.nextInt(31); // 30 a 60
            case "conejo": return 40 + random.nextInt(41); // 40 a 80
            case "lobo":   return 50 + random.nextInt(41); // 50 a 90
            default:       return 50;
        }
    }

    private String conSigno(int valor) {
        return valor > 0 ? "+" + valor : String.valueOf(valor);
    }

    //  GETTERS DE ESTADO

    public ArrayList<Planta> getPlantas() {
        return plantas;
    }

    public ArrayList<Conejo> getConejos() {
        return conejos;
    }

    public ArrayList<Lobo> getLobos() {
        return lobos;
    }

    /** Todas las entidades vivas juntas (para la tabla). */
    public ArrayList<Entidad> getEntidadesVivas() {
        ArrayList<Entidad> vivas = new ArrayList<>();
        vivas.addAll(plantas);
        vivas.addAll(conejos);
        vivas.addAll(lobos);
        return vivas;
    }

    public ArrayList<Planta> getTodasLasPlantas() {
        return todasLasPlantas;
    }

    public ArrayList<Conejo> getTodosLosConejos() {
        return todosLosConejos;
    }

    public ArrayList<Lobo> getTodosLosLobos() {
        return todosLosLobos;
    }

    public Clima getClimaActual() {
        return climaActual;
    }

    public int getTurnoActual() {
        return turnoActual;
    }

    public int getTurnosTotales() {
        return turnosTotales;
    }

    public int getUltimoTurnoIntervenido() {
        return ultimoTurnoIntervenido;
    }

    public int getLobosCreados() {
        return lobosCreados;
    }

    public int getContadorPlantas() {
        return contadorPlantas;
    }

    public int getContadorConejos() {
        return contadorConejos;
    }

    public int getNacimientosPlantas() {
        return nacimientosPlantas;
    }

    public int getNacimientosConejos() {
        return nacimientosConejos;
    }

    public int getMuertesPlantas() {
        return muertesPlantas;
    }

    public int getMuertesConejos() {
        return muertesConejos;
    }

    public int getMuertesLobos() {
        return muertesLobos;
    }

    public List<Integer> getEventosPorTurno() {
        return eventosPorTurno;
    }

    public List<Integer> getHistorialPlantas() {
        return historialPlantas;
    }

    public List<Integer> getHistorialConejos() {
        return historialConejos;
    }

    public List<Integer> getHistorialLobos() {
        return historialLobos;
    }

    /** Copia de los eventos del último turno (más las intervenciones posteriores). */
    public List<String> getEventosTurno() {
        return new ArrayList<>(eventosTurno);
    }

    //  RESTAURACIÓN (la usa el DAO al cargar desde el archivo)

    /** Agrega una entidad leída del archivo al historial y, si está viva, a la lista de vivas. */
    public void restaurarEntidad(Entidad e) {
        if (e instanceof Planta) {
            todasLasPlantas.add((Planta) e);
            if (e.isViva()) {
                plantas.add((Planta) e);
            }
        } else if (e instanceof Conejo) {
            todosLosConejos.add((Conejo) e);
            if (e.isViva()) {
                conejos.add((Conejo) e);
            }
        } else if (e instanceof Lobo) {
            todosLosLobos.add((Lobo) e);
            if (e.isViva()) {
                lobos.add((Lobo) e);
            }
        }
    }

    public void setTurnoActual(int turno) {
        this.turnoActual = Math.max(0, turno);
    }

    public void setUltimoTurnoIntervenido(int turno) {
        this.ultimoTurnoIntervenido = turno;
    }

    public void setContadores(int contadorPlantas, int contadorConejos, int lobosCreados) {
        this.contadorPlantas = contadorPlantas;
        this.contadorConejos = contadorConejos;
        this.lobosCreados = lobosCreados;
    }

    public void setEstadisticas(int nacimientosPlantas, int nacimientosConejos,
                                int muertesPlantas, int muertesConejos, int muertesLobos) {
        this.nacimientosPlantas = nacimientosPlantas;
        this.nacimientosConejos = nacimientosConejos;
        this.muertesPlantas = muertesPlantas;
        this.muertesConejos = muertesConejos;
        this.muertesLobos = muertesLobos;
    }

    public void setEventosPorTurno(List<Integer> eventos) {
        this.eventosPorTurno = new ArrayList<>(eventos);
    }

    public void setHistorial(List<Integer> plantas, List<Integer> conejos, List<Integer> lobos) {
        this.historialPlantas = new ArrayList<>(plantas);
        this.historialConejos = new ArrayList<>(conejos);
        this.historialLobos = new ArrayList<>(lobos);
    }

    public void setEventosTurno(List<String> eventos) {
        this.eventosTurno = new ArrayList<>(eventos);
    }
}
