package es.batbatcar.v2p4.modelo.dao.sqldao;

import es.batbatcar.v2p4.exceptions.ReservaAlreadyExistsException;
import es.batbatcar.v2p4.exceptions.ReservaNotFoundException;
import es.batbatcar.v2p4.exceptions.ViajeNotFoundException;
import es.batbatcar.v2p4.modelo.dto.Reserva;
import es.batbatcar.v2p4.modelo.dto.viaje.EstadoViaje;
import es.batbatcar.v2p4.modelo.dto.viaje.Viaje;
import es.batbatcar.v2p4.modelo.services.MySQLConnection;
import es.batbatcar.v2p4.modelo.dao.interfaces.ReservaDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Implementación de ReservaDAO utilizando SQL para el acceso a datos.
 */
@Repository
public class SQLReservaDAO implements ReservaDAO {

	@Autowired
	private MySQLConnection mySQLConnection;
	private SQLViajeDAO sqlViajeDAO;

	
	/**
     * Constructor para inyección de dependencias.
     *
     * @param mySQLConnection la conexión a la base de datos MySQL.
     * @param sqlViajeDAO     el DAO para gestionar los viajes relacionados con las reservas.
     */
	public SQLReservaDAO(@Autowired MySQLConnection mySQLConnection, @Autowired SQLViajeDAO sqlViajeDAO) {
		this.mySQLConnection = mySQLConnection;
		this.sqlViajeDAO = sqlViajeDAO;
	}

	/**
     * Mapea un ResultSet a un objeto Reserva.
     *
     * @param rs el ResultSet obtenido de la consulta SQL.
     * @return una instancia de Reserva.
     * @throws SQLException si ocurre algún error al acceder a los datos en el ResultSet.
     */
	private Reserva MapToReserva(ResultSet rs) throws SQLException {

		String codigo = rs.getString("codigoReserva");
		String usuario = rs.getString("usuario");
		int plazasSolicitadas = rs.getInt("plazasSolicitadas");
		LocalDateTime fechaRealizacion = rs.getTimestamp("fechaRealizacion").toLocalDateTime();
		Viaje viaje = sqlViajeDAO.findById(rs.getInt("codViaje"));

		return new Reserva(codigo, usuario, plazasSolicitadas, fechaRealizacion, viaje);

	}

	/**
     * Obtiene todas las reservas.
     *
     * @return un conjunto de objetos Reserva.
     */
	@Override
	public Set<Reserva> findAll() {
		String sql = "SELECT * FROM reservas";

		Connection connection = this.mySQLConnection.getConnection();

		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ResultSet rs = ps.executeQuery();

			Set<Reserva> reservas = new HashSet<>();

			while (rs.next()) {
				reservas.add(MapToReserva(rs));
			}
			return reservas;

		} catch (SQLException e) {
			return null;
		}
	}

	/**
     * Busca una reserva por su código.
     *
     * @param codigoReserva el código de la reserva a buscar.
     * @return la reserva encontrada o null si no existe.
     */
	@Override
	public Reserva findById(String codigoReserva) {
		String sql = "SELECT * FROM reservas WHERE codigoReserva = ?";

		Connection connection = this.mySQLConnection.getConnection();

		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, codigoReserva);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				return MapToReserva(rs);
			}
			return null;

		} catch (SQLException e) {
			return null;
		}
	}

	 /**
     * Obtiene todas las reservas de un usuario específico.
     *
     * @param user el nombre de usuario del usuario.
     * @return una lista de objetos Reserva del usuario especificado.
     */
	@Override
	public ArrayList<Reserva> findAllByUser(String user) {
		String sql = "SELECT * FROM reservas WHERE usuario = ?";

		Connection connection = this.mySQLConnection.getConnection();

		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, user);
			ResultSet rs = ps.executeQuery();

			ArrayList<Reserva> reservas = new ArrayList<>();

			while (rs.next()) {
				reservas.add(MapToReserva(rs));
			}
			return reservas;

		} catch (SQLException e) {
			return new ArrayList<>();
		}
	}

	/**
     * Obtiene todas las reservas asociadas a un viaje específico.
     *
     * @param viaje el viaje para el cual se buscan las reservas.
     * @return una lista de objetos Reserva asociadas al viaje especificado.
     */
	@Override
	public ArrayList<Reserva> findAllByTravel(Viaje viaje) {
		String sql = String.format("SELECT * FROM reservas WHERE codViaje = ?");

		Connection connection = this.mySQLConnection.getConnection();

		try (PreparedStatement ps = connection.prepareStatement(sql)) {

			ps.setInt(1, viaje.getCodViaje());
			ResultSet rs = ps.executeQuery();

			ArrayList<Reserva> reservas = new ArrayList<>();

			while (rs.next()) {
				reservas.add(MapToReserva(rs));
			}
			return reservas;

		} catch (SQLException e) {
			return new ArrayList<>();
		}
	}

	/**
     * Obtiene una reserva por su ID.
     *
     * @param id el ID de la reserva a buscar.
     * @return la reserva encontrada.
     * @throws ReservaNotFoundException si no se encuentra la reserva con el ID especificado.
     */
	@Override
	public Reserva getById(String id) throws ReservaNotFoundException {
		Reserva reserva = findById(id);
		if (reserva == null) {
			throw new ReservaNotFoundException("No se encontró la reserva con ID: " + id);
		}
		return reserva;
	}

	/**
     * Busca reservas por parámetros de búsqueda específicos.
     *
     * @param viaje        el viaje asociado a las reservas.
     * @param searchParams el texto de búsqueda para coincidir con usuario o código de reserva.
     * @return una lista de reservas que coinciden con los parámetros de búsqueda.
     */
	@Override
	public List<Reserva> findAllBySearchParams(Viaje viaje, String searchParams) {
		String sql = "SELECT * FROM reservas WHERE codViaje = ? AND (usuario LIKE ? OR codigoReserva LIKE ?)";

		Connection connection = this.mySQLConnection.getConnection();

		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setInt(1, viaje.getCodViaje());
			ps.setString(2, "%" + searchParams + "%");
			ps.setString(3, "%" + searchParams + "%");
			ResultSet rs = ps.executeQuery();

			List<Reserva> reservas = new ArrayList<>();

			while (rs.next()) {
				reservas.add(MapToReserva(rs));
			}
			return reservas;

		} catch (SQLException e) {
			return new ArrayList<>();
		}
	}

	 /**
     * Agrega una nueva reserva.
     *
     * @param reserva la reserva a ser agregada.
     */
	@Override
	public void add(Reserva reserva) {
		String sql = "INSERT INTO reservas (codigoReserva, usuario, plazasSolicitadas, fechaRealizacion, codViaje) "
				+ "VALUES (?, ?, ?, ?, ?)";

		Connection connection = this.mySQLConnection.getConnection();

		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, reserva.getCodigoReserva());
			ps.setString(2, reserva.getUsuario());
			ps.setInt(3, reserva.getPlazasSolicitadas());
			ps.setTimestamp(4, Timestamp.valueOf(reserva.getFechaRealizacion()));
			ps.setInt(5, reserva.getViaje().getCodViaje());

			ps.executeUpdate();

		} catch (SQLException e) {

		}
	}

	/**
     * Actualiza una reserva existente.
     *
     * @param reserva la reserva a ser actualizada.
     * @throws ReservaNotFoundException si no se encuentra la reserva con el código especificado.
     */
	@Override
	public void update(Reserva reserva) throws ReservaNotFoundException {
		String sql = "UPDATE reservas SET usuario = ?, plazasSolicitadas = ?, fechaRealizacion = ?, codViaje = ? WHERE codigoReserva = ?";

		Connection connection = this.mySQLConnection.getConnection();

		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, reserva.getUsuario());
			ps.setInt(2, reserva.getPlazasSolicitadas());
			ps.setTimestamp(3, Timestamp.valueOf(reserva.getFechaRealizacion()));
			ps.setInt(4, reserva.getViaje().getCodViaje());
			ps.setString(5, reserva.getCodigoReserva());

			int rowsAffected = ps.executeUpdate();
			if (rowsAffected == 0) {
				throw new ReservaNotFoundException(
						"No se encontró la reserva con codigoReserva: " + reserva.getCodigoReserva());
			}

		} catch (SQLException e) {

		}
	}
	
	 /**
     * Elimina una reserva existente.
     *
     * @param reserva la reserva a ser eliminada.
     * @throws ReservaNotFoundException si no se encuentra la reserva con el código especificado.
     */
	@Override
	public void remove(Reserva reserva) throws ReservaNotFoundException {
		String sql = "DELETE FROM reservas WHERE codigoReserva = ?";

		Connection connection = this.mySQLConnection.getConnection();

		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, reserva.getCodigoReserva());

			int rowsAffected = ps.executeUpdate();
			if (rowsAffected == 0) {
				throw new ReservaNotFoundException(
						"No se encontró la reserva con codigoReserva: " + reserva.getCodigoReserva());
			}

		} catch (SQLException e) {

		}
	}

	/**
     * Obtiene el número total de plazas reservadas en un viaje específico.
     *
     * @param viaje el viaje para el cual se desea obtener el número de plazas reservadas.
     * @return el número total de plazas reservadas en el viaje especificado.
     */
	@Override
	public int getNumPlazasReservadasEnViaje(Viaje viaje) {
		String sql = "SELECT SUM(plazasSolicitadas) AS numPlazas FROM reservas WHERE codViaje = ?";

		Connection connection = this.mySQLConnection.getConnection();

		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setInt(1, viaje.getCodViaje());
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				return rs.getInt("numPlazas");
			}
			return 0;

		} catch (SQLException e) {
			return 0;
		}
	}

	 /**
     * Busca una reserva específica de un usuario en un viaje.
     *
     * @param usuario el nombre de usuario del usuario.
     * @param viaje   el viaje en el cual se busca la reserva.
     * @return la reserva encontrada para el usuario y viaje especificados, o null si no se encuentra.
     */
	@Override
	public Reserva findByUserInTravel(String usuario, Viaje viaje) {
		String sql = "SELECT * FROM reservas WHERE usuario = ? AND codViaje = ?";

		Connection connection = this.mySQLConnection.getConnection();

		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, usuario);
			ps.setInt(2, viaje.getCodViaje());
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				return MapToReserva(rs);
			}
			return null;

		} catch (SQLException e) {
			return null;
		}
	}

}
