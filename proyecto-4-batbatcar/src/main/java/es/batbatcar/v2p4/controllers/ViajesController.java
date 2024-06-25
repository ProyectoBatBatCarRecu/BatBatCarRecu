package es.batbatcar.v2p4.controllers;

import es.batbatcar.v2p4.exceptions.ViajeAlreadyExistsException;
import es.batbatcar.v2p4.exceptions.ViajeNotFoundException;
import es.batbatcar.v2p4.modelo.dto.Reserva;
import es.batbatcar.v2p4.modelo.dto.viaje.EstadoViaje;
import es.batbatcar.v2p4.modelo.dto.viaje.Viaje;
import es.batbatcar.v2p4.modelo.repositories.ViajesRepository;
import es.batbatcar.v2p4.utils.Validator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controlador para gestionar las acciones relacionadas con los viajes.
 */
@Controller
public class ViajesController {

    @Autowired
    private ViajesRepository viajesRepository;

    /**
     * Redirecciona a la página de listado de viajes.
     *
     * @return la cadena de redirección a la lista de viajes.
     */
    @GetMapping("/")
    public String getViajesRedirect() {
        return "redirect:/viajes";
    }

    /**
     * Muestra el listado de viajes, con la posibilidad de filtrar por destino.
     *
     * @param destino el destino a filtrar (opcional).
     * @param model   el modelo para pasar datos a la vista.
     * @return la vista del listado de viajes.
     */
    @GetMapping("/viajes")
    public String getViajesAction(@RequestParam(required = false) String destino, Model model) {
        if (destino != null && !destino.isEmpty()) {
            model.addAttribute("viajes", viajesRepository.buscarViajeConDestino(destino));
        } else {
            model.addAttribute("viajes", viajesRepository.findAll());
        }
        model.addAttribute("titulo", "Listado de viajes");
        return "viaje/listado";
    }

    /**
     * Muestra el formulario para añadir nuevos viajes.
     *
     * @param model el modelo para pasar datos a la vista.
     * @return la vista del formulario de viaje.
     */
    @GetMapping("/insertar-viajes")
    public String getAnyadirViajes(Model model) {
        return "viaje/viaje_form";
    }

    /**
     * Maneja la acción de añadir un nuevo viaje.
     *
     * @param params los parámetros del formulario.
     * @param model  el modelo para pasar datos a la vista.
     * @return la redirección al listado de viajes o la vista del formulario con errores.
     * @throws ViajeAlreadyExistsException si el viaje ya existe.
     * @throws ViajeNotFoundException      si el viaje no se encuentra.
     */
    @PostMapping("/viajes-add")
    public String postAddViajesAction(@RequestParam Map<String, String> params, Model model) throws ViajeAlreadyExistsException, ViajeNotFoundException {
        Map<String, String> errors = new HashMap<>();

        int codigo = viajesRepository.getNextCodViaje();
        String ruta = params.get("ruta");
        int plazas = Integer.parseInt(params.get("plazas"));
        String propietario = params.get("propietario");
        float precio = Float.parseFloat(params.get("precio"));
        int duracion = Integer.parseInt(params.get("duracion"));

        String dateValue = params.get("dia-salida");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate fechaSalida = LocalDate.parse(dateValue, formatter);

        int horaSalida = Integer.parseInt(params.get("hora-salida"));
        int minSalida = Integer.parseInt(params.get("min-salida"));

        LocalDateTime fechaHoraSalida = fechaSalida.atTime(horaSalida, minSalida);

        if (!Validator.validarRuta(ruta)) {
            errors.put("Ruta", "La ruta debe cumplir con el formato 'Origen - Destino'");
        }

        if (!Validator.validarPlazas(Integer.toString(plazas))) {
            errors.put("Plazas", "Las plazas ofertadas deben ser un valor mayor a 0");
        }

        if (!Validator.validarPropietario(propietario)) {
            errors.put("Propietario", "El propietario debe contener al menos dos nombres separados por un espacio y empezar con mayúscula");
        }

        if (!Validator.validarPrecio(Float.toString(precio))) {
            errors.put("Precio", "El precio debe ser un valor mayor a 0");
        }

        if (!Validator.validarDuracion(Integer.toString(duracion))) {
            errors.put("Duracion", "La duración debe ser un valor mayor a 0");
        }

        if (!Validator.validarDiaSalida(fechaSalida.toString())) {
            errors.put("DiaSalida", "El día de salida debe estar introducido");
        }

        if (!Validator.validarHoraSalida(Integer.toString(horaSalida), Integer.toString(minSalida))) {
            errors.put("HoraSalida", "La hora de salida debe ser válida");
        }

        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            return "viaje/viaje_form";
        }

        Viaje nuevoViaje = new Viaje(codigo, propietario, ruta, fechaHoraSalida, duracion, precio, plazas, EstadoViaje.ABIERTO);
        viajesRepository.save(nuevoViaje);

        return "redirect:/viajes";
    }

    /**
     * Muestra los detalles de un viaje específico.
     *
     * @param codigo el código del viaje.
     * @param model  el modelo para pasar datos a la vista.
     * @return la vista de los detalles del viaje.
     */
    @GetMapping("/viaje-detalles")
    public String mostrarFormularioReserva(@RequestParam int codigo, Model model) {
        Viaje viaje = viajesRepository.findByCodigo(codigo);
       
        if (viaje == null) {
            model.addAttribute("error", "Viaje no encontrado");
            return "error";
        }
        
        List<Reserva> reservas = viajesRepository.findReservasByViaje(viaje);
        model.addAttribute("viaje", viaje);
        model.addAttribute("reservas", reservas);
        return "viaje/viaje_detalle";
    }

    /**
     * Cancela un viaje específico.
     *
     * @param codigo el código del viaje.
     * @param model  el modelo para pasar datos a la vista.
     * @return la redirección al listado de viajes.
     * @throws ViajeAlreadyExistsException si el viaje ya existe.
     * @throws ViajeNotFoundException      si el viaje no se encuentra.
     */
    @GetMapping("/cancelar-viaje")
    public String cancelarViaje(@RequestParam int codigo, Model model) throws ViajeAlreadyExistsException, ViajeNotFoundException {
        Viaje viaje = viajesRepository.findByCodigo(codigo);
       
        if (viaje == null) {
            model.addAttribute("error", "Viaje no encontrado");
            return "error";
        }
        
        viaje.cerrarViaje();
        viajesRepository.save(viaje);
        return "redirect:/viajes";
    }

    /**
     * Redirecciona a la búsqueda de viajes con un destino específico.
     *
     * @param destino el destino a buscar.
     * @param model   el modelo para pasar datos a la vista.
     * @return la cadena de redirección a la búsqueda de viajes.
     */
    @GetMapping("/buscar-viaje")
    public String buscarViaje(@RequestParam String destino, Model model) {
        return "redirect:/viajes?destino=" + destino;
    }
}
