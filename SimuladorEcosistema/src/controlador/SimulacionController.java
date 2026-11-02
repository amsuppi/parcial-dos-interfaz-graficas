package controlador;

import dao.SimulacionDAO;
import java.util.ArrayList;
import java.util.List;
import modelo.Clima;
import modelo.Ecosistema;
import modelo.Entidad;
import modelo.Reporte;
import vista.VentanaPrincipal;

/**
 * Recibe las acciones de la vista, valida los datos y coordina al modelo y al DAO.
 * No tiene código Swing: solo le pide a la vista que muestre cosas.
 */
public class SimulacionController {

    // Rangos de la configuración inicial (los mismos de la IE1)
    private static final int MIN_PLANTAS = 5, MAX_PLANTAS = 30;
    private static final int MIN_CONEJOS = 2, MAX_CONEJOS = 15;
    private static final int MIN_LOBOS = 1, MAX_LOBOS = Ecosistema.MAX_LOBOS;
    private static final int MIN_TURNOS = 10, MAX_TURNOS = 50;

    private final VentanaPrincipal vista;
    private final SimulacionDAO dao;
    private Ecosistema eco; // null mientras no haya una simulación

    public SimulacionController(VentanaPrincipal vista, SimulacionDAO dao) {
        this.vista = vista;
        this.dao = dao;
    }

    //  INICIO DE LA APLICACIÓN

    /** Al abrir la app: si hay una simulación guardada la retoma; si no, muestra la configuración. */
    public void iniciarAplicacion() {
        if (dao.existeGuardado() && retomarGuardada()) {
            return;
        }
        vista.getPanelSimulacion().mostrarSinSimulacion();
        vista.mostrarPantallaConfiguracion();
        vista.mostrarEstadoApp("Configurá el ecosistema para empezar.");
    }

    /** Botón "Cargar guardada" de la pantalla de configuración. */
    public void cargarSimulacionGuardada() {
        if (!dao.existeGuardado()) {
            vista.mostrarError("No hay ninguna simulación guardada.");
            return;
        }
        retomarGuardada();
    }

    /** Lee el archivo con el DAO y muestra la simulación. Devuelve false si el archivo estaba dañado. */
    private boolean retomarGuardada() {
        Ecosistema cargado = dao.cargar();
        if (cargado == null) {
            dao.borrar();
            vista.mostrarError("El archivo guardado estaba dañado y no se pudo cargar. Empezá una simulación nueva.");
            return false;
        }
        eco = cargado;
        actualizarVistaSimulacion();
        vista.mostrarPantallaSimulacion();
        if (eco.simulacionTerminada()) {
            vista.mostrarEstadoApp("Se cargó una simulación ya terminada. Podés ver el reporte final.");
        } else {
            vista.mostrarEstadoApp("Se retomó la simulación guardada en el turno " + eco.getTurnoActual() + ".");
        }
        return true;
    }

    //  CONFIGURACIÓN

    /** Valida los datos del formulario. Si todo está bien crea el ecosistema y pasa a la simulación. */
    public void iniciarSimulacion(String plantas, String conejos, String lobos, String clima, String turnos) {
        vista.getPanelConfiguracion().limpiarErrores();

        Integer cantPlantas = validarEntero("plantas", plantas, MIN_PLANTAS, MAX_PLANTAS);
        Integer cantConejos = validarEntero("conejos", conejos, MIN_CONEJOS, MAX_CONEJOS);
        Integer cantLobos = validarEntero("lobos", lobos, MIN_LOBOS, MAX_LOBOS);
        Integer cantTurnos = validarEntero("turnos", turnos, MIN_TURNOS, MAX_TURNOS);
        Clima climaInicial = buscarClima(clima);
        if (climaInicial == null) {
            vista.getPanelConfiguracion().mostrarErrorCampo("clima", "Elegí un clima.");
        }

        if (cantPlantas == null || cantConejos == null || cantLobos == null
                || cantTurnos == null || climaInicial == null) {
            vista.mostrarEstadoApp("Hay datos inválidos. Corregí los campos marcados.");
            return;
        }

        eco = new Ecosistema(climaInicial, cantTurnos);
        eco.inicializar(cantPlantas, cantConejos, cantLobos);
        guardar();
        actualizarVistaSimulacion();
        vista.mostrarPantallaSimulacion();
        vista.mostrarEstadoApp("Simulación creada. Presioná \"Avanzar turno\" para empezar.");
    }

    /** Devuelve el número si es válido; si no, le pide a la vista que marque el error y devuelve null. */
    private Integer validarEntero(String campo, String texto, int min, int max) {
        if (texto == null || texto.trim().isEmpty()) {
            vista.getPanelConfiguracion().mostrarErrorCampo(campo, "Este campo es obligatorio.");
            return null;
        }
        int valor;
        try {
            valor = Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            vista.getPanelConfiguracion().mostrarErrorCampo(campo, "Ingresá un número entero.");
            return null;
        }
        if (valor < min || valor > max) {
            vista.getPanelConfiguracion().mostrarErrorCampo(campo, "Debe estar entre " + min + " y " + max + ".");
            return null;
        }
        return valor;
    }

    /** Busca el clima por su nombre ("Soleado", "Lluvioso", ...). null si no existe. */
    private Clima buscarClima(String nombre) {
        if (nombre == null) {
            return null;
        }
        for (Clima c : Clima.values()) {
            if (c.getNombre().equalsIgnoreCase(nombre.trim())) {
                return c;
            }
        }
        return null;
    }

    //  SIMULACIÓN

    public void avanzarTurno() {
        if (eco == null) {
            vista.mostrarError("Todavía no hay una simulación en curso. Configurala primero.");
            return;
        }
        if (eco.simulacionTerminada()) {
            verReporte();
            return;
        }

        eco.procesarTurno();
        guardar();
        actualizarVistaSimulacion();

        if (eco.simulacionTerminada()) {
            vista.mostrarEstadoApp("La simulación terminó.");
            verReporte();
        } else if (eco.puedeIntervenir()) {
            vista.mostrarEstadoApp("Turno " + eco.getTurnoActual() + ": podés intervenir o seguir avanzando.");
        } else {
            vista.mostrarEstadoApp("Turno " + eco.getTurnoActual() + " procesado.");
        }
    }

    //  INTERVENCIÓN

    public void cambiarClima(String clima) {
        if (!validarIntervencion()) {
            return;
        }
        Clima nuevo = buscarClima(clima);
        if (nuevo == null) {
            vista.mostrarError("Elegí un clima válido.");
            return;
        }
        if (nuevo == eco.getClimaActual()) {
            vista.mostrarError("El clima ya es " + nuevo + ". Elegí otro.");
            return;
        }
        eco.cambiarClima(nuevo);
        guardar();
        actualizarVistaSimulacion();
        vista.mostrarEstadoApp("Clima cambiado a " + nuevo + ".");
    }

    public void agregarEntidad(String tipo) {
        if (!validarIntervencion()) {
            return;
        }
        if (tipo == null || tipo.trim().isEmpty()) {
            vista.mostrarError("Elegí qué entidad agregar.");
            return;
        }
        if (tipo.trim().equalsIgnoreCase("lobo") && !eco.puedeAgregarLobo()) {
            vista.mostrarError("Ya hubo " + Ecosistema.MAX_LOBOS + " lobos en la simulación. No se pueden agregar más.");
            return;
        }
        if (!eco.agregarEntidad(tipo)) {
            vista.mostrarError("No se pudo agregar la entidad.");
            return;
        }
        guardar();
        actualizarVistaSimulacion();
        vista.mostrarEstadoApp("Se agregó un/a " + tipo.trim().toLowerCase() + " al ecosistema.");
    }

    private boolean validarIntervencion() {
        if (eco == null) {
            vista.mostrarError("Todavía no hay una simulación en curso.");
            return false;
        }
        if (!eco.puedeIntervenir()) {
            vista.mostrarError("Solo se puede intervenir una vez cada " + Ecosistema.TURNOS_ENTRE_INTERVENCIONES + " turnos.");
            return false;
        }
        return true;
    }

    //  NUEVA SIMULACIÓN Y REPORTE

    /** La vista lo consulta para decidir si pide confirmación antes de empezar otra. */
    public boolean haySimulacionEnCurso() {
        return eco != null && !eco.simulacionTerminada();
    }

    /** Descarta la simulación actual (la vista ya pidió confirmación si hacía falta). */
    public void nuevaSimulacion() {
        eco = null;
        if (!dao.borrar()) {
            vista.mostrarError("No se pudo borrar el archivo de la simulación anterior.");
        }
        vista.getPanelConfiguracion().limpiarFormulario();
        vista.getPanelSimulacion().mostrarSinSimulacion();
        vista.mostrarPantallaConfiguracion();
        vista.mostrarEstadoApp("Configurá el ecosistema para empezar.");
    }

    public void verReporte() {
        if (eco == null) {
            vista.mostrarError("Todavía no hay una simulación para mostrar el reporte.");
            return;
        }
        if (!eco.simulacionTerminada()) {
            vista.mostrarError("El reporte final está disponible cuando termina la simulación.");
            return;
        }
        Reporte r = new Reporte(eco);
        vista.getPanelReporte().mostrarReporte(r.getResumen(), r.getFilasLongevas(),
                r.getFilasNacimientosMuertes(), r.getFilasPoblacion());
        vista.mostrarPantallaReporte();
    }

    /** La vista lo usa para habilitar u ocultar el menú/botón de simulación. */
    public void irASimulacion() {
        if (eco == null) {
            vista.getPanelSimulacion().mostrarSinSimulacion();
        } else {
            actualizarVistaSimulacion();
        }
        vista.mostrarPantallaSimulacion();
    }

    //  AUXILIARES

    private void guardar() {
        if (!dao.guardar(eco)) {
            vista.mostrarEstadoApp("Atención: no se pudo guardar la simulación en el archivo.");
        }
    }

    /** Le entrega a la vista todos los datos actuales del ecosistema. */
    private void actualizarVistaSimulacion() {
        vista.getPanelSimulacion().mostrarResumen(eco.getTurnoActual(), eco.getTurnosTotales(),
                eco.getClimaActual().getNombre(), eco.getPlantas().size(),
                eco.getConejos().size(), eco.getLobos().size());
        vista.getPanelSimulacion().mostrarEntidades(filasEntidades());
        vista.getPanelSimulacion().mostrarEventos(eco.getEventosTurno());

        boolean puede = eco.puedeIntervenir();
        String disponibilidad;
        if (eco.simulacionTerminada()) {
            disponibilidad = "Simulación terminada";
        } else if (puede) {
            disponibilidad = "Intervención disponible en este turno";
        } else {
            int faltan = Ecosistema.TURNOS_ENTRE_INTERVENCIONES - (eco.getTurnoActual() % Ecosistema.TURNOS_ENTRE_INTERVENCIONES);
            disponibilidad = "Próxima intervención en " + faltan + (faltan == 1 ? " turno" : " turnos");
        }
        vista.getPanelSimulacion().habilitarIntervencion(puede, disponibilidad, eco.puedeAgregarLobo());
        vista.getPanelSimulacion().habilitarAvanzar(!eco.simulacionTerminada());
        vista.habilitarReporte(eco.simulacionTerminada());
    }

    /** Filas {tipo, nombre, energía, edad, estado} de las entidades vivas. */
    private String[][] filasEntidades() {
        List<Entidad> vivas = new ArrayList<>(eco.getEntidadesVivas());
        String[][] filas = new String[vivas.size()][];
        for (int i = 0; i < vivas.size(); i++) {
            Entidad e = vivas.get(i);
            filas[i] = new String[]{e.getTipo(), e.getNombre(), String.valueOf((int) e.getEnergia()),
                String.valueOf(e.getEdad()), e.describirEstado()};
        }
        return filas;
    }
}
