package es.batbatcar.v2p4.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Clase utilitaria para validar diferentes tipos de datos y formatos.
 */
public class Validator {

	private static final String RUTA_REGEXP = "^[A-Za-z]+\\s*-\\s*[A-Za-z]+$";
    private static final String NOMBRE_REGEXP = "^[A-Z][a-z]+\\s+[A-Z][a-z]+$";
    private static final String NUMERIC_REGEXP = "\\d+";
    private static final String DECIMAL_REGEXP = "\\d+\\.?\\d*";
	
    /**
     * Valida si una cadena representa una fecha y hora válida en formato "yyyy-MM-dd HH:mm".
     *
     * @param dateTime la cadena que se desea validar como fecha y hora.
     * @return true si la cadena es válida como fecha y hora, false de lo contrario.
     */
    public static boolean isValidDateTime(String dateTime) {
        try {
            LocalDate.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch (DateTimeParseException e) {
            return false;
        }
        return true;
    }

    /**
     * Valida si una cadena representa una fecha válida en formato "yyyy-MM-dd".
     *
     * @param date la cadena que se desea validar como fecha.
     * @return true si la cadena es válida como fecha, false de lo contrario.
     */
    public static boolean isValidDate(String date) {
        try {
            LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            return false;
        }
        return true;
    }

    
    /**
     * Valida si una cadena representa una hora válida en formato "HH:mm".
     *
     * @param time la cadena que se desea validar como hora.
     * @return true si la cadena es válida como hora, false de lo contrario.
     */
    public static boolean isValidTime(String time) {
        try {
            LocalDate.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            return false;
        }
        return true;
    }
    
    
    /**
     * Valida si una cadena representa una ruta válida según el patrón especificado.
     *
     * @param ruta la cadena que se desea validar como ruta.
     * @return true si la cadena cumple con el patrón de ruta, false de lo contrario.
     */
    public static boolean validarRuta(String ruta) {
        return ruta != null && ruta.matches(RUTA_REGEXP);
    }

    
    /**
     * Valida si una cadena representa un número de plazas válido (entero positivo mayor que cero).
     *
     * @param plazas la cadena que se desea validar como número de plazas.
     * @return true si la cadena es un número entero positivo, false de lo contrario.
     */
    public static boolean validarPlazas(String plazas) {
        return plazas != null && plazas.matches(NUMERIC_REGEXP) && Integer.parseInt(plazas) > 0;
    }

    
    /**
     * Valida si una cadena representa un nombre de propietario válido según el patrón especificado.
     *
     * @param propietario la cadena que se desea validar como nombre de propietario.
     * @return true si la cadena cumple con el patrón de nombre de propietario, false de lo contrario.
     */
    public static boolean validarPropietario(String propietario) {
        return propietario != null && propietario.matches(NOMBRE_REGEXP);
    }

    /**
     * Valida si una cadena representa un precio válido (decimal positivo mayor que cero).
     *
     * @param precio la cadena que se desea validar como precio.
     * @return true si la cadena es un número decimal positivo, false de lo contrario.
     */
    public static boolean validarPrecio(String precio) {
        return precio != null && precio.matches(DECIMAL_REGEXP) && Double.parseDouble(precio) > 0;
    }

    
    /**
     * Valida si una cadena representa una duración válida (entero positivo mayor que cero).
     *
     * @param duracion la cadena que se desea validar como duración.
     * @return true si la cadena es un número entero positivo, false de lo contrario.
     */
    public static boolean validarDuracion(String duracion) {
        return duracion != null && duracion.matches(NUMERIC_REGEXP) && Integer.parseInt(duracion) > 0;
    }

    /**
     * Valida si una cadena de día de salida no es nula ni vacía.
     *
     * @param diaSalida la cadena que se desea validar como día de salida.
     * @return true si la cadena no es nula ni vacía, false de lo contrario.
     */
    public static boolean validarDiaSalida(String diaSalida) {
        return diaSalida != null && !diaSalida.isEmpty();
    }
    
    /**
     * Valida si las cadenas de hora y minutos de salida representan una hora válida.
     *
     * @param horaSalida la cadena que representa las horas de salida.
     * @param minSalida  la cadena que representa los minutos de salida.
     * @return true si las cadenas son válidas como hora y minutos, false de lo contrario.
     */
    public static boolean validarHoraSalida(String horaSalida, String minSalida) {
        return horaSalida != null && minSalida != null 
               && horaSalida.matches(NUMERIC_REGEXP) && minSalida.matches(NUMERIC_REGEXP)
               && Integer.parseInt(horaSalida) >= 0 && Integer.parseInt(horaSalida) < 24 
               && Integer.parseInt(minSalida) >= 0 && Integer.parseInt(minSalida) < 60;
    }


}

