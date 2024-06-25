package es.batbatcar.v2p4.modelo.dao.sqldao;

import es.batbatcar.v2p4.exceptions.ViajeNotFoundException;
import es.batbatcar.v2p4.modelo.services.MySQLConnection;
import es.batbatcar.v2p4.modelo.dao.interfaces.ViajeDAO;
import es.batbatcar.v2p4.modelo.dto.viaje.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.beans.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


/**
 * Implementación concreta de ViajeDAO utilizando SQL para acceso a datos.
 */
@Repository
public class SQLViajeDAO implements ViajeDAO {

	@Autowired
	private MySQLConnection mySQLConnection;

	/**
     * Constructor que inyecta la conexión MySQL.
     * 
     * @param mySQLConnection Conexión a MySQL a ser inyectada.
     */
	public SQLViajeDAO(@Autowired MySQLConnection mySQLConnection) {
		this.mySQLConnection = mySQLConnection;
	}

	
	/**
     * Mapea un ResultSet a un objeto Viaje.
     * 
     * @param rs ResultSet a ser mapeado.
     * @return Objeto Viaje mapeado desde el ResultSet.
     * @throws SQLException Si hay un error de SQL durante el mapeo.
     */
	private Viaje MapToViaje(ResultSet rs) throws SQLException {

		int codigo = rs.getInt("codViaje");
		String ruta = rs.getString("ruta");
		String propietario = rs.getString("propietario");
		LocalDateTime fechaSalida = rs.getTimestamp("fechaSalida").toLocalDateTime();
		long duracion = rs.getLong("duracion");
		float precio = rs.getFloat("precio");
		int plazasOfertadas = rs.getInt("plazasOfertadas");
		EstadoViaje estadoViaje = EstadoViaje.parse(rs.getString("estadoViaje"));

		return new Viaje(codigo, propietario, ruta, fechaSalida, duracion, precio, plazasOfertadas, estadoViaje);

	}
	
	/**
     * Obtiene todos los viajes almacenados.
     * 
     * @return Set de Viajes encontrados.
     */
	@Override
	public Set<Viaje> findAll() {
		String sql = String.format("SELECT * FROM viajes");

		Connection connection = this.mySQLConnection.getConnection();

		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ResultSet rs = ps.executeQuery();

			Set<Viaje> viajes = new HashSet<>();

			while (rs.next()) {
				viajes.add(MapToViaje(rs));
			}
			return viajes;

		} catch (SQLException e) {
			return null;
		}
	}

	/**
     * Obtiene todos los viajes que coinciden con una ciudad específica.
     * 
     * @param city Ciudad a buscar en las rutas de los viajes.
     * @return Set de Viajes encontrados para la ciudad especificada.
     */
	@Override
	public Set<Viaje> findAll(String city) {
		String sql = String.format("SELECT * FROM viajes WHERE ruta LIKE ? ");

		Connection connection = this.mySQLConnection.getConnection();

		try (PreparedStatement ps = connection.prepareStatement(sql)) {

			ps.setString(1, "%" + city);
			ResultSet rs = ps.executeQuery();

			Set<Viaje> viajes = new HashSet<>();

			while (rs.next()) {
				viajes.add(MapToViaje(rs));
			}
			return viajes;

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
     * Obtiene todos los viajes que tienen un estado específico.
     * 
     * @param estadoViaje Estado del viaje a buscar.
     * @return Set de Viajes encontrados para el estado especificado.
     */
	@Override
	public Set<Viaje> findAll(EstadoViaje estadoViaje) {
		String sql = String.format("SELECT * FROM viajes WHERE estadoViaje = ?");

		Connection connection = this.mySQLConnection.getConnection();

		try (PreparedStatement ps = connection.prepareStatement(sql)) {

			ps.setString(0, estadoViaje.toString());
			ResultSet rs = ps.executeQuery();

			Set<Viaje> viajes = new HashSet<>();

			while (rs.next()) {
				viajes.add(MapToViaje(rs));
			}
			return viajes;

		} catch (SQLException e) {
			return null;
		}
	}

	/**
     * Método no implementado actualmente.
     * 
     * @param viajeClass Clase específica de Viaje.
     * @return Set de Viajes de la clase especificada.
     * @throws RuntimeException Método no implementado.
     */
	@Override
	public Set<Viaje> findAll(Class<? extends Viaje> viajeClass) {
		throw new RuntimeException("Not yet implemented");
	}

	/**
     * Busca un viaje por su identificador único.
     * 
     * @param codViaje Identificador del viaje a buscar.
     * @return Viaje encontrado o null si no se encuentra.
     */
	@Override
	public Viaje findById(int codViaje) {
		String sql = String.format("SELECT * FROM viajes WHERE codViaje = ? ");

		Connection connection = this.mySQLConnection.getConnection();

		try (PreparedStatement ps = connection.prepareStatement(sql)) {

			ps.setInt(1, codViaje);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				return MapToViaje(rs);
			}
			return null;

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
     * Busca un viaje por su identificador único, lanzando una excepción si no se encuentra.
     * 
     * @param codViaje Identificador del viaje a buscar.
     * @return Viaje encontrado.
     * @throws ViajeNotFoundException Si no se encuentra el viaje.
     */
	@Override
	public Viaje getById(int codViaje) throws ViajeNotFoundException {
		String sql = String.format("SELECT * FROM viajes WHERE codViaje = ?");

		Connection connection = this.mySQLConnection.getConnection();

		try (PreparedStatement ps = connection.prepareStatement(sql)) {

			ps.setInt(0, codViaje);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				return MapToViaje(rs);
			}
			throw new ViajeNotFoundException("El viaje no existe ;(");

		} catch (SQLException e) {
			return null;
		}
	}

	/**
     * Agrega un nuevo viaje a la base de datos.
     * 
     * @param viaje Viaje a ser agregado.
     */
	@Override
	public void add(Viaje viaje) {
		String sql = "INSERT INTO viajes (codViaje, propietario, ruta, fechaSalida, duracion, precio, plazasOfertadas, estadoViaje) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

		Connection connection = this.mySQLConnection.getConnection();

		try (PreparedStatement ps = connection.prepareStatement(sql)) {

			ps.setInt(1, viaje.getCodViaje());
			ps.setString(2, viaje.getPropietario());
			ps.setString(3, viaje.getRuta());
			ps.setTimestamp(4, Timestamp.valueOf(viaje.getFechaSalida()));
			ps.setInt(5, (int) viaje.getDuracion());
			ps.setFloat(6, viaje.getPrecio());
			ps.setInt(7, viaje.getPlazasOfertadas());
			ps.setString(8, viaje.getEstado().toString());

			ps.executeUpdate();

		} catch (SQLException e) {

		}
	}

	/**
     * Actualiza la información de un viaje existente en la base de datos.
     * 
     * @param viaje Viaje con la información actualizada.
     * @throws ViajeNotFoundException Si no se encuentra el viaje a actualizar.
     */
	@Override
	public void update(Viaje viaje) throws ViajeNotFoundException {
		String sql = "UPDATE viajes SET propietario = ?, ruta = ?, fechaSalida = ?, duracion = ?, precio = ?, plazasOfertadas = ?, estadoViaje = ? WHERE codViaje = ?";

		Connection connection = this.mySQLConnection.getConnection();

		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, viaje.getPropietario());
			ps.setString(2, viaje.getRuta());
			ps.setTimestamp(3, Timestamp.valueOf(viaje.getFechaSalida()));
			ps.setInt(4, (int) viaje.getDuracion());
			ps.setFloat(5, viaje.getPrecio());
			ps.setInt(6, viaje.getPlazasOfertadas());
			ps.setString(7, viaje.getEstado().toString());
			ps.setInt(8, viaje.getCodViaje());

			int rowsAffected = ps.executeUpdate();
			if (rowsAffected == 0) {
				throw new ViajeNotFoundException("No se encontró el viaje con codViaje: " + viaje.getCodViaje());
			}

		} catch (SQLException e) {

		}
	}

	/**
     * Elimina un viaje de la base de datos.
     * 
     * @param viaje Viaje a ser eliminado.
     * @throws ViajeNotFoundException Si no se encuentra el viaje a eliminar.
     */
	@Override
	public void remove(Viaje viaje) throws ViajeNotFoundException {
		String sql = "DELETE FROM viajes WHERE codViaje = ?";

		Connection connection = this.mySQLConnection.getConnection();

		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setInt(1, viaje.getCodViaje());

			int rowsAffected = ps.executeUpdate();
			if (rowsAffected == 0) {
				throw new ViajeNotFoundException("No se encontró el viaje con codViaje: " + viaje.getCodViaje());
			}

		} catch (SQLException e) {

		}
	}

}
