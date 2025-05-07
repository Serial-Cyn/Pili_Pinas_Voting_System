/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sqlcommands;

/**
 *
 * @author Christian
 */
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import utilities.PopupWindows;

public class SQLConnector {
    
    
    public static Connection connectToDB() {
        try {
            String url = "jdbc:mysql://localhost:3306/voting_system";
            String user = "root";
            String password = "";
            
            Connection conn = DriverManager.getConnection(url, user, password);
            
            return conn;
            
        } catch (SQLException e) {
            PopupWindows.errorMessage("Connection Error!", "Could not connect to the database.");
        }
        
        return null;
    }
}
