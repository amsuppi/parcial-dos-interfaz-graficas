package principal;

import controlador.SimulacionController;
import dao.SimulacionDAO;
import vista.VentanaPrincipal;

/**
 * Punto de entrada: crea las piezas (DAO, vista y controlador), las conecta y muestra la ventana.
 */
public class Main {

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            SimulacionDAO dao = new SimulacionDAO();
            VentanaPrincipal ventana = new VentanaPrincipal();
            SimulacionController controlador = new SimulacionController(ventana, dao);

            ventana.setControlador(controlador);
            ventana.setLocationRelativeTo(null);
            ventana.setVisible(true);

            controlador.iniciarAplicacion();
        });
    }
}
