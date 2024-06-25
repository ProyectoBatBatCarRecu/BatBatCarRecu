package es.batbatcar.v2p4.modelo.repositories;

import es.batbatcar.v2p4.exceptions.ReservaAlreadyExistsException;
import es.batbatcar.v2p4.exceptions.ReservaNotFoundException;
import es.batbatcar.v2p4.exceptions.ViajeAlreadyExistsException;
import es.batbatcar.v2p4.exceptions.ViajeNotFoundException;
import es.batbatcar.v2p4.modelo.dao.inmemorydao.InMemoryReservaDAO;
import es.batbatcar.v2p4.modelo.dao.inmemorydao.InMemoryViajeDAO;
import es.batbatcar.v2p4.modelo.dto.Reserva;
import es.batbatcar.v2p4.modelo.dto.viaje.Viaje;
import es.batbatcar.v2p4.modelo.dao.interfaces.ReservaDAO;
import es.batbatcar.v2p4.modelo.dao.interfaces.ViajeDAO;
import es.batbatcar.v2p4.modelo.dao.sqldao.SQLReservaDAO;
import es.batbatcar.v2p4.modelo.dao.sqldao.SQLViajeDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public class ViajesRepository {

    private final ViajeDAO viajeDAO;
    private final ReservaDAO reservaDAO;

    public ViajesRepository(@Autowired SQLViajeDAO viajeDAO, @Autowired SQLReservaDAO reservaDAO) {
        this.viajeDAO = viajeDAO;
        this.reservaDAO = reservaDAO;
    }
    
    /** 
     * Obtiene un conjunto de todos los viajes
     * @return
     */
    public Set<Viaje> findAll() {
        
    	// Se recuperan todos los viajes del DAO de viajes
    	Set<Viaje> viajes = viajeDAO.findAll();
        
    	// Se completa la información acerca de las reservas de cada viaje a través del DAO de reservas
        for (Viaje viaje : viajes) {
        	if (this.reservaDAO.findAllByTravel(viaje).size() > 0) {
            	viaje.setSeHanRealizadoReservas(true);
            }
		}
        return viajes;
    }
    
    /**
     * Obtiene el código del siguiente viaje
     * @return
     */
    public int getNextCodViaje() {
        return this.viajeDAO.findAll().size() + 1;
    }
    
    /**
     * Guarda el viaje (actualiza si ya existe o añade si no existe)
     * @param viaje
     * @throws ViajeAlreadyExistsException
     * @throws ViajeNotFoundException
     */
    public void save(Viaje viaje) throws ViajeAlreadyExistsException, ViajeNotFoundException {
    	
    	if (viajeDAO.findById(viaje.getCodViaje()) == null) {
    		viajeDAO.add(viaje);
    	} else {
    		viajeDAO.update(viaje);
    	}
    }
	
    /**
     * Encuentra todas las reservas de @viaje
     * @param viaje
     * @return
     */
	public List<Reserva> findReservasByViaje(Viaje viaje) {
		
		return reservaDAO.findAllByTravel(viaje);
	}
	
	/**
	 * Guarda la reserva
	 * @param reserva
	 * @throws ReservaAlreadyExistsException
	 * @throws ReservaNotFoundException
	 */
    public void save(Reserva reserva) throws ReservaAlreadyExistsException, ReservaNotFoundException {
    	
    	if (reservaDAO.findById(reserva.getCodigoReserva()) == null) {
    		reservaDAO.add(reserva);
    	} else {
    		reservaDAO.update(reserva);
    	}
    }
    
    /**
     * Elimina la reserva
     * @param reserva
     * @throws ReservaNotFoundException
     */
	public void remove(Reserva reserva) throws ReservaNotFoundException {
		reservaDAO.remove(reserva);
	}
	
	/**
	 * Busca un viaje por su código.
	 *
	 * @param codigoViaje el código del viaje a buscar.
	 * @return el viaje encontrado, o null si no se encuentra.
	 */
	public Viaje findByCodigo(int codigoViaje) {
	    for (Viaje viaje : viajeDAO.findAll()) {
	        if (viaje.getCodViaje() == codigoViaje) {
	            return viaje;
	        }
	    }
	    return null;
	}
	
	/**
	 * Busca un viaje por su código y verifica si permite realizar una reserva.
	 *
	 * @param codViaje          el código del viaje a buscar.
	 * @param usuario           el usuario que quiere realizar la reserva.
	 * @param plazasSolicitadas el número de plazas que se quieren reservar.
	 * @return el viaje encontrado si cumple con todas las condiciones.
	 * @throws ViajeNotFoundException si no se encuentra el viaje con el código dado.
	 * @throws Exception              si el usuario es el propietario del viaje,
	 *                                si el viaje no está disponible,
	 *                                si el viaje está cancelado,
	 *                                si no hay suficientes plazas disponibles,
	 *                                o si el usuario ya tiene una reserva en el viaje.
	 */
	public Viaje findViajeSiPermiteReserva(int codViaje, String usuario, int plazasSolicitadas) throws Exception {
	    Viaje viaje = viajeDAO.findById(codViaje);

	    if (viaje == null) {
	        throw new ViajeNotFoundException(String.valueOf(codViaje));
	    }

	    if (viaje.getPropietario().equals(usuario)) {
	        throw new Exception("El usuario propietario no puede reservar en su propio viaje");
	    }

	    if (!viaje.estaDisponible()) {
	        throw new Exception("El viaje no está disponible para reservas");
	    }

	    if (viaje.isCancelado()) {
	        throw new Exception("El viaje está cancelado");
	    }

	    int plazasOcupadas = reservaDAO.getNumPlazasReservadasEnViaje(viaje);
	    if (plazasOcupadas + plazasSolicitadas > viaje.getPlazasOfertadas()) {
	        throw new Exception("No hay suficientes plazas disponibles");
	    }

	    if (reservaDAO.findByUserInTravel(usuario, viaje) != null) {
	        throw new Exception("El usuario ya tiene una reserva en este viaje");
	    }

	    return viaje;
	}
	
	/**
	 * Busca viajes que tienen un destino específico.
	 *
	 * @param destino el destino a buscar.
	 * @return un conjunto de viajes que tienen el destino especificado.
	 */
	public Set<Viaje> buscarViajeConDestino (String destino) {
		
		return viajeDAO.findAll(destino);
	}
	
	/**
     * Encuentra una reserva por su código.
     *
     * @param codigoReserva el código de la reserva a buscar.
     * @return la reserva encontrada, o null si no se encuentra.
     */
    public Reserva findReservaByCodigo(String codigoReserva) {
        return reservaDAO.findById(codigoReserva);
    }

}
