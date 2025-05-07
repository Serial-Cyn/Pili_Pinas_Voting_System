/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utilities;

import java.security.SecureRandom;
import sqlcommands.SQLConnector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Christian
 */
public class RandomKeyGenerator {
    SQLConnector sqlConnector = new SQLConnector();
    
    Connection conn = sqlConnector.connectToDB();
    
    public String randomizeKey(int characters) {
        char[] CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        SecureRandom RANDOM = new SecureRandom();
        String key;

        try {
            while (true) {
                // Generate a key
                char[] generated = new char[characters];
                for (int i = 0; i < generated.length; i++) {
                    generated[i] = CHAR_POOL[RANDOM.nextInt(CHAR_POOL.length)];
                }
                key = new String(generated);

                // Check if key exists in the database
                String query = "SELECT COUNT(*) FROM tbl_voters WHERE voters_key = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, key);
                ResultSet rs = stmt.executeQuery();

                rs.next();
                int count = rs.getInt(1);
                rs.close();
                stmt.close();

                // If no matches, it's unique
                if (count == 0) break;
            }

        } catch (SQLException e) {
            PopupWindows.errorMessage("Key Generation Error - 0004", "Unable to produce key codes for voters");
            
            return null; // or throw a custom exception
        }

        return key;
    }
}
