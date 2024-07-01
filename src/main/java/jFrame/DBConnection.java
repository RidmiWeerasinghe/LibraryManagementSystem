/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jFrame;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 *
 * @author upeks
 */
public class DBConnection {
    static Connection con = null;
    
    public  static  Connection getConnection(){
        try {
            //The driver is automatically registered via the SPI and manual loading of the driver class is generally unnecessary.
            //Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/librarymanagementsystemdb","root","");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return con;
    }
}
