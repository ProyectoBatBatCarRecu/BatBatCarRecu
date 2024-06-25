package es.batbatcar.v2p4.controllers;

import es.batbatcar.v2p4.exceptions.ReservaAlreadyExistsException;
import es.batbatcar.v2p4.exceptions.ReservaNotFoundException;
import es.batbatcar.v2p4.exceptions.ViajeNotFoundException;
import es.batbatcar.v2p4.modelo.dto.Reserva;
import es.batbatcar.v2p4.modelo.dto.viaje.Viaje;
import es.batbatcar.v2p4.modelo.repositories.ViajesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Controlador para gestionar las acciones relacionadas con las reservas.
 */
@Controller
public class ReservaController {

	@Autowired
	private ViajesRepository viajesRepository;

	/**
	 * Muestra el formulario para insertar una reserva.
	 *
	 * @param viajeCod el código del viaje.
	 * @param model    el modelo para pasar datos a la vista.
	 * @return la vista del formulario de reserva.
	 */
	@GetMapping("insertar-reservas")
	public String mostrarFormularioReserva(@RequestParam("viajeCod") int viajeCod, Model model) {
		Viaje viaje = viajesRepository.findByCodigo(viajeCod);
		model.addAttribute("viaje", viaje);
		return "reserva/reserva_form";
	}

	/**
	 * Maneja la acción de insertar una nueva reserva.
	 *
	 * @param viajeCod           el código del viaje.
	 * @param usuario            el nombre del usuario que realiza la reserva.
	 * @param plazas             el número de plazas reservadas.
	 * @param redirectAttributes atributos para redirección.
	 * @return la redirección a la lista de viajes o de nuevo al formulario con
	 *         errores.
	 */
	@PostMapping("reservas-add")
	public String insertarReservasAction(@RequestParam("viajeCod") int viajeCod,
			@RequestParam("usuario") String usuario, @RequestParam("plazas") int plazas,
			RedirectAttributes redirectAttributes) {
		Map<String, String> errors = new HashMap<>();
		try {
			Viaje viaje = viajesRepository.findViajeSiPermiteReserva(viajeCod, usuario, plazas);

			List<Reserva> reservas = viajesRepository.findReservasByViaje(viaje);

			String codigoReserva = viajeCod + "-" + (reservas.size() + 1);
			Reserva reserva = new Reserva(codigoReserva, usuario, plazas, viaje);

			viajesRepository.save(reserva);

			redirectAttributes.addFlashAttribute("mensaje", "Reserva realizada correctamente.");
			System.out.println("Reserva añadida");

			if (reserva.getPlazasSolicitadas() - plazas == 0) {
				viaje.cerrarViaje();
				viajesRepository.save(viaje);
			}

			return "redirect:/viajes";
		} catch (ViajeNotFoundException e) {
			errors.put("Error", "El viaje no fue encontrado.");
		} catch (Exception e) {
			errors.put("Error", e.getMessage());
		}
		redirectAttributes.addFlashAttribute("errors", errors);
		return "redirect:/insertar-reservas?viajeCod=" + viajeCod;
	}

	/**
	 * Muestra las reservas de un viaje específico.
	 *
	 * @param codigo el código del viaje (opcional).
	 * @param model  el modelo para pasar datos a la vista.
	 * @return la vista de la lista de reservas.
	 */
	@GetMapping("/viaje/reservas")
	public String getReservasAction(@RequestParam(required = false) Integer codigo, Model model) {
		if (codigo == null) {
			return "redirect:/viajes";
		}

		Viaje viaje = viajesRepository.findByCodigo(codigo);
		if (viaje == null) {
			model.addAttribute("error", "Viaje no encontrado");
			return "error";
		}

		List<Reserva> reservas = viajesRepository.findReservasByViaje(viaje);
		model.addAttribute("viaje", viaje);
		model.addAttribute("reservas", reservas);
		return "reserva/listado";
	}

	
	 /**
     * Cancela una reserva existente.
     *
     * @param codigoReserva       el código de la reserva a cancelar.
     * @param redirectAttributes  atributos para redirección.
     * @return la redirección a la lista de viajes después de la cancelación.
     */
	@GetMapping("/cancelar-reserva")
	public String cancelarReserva(@RequestParam("codigo") String codigoReserva, RedirectAttributes redirectAttributes) {
		try {
			Reserva reserva = viajesRepository.findReservaByCodigo(codigoReserva);
			viajesRepository.remove(reserva);
			redirectAttributes.addFlashAttribute("mensaje", "Reserva cancelada correctamente.");
		} catch (ReservaNotFoundException e) {
			redirectAttributes.addFlashAttribute("error", "No se encontró la reserva.");
		}
		return "redirect:/viajes";
	}

	 /**
     * Muestra los detalles de una reserva.
     *
     * @param codigoReserva el código de la reserva.
     * @param model         el modelo para pasar datos a la vista.
     * @return la vista de los detalles de la reserva.
     */
	@GetMapping("/detalle-reserva")
	public String detalleReserva(@RequestParam("codigo") String codigoReserva, Model model) {
		Reserva reserva = viajesRepository.findReservaByCodigo(codigoReserva);
		model.addAttribute("reserva", reserva);
		return "reserva/detalle_reserva";
	}

}
