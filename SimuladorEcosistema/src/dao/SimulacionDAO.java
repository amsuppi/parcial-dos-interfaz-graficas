package dao;

import modelo.Ecosistema;

/**
 * Guarda y recupera el estado de la simulación en un archivo de texto plano.
 * No usa Swing ni muestra mensajes: informa el resultado con lo que devuelve
 * y el controlador decide qué hacer.
 *
 * TODO (Etapa 2): implementar los cuatro métodos.
 */
public class SimulacionDAO {

    private static final String RUTA = "datos/simulacion.txt"; // relativa a la carpeta del proyecto

    /** Escribe el estado completo. true si pudo guardar, false si hubo error de E/S. */
    public boolean guardar(Ecosistema eco) {
        // TODO Etapa 2
        return false;
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
}
