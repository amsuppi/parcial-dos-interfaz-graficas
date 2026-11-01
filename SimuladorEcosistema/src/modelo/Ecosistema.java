package modelo;

import java.util.ArrayList;
import java.util.Random;

 //Contiene todas las entidades y coordina lo que pasa en cada turno.

public class Ecosistema {

    public static final int MAX_LOBOS = 5; // límite de lobos en TODA la simulación

    private static final String[] NOMBRES_PLANTAS = {"Helecho", "Trebol", "Musgo", "Ortiga", "Diente de leon"};
    private static final String[] NOMBRES_CONEJOS = {"Blas", "Luna", "Topo", "Rex", "Nube", "Copo", "Pipo", "Mora"};
    private static final String[] NOMBRES_LOBOS = {"Fang", "Sombra", "Colmillo", "Aullido", "Niebla"};

    private static final double ENERGIA_CRIA_PLANTA = 40;
    private static final double ENERGIA_CRIA_CONEJO = 40;

    // ---- Atributos que pide la consigna ----
    private ArrayList<Planta> plantas;
    private ArrayList<Conejo> conejos;
    private ArrayList<Lobo> lobos;
    private Clima climaActual;
    private int turnoActual;

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

    private int eventosTurnoActual = 0;
    private ArrayList<Integer> eventosPorTurno = new ArrayList<>(); // posición 0 = turno 1

    // ---- Bonus: estadísticas en tiempo real (conteo de cada población por turno) ----
    private ArrayList<Integer> historialPlantas = new ArrayList<>();
    private ArrayList<Integer> historialConejos = new ArrayList<>();
    private ArrayList<Integer> historialLobos = new ArrayList<>();

    private Random random = new Random();

    public Ecosistema(Clima climaInicial) {
        this.plantas = new ArrayList<>();
        this.conejos = new ArrayList<>();
        this.lobos = new ArrayList<>();
        this.climaActual = climaInicial;
        this.turnoActual = 0;
    }

    /** Crea las entidades iniciales con energía aleatoria (la llama el Main después de la configuración). */
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

    /** Ejecuta un turno completo en el orden que pide la consigna. */
    public void procesarTurno() {
        turnoActual++;
        eventosTurnoActual = 0;

        System.out.println();
        System.out.println("=== TURNO " + turnoActual + " | Clima: " + climaActual + " ===");
        System.out.println("Plantas: " + plantas.size() + "  Conejos: " + conejos.size()
                + "  Lobos: " + lobos.size());
        System.out.println("-- Eventos --");

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
        for (Conejo c : conejos) {
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

        if (eventosTurnoActual == 0) {
            System.out.println("(sin eventos este turno)");
        }

        // Guardar estadísticas de este turno.
        eventosPorTurno.add(eventosTurnoActual);
        historialPlantas.add(plantas.size());
        historialConejos.add(conejos.size());
        historialLobos.add(lobos.size());

        mostrarEstado();
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
            System.out.println("[Clima " + climaActual + "] conejos " + conSigno(efectoConejos)
                    + " energia, lobos " + conSigno(efectoLobos) + " energia");
        }
    }

    private void verificarMuertes() {
        // Animales: usan el método default verificarMuerte() de la interfaz Mortal.
        ArrayList<Mortal> mortales = new ArrayList<>();
        mortales.addAll(conejos);
        mortales.addAll(lobos);
        for (Mortal m : mortales) {
            boolean estabaVivo = m.estaVivo();
            m.verificarMuerte();              // si murió, el mensaje lo imprime la interfaz
            if (estabaVivo && !m.estaVivo()) {
                eventosTurnoActual++;         // igual lo contamos como evento del turno
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
        System.out.println(evento);
        eventosTurnoActual++;
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
        registrarEvento("Planta '" + madre.getNombre() + "' se reprodujo -> nueva planta '"
                + cria.getNombre() + "' (energia: " + (int) ENERGIA_CRIA_PLANTA + ")");
    }

    public void nacerConejo(Conejo madre) {
        Conejo cria = new Conejo(siguienteNombre(NOMBRES_CONEJOS, contadorConejos++),
                ENERGIA_CRIA_CONEJO, 0, true, madre.getVelocidad(), 1.0);
        conejos.add(cria);
        todosLosConejos.add(cria);
        nacimientosConejos++;
        registrarEvento("Conejo '" + madre.getNombre() + "' tuvo una cría -> nuevo conejo '"
                + cria.getNombre() + "' (energia: " + (int) ENERGIA_CRIA_CONEJO + ")");
    }


    //  INTERVENCIÓN DEL JUGADOR

    /** Versión 1 (sobrecarga): sin energía, se genera una aleatoria. */
    public boolean agregarEntidad(String tipo) {
        return agregarEntidad(tipo, energiaAleatoria(tipo));
    }

    /** Versión 2 (sobrecarga): con energía inicial indicada. */
    public boolean agregarEntidad(String tipo, double energiaInicial) {
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
                    System.out.println("No se pueden agregar más lobos: ya hubo "
                            + MAX_LOBOS + " en total en la simulación.");
                    return false;
                }
                nueva = crearLobo(energiaInicial);
                break;
            default:
                System.out.println("Tipo de entidad inválido: " + tipo);
                return false;
        }
        System.out.println("Se agregó '" + nueva.getNombre() + "' al ecosistema.");
        return true;
    }

    public boolean puedeAgregarLobo() {
        return lobosCreados < MAX_LOBOS;
    }

    public void cambiarClima(Clima nuevo) {
        System.out.println("El clima cambió: " + climaActual + " -> " + nuevo);
        this.climaActual = nuevo;
    }

    //  ESTADO Y FIN

    public void mostrarEstado() {
        System.out.println("Estado: Plantas: " + plantas.size() + "  Conejos: " + conejos.size()
                + "  Lobos: " + lobos.size() + "  | Clima: " + climaActual);
    }

    public boolean ecosistemaColapsado() {
        return plantas.isEmpty() || conejos.isEmpty() || lobos.isEmpty();
    }

    public void generarReporteFinal() {
        System.out.println();
        System.out.println("========== REPORTE FINAL ==========");
        System.out.println("Turnos jugados: " + turnoActual + " | Clima final: " + climaActual);

        // Causa de fin
        if (ecosistemaColapsado()) {
            String extintas = "";
            if (plantas.isEmpty()) extintas += "plantas ";
            if (conejos.isEmpty()) extintas += "conejos ";
            if (lobos.isEmpty()) extintas += "lobos ";
            System.out.println("Causa de fin: COLAPSO. Se extinguieron: " + extintas.trim());
        } else {
            System.out.println("Causa de fin: se completaron los " + turnoActual + " turnos configurados.");
        }

        // Turno de mayor actividad
        int turnoMax = 0;
        for (int i = 1; i < eventosPorTurno.size(); i++) {
            if (eventosPorTurno.get(i) > eventosPorTurno.get(turnoMax)) {
                turnoMax = i;
            }
        }
        if (!eventosPorTurno.isEmpty()) {
            System.out.println("Turno de mayor actividad: turno " + (turnoMax + 1)
                    + " (" + eventosPorTurno.get(turnoMax) + " eventos)");
        }

        // Entidades más longevas (se busca entre todas las que existieron)
        System.out.println("-- Entidades más longevas --");
        mostrarMasLongeva("Planta", todasLasPlantas);
        mostrarMasLongeva("Conejo", todosLosConejos);
        mostrarMasLongeva("Lobo", todosLosLobos);

        // Lobo con más cacerías
        Lobo mejorCazador = null;
        for (Lobo l : todosLosLobos) {
            if (mejorCazador == null || l.getExitosCaza() > mejorCazador.getExitosCaza()) {
                mejorCazador = l;
            }
        }
        if (mejorCazador != null) {
            System.out.println("Lobo con más cacerías: '" + mejorCazador.getNombre() + "' ("
                    + mejorCazador.getExitosCaza() + " cacerías exitosas)");
        }

        // Nacimientos y muertes
        System.out.println("-- Nacimientos / Muertes --");
        System.out.println("Plantas: " + nacimientosPlantas + " nacimientos / " + muertesPlantas + " muertes");
        System.out.println("Conejos: " + nacimientosConejos + " nacimientos / " + muertesConejos + " muertes");
        System.out.println("Lobos:   0 nacimientos (no se reproducen) / " + muertesLobos + " muertes");

        // Bonus: máximos y mínimos de cada población
        System.out.println("-- Máximos y mínimos por población --");
        mostrarMaxMin("Plantas", historialPlantas);
        mostrarMaxMin("Conejos", historialConejos);
        mostrarMaxMin("Lobos", historialLobos);
        System.out.println("===================================");
    }

    private void mostrarMasLongeva(String tipo, ArrayList<? extends Entidad> lista) {
        Entidad mayor = null;
        for (Entidad e : lista) {
            if (mayor == null || e.getEdad() > mayor.getEdad()) {
                mayor = e;
            }
        }
        if (mayor != null) {
            System.out.println(tipo + ": '" + mayor.getNombre() + "' con " + mayor.getEdad()
                    + " turnos de edad" + (mayor.isViva() ? " (sigue viva)" : " (murió)"));
        }
    }

    private void mostrarMaxMin(String tipo, ArrayList<Integer> historial) {
        if (historial.isEmpty()) {
            return;
        }
        int iMax = 0;
        int iMin = 0;
        for (int i = 1; i < historial.size(); i++) {
            if (historial.get(i) > historial.get(iMax)) iMax = i;
            if (historial.get(i) < historial.get(iMin)) iMin = i;
        }
        System.out.println(tipo + ": máximo " + historial.get(iMax) + " en turno " + (iMax + 1)
                + " | mínimo " + historial.get(iMin) + " en turno " + (iMin + 1));
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

    //  GETTERS

    public ArrayList<Planta> getPlantas() {
        return plantas;
    }

    public ArrayList<Conejo> getConejos() {
        return conejos;
    }

    public ArrayList<Lobo> getLobos() {
        return lobos;
    }

    public Clima getClimaActual() {
        return climaActual;
    }

    public int getTurnoActual() {
        return turnoActual;
    }

    public int getLobosCreados() {
        return lobosCreados;
    }
}
