/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jFrame;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.JOptionPane;

/**
 *
 * @author upeks
 */
public class Book {

    private String ISBN;
    private String Book_Name;
    private String Author;
    private String Publisher;
    private String Edition;
    private String Catagory;
    private int Quantity;
    private int No_of_pages;
    private int Price;

    public boolean addBook() {
        boolean isAdded = false;
        try {
        Connection con = DBConnection.getConnection();

        String checkSql = "SELECT * FROM tbl_book WHERE ISBN = ?";
        PreparedStatement checkPst = con.prepareStatement(checkSql);
        checkPst.setString(1, ISBN);
        ResultSet checkRs = checkPst.executeQuery();

        if (checkRs.next()) {
            // Book already exists
           // JOptionPane.showMessageDialog(this, "Book with ISBN " + ISBN + " already exists");
        } else {
            String sql = "INSERT INTO tbl_book(ISBN,Book_name,Author,Publisher,Edition,Catagory,No_of_pages,Quantity,Price) values(?,?,?,?,?,?,?,?,?)";
            PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pst.setString(1, ISBN);
            pst.setString(2, Book_Name);
            pst.setString(3, Author);
            pst.setString(4, Publisher);
            pst.setString(5, Edition);
            pst.setString(6, Catagory);
            pst.setInt(7, No_of_pages);
            pst.setInt(8, Quantity);
            pst.setInt(9, Price);

            int rowCount = pst.executeUpdate();

            // Get the generated ISBN
            int bookID = -1;
            ResultSet generatedKeys = pst.getGeneratedKeys();
            if (generatedKeys.next()) {
                //bookID = generatedKeys.getInt(1);
                ISBN = generatedKeys.getString(1);
                //txt_ISBN.setText(ISBN);
            }

            if (rowCount > 0 && ISBN != null) {
                String bookCopySql = "INSERT INTO tbl_BookCopy (ISBN, Status) VALUES (?, ?)";
                PreparedStatement bookCopyPst = con.prepareStatement(bookCopySql);

                for (int i = 1; i <= Quantity; i++) {
                    bookCopyPst.setString(1, ISBN);
                    bookCopyPst.setString(2, "Available");
                    bookCopyPst.executeUpdate();
                }
                isAdded = true;
            } else {
                isAdded = false;
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
        return isAdded;
    }
}
