package es.batbatcar.v2p4.modelo.services;

import es.batbatcar.v2p4.exceptions.DatabaseConnectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Servicio para manejar la conexión a la base de datos MySQL.
 */
@Service
public class MySQLConnection {

   private static Connection connection;
   private String ip;
   private String database;
   private String userName;
   private String password;

   
   public MySQLConnection() {

	   /**
	     * Constructor de la clase MySQLConnection. Define los parámetros de conexión por defecto.
	     * 
	     * Modifica estos datos para que se adapte a tu entorno de desarrollo.
	     */
       this.ip = "localhost:3306";
       this.database = "batbatcar";
       this.userName = "root";
       this.password = "1234";
   }
   
   /**
    * Obtiene la conexión a la base de datos MySQL.
    *
    * @return la conexión activa a la base de datos MySQL.
    * @throws DatabaseConnectionException si ocurre un error al intentar establecer la conexión.
    */
   public Connection getConnection() {
	   
	   if (connection == null) {
           try {
               String dbURL = "jdbc:mysql://" + ip + "/" + database;
               Connection connection = DriverManager.getConnection(dbURL,userName,password);
               this.connection = connection;
               System.out.println("Conexion valida: " + connection.isValid(20));

           } catch (SQLException ex) {
               throw new RuntimeException(ex.getMessage());
           }
       }

       return this.connection;

   }
}
