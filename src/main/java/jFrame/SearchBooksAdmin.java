/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jFrame;

import static jFrame.Validation.validateNIC;
import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Date;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Senath Chandira
 */
public class SearchBooksAdmin extends javax.swing.JFrame {

    /**
     * Creates new form userMember
     */
    String bookName, Author, Publisher, Edition, Category, No_of_pages, NIC, ISBN, BookID, Status;

    public SearchBooksAdmin() {
        initComponents();
        setBookDetailsToTable();
    }

    public void searchBook() {
        Pattern pattern = Pattern.compile("^\\s*$");
        if (!txt_Search.getText().isEmpty() && !pattern.matcher(txt_Search.getText()).matches()) {
            String searchText = txt_Search.getText().trim();
            int choice = cBox_Search.getSelectedIndex();
            String columnName = "";

            switch (choice) {
                case 0:
                    columnName = "ISBN";
                    break;
                case 1:
                    columnName = "Book_name";
                    break;
                case 2:
                    columnName = "Catagory";
                    break;
                case 3:
                    columnName = "Publisher";
                    break;
                case 4:
                    columnName = "Author";
                    break;
                default:
                    throw new AssertionError();
            }

            try {
                Connection con = DBConnection.getConnection();
                String sql = "SELECT * FROM tbl_book WHERE " + columnName + " LIKE ?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setString(1, "%" + searchText + "%");
                ResultSet rs = pst.executeQuery();

                DefaultTableModel model = (DefaultTableModel) tbl_book.getModel();
                model.setRowCount(0); // Clear previous search results

                while (rs.next()) {
                    // Extract data from the ResultSet and add it to the table model
                    String ISBN = rs.getString("ISBN");
                    String bookName = rs.getString("Book_name");
                    String category = rs.getString("Catagory");
                    String publisher = rs.getString("Publisher");
                    String author = rs.getString("Author");
                    String pages = rs.getString("No_of_pages");
                    String edition = rs.getString("Edition");

                    //String Status;
                    model.addRow(new Object[]{ISBN, bookName, author, publisher, pages, edition, category, /*Status*/});
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please enter the search criteria");
        }
    }

    public void setBookDetailsToTable() {
        try {
            Connection con = DBConnection.getConnection();
            Statement st1 = con.createStatement();
            Statement st2 = con.createStatement();

            // Retrieve book ID, status, and ISBN from tbl_bookcopy
            ResultSet rs1 = st1.executeQuery("SELECT Book_ID, Status, ISBN FROM tbl_bookcopy");

            // Clear existing rows from the table
            DefaultTableModel model = (DefaultTableModel) tbl_book.getModel();
            model.setRowCount(0);

            while (rs1.next()) {
                BookID = rs1.getString("Book_ID");
                Status = rs1.getString("Status");
                ISBN = rs1.getString("ISBN");

                // Retrieve book name from tbl_book using the ISBN from rs1
                ResultSet rs2 = st2.executeQuery("SELECT Book_name,Author,Publisher,Edition,Category,No_of_pages FROM tbl_book WHERE ISBN = '" + ISBN + "'");

                if (rs2.next()) {
                    bookName = rs2.getString("Book_name");
                    No_of_pages = rs2.getString("No_of_pages");
                    Category = rs2.getString("Category");
                    Edition = rs2.getString("Edition");
                    Publisher = rs2.getString("Publisher");
                    Author = rs2.getString("Author");
                }

                rs2.close();

                Object[] obj = {/*Book_ID,*/ISBN, bookName, Author, Publisher, No_of_pages, Edition, Category, Status};
                model.addRow(obj);
            }

            // Close the database resources
            rs1.close();
            st1.close();
            st2.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean reserveBook1() {
        NIC = txt_NIC.getText();
        boolean bookisReserved = false;
        int selectedRow = tbl_book.getSelectedRow();

        if (selectedRow >= 0) {
            ISBN = tbl_book.getValueAt(selectedRow, 0).toString();
            Status = tbl_book.getValueAt(selectedRow, 7).toString();
        } else {
            // No row is selected
            JOptionPane.showMessageDialog(this, "No book is selected.", "", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        LocalDate reservedDate = LocalDate.now(); // Get the current system date

        try {
            Connection con = DBConnection.getConnection();

            // Check if the entered NIC exists in tbl_memberdetails
            String checkMemberSql = "SELECT NIC FROM tbl_memberdetails WHERE NIC = ?";
            PreparedStatement checkMemberPst = con.prepareStatement(checkMemberSql);
            checkMemberPst.setString(1, NIC);
            ResultSet memberRs = checkMemberPst.executeQuery();

            if (memberRs.next()) {
                // Check if the member has already reserved a book
                String checkReservationSql = "SELECT NIC FROM tbl_reservedbook WHERE NIC = ? AND Reservation_Status = 'Reserved'";
                PreparedStatement checkReservationPst = con.prepareStatement(checkReservationSql);
                checkReservationPst.setString(1, NIC);
                ResultSet reservationRs = checkReservationPst.executeQuery();

                if (reservationRs.next()) {
                    JOptionPane.showMessageDialog(this, "This member has already reserved a book.", "", JOptionPane.WARNING_MESSAGE);
                    return false;
                } else {
                    // Check the number of available or pending copies
                    String checkBookReservationSql = "SELECT COUNT(*) AS Copies FROM tbl_bookcopy WHERE ISBN = ? AND (Status = 'Available' OR Status = 'Pending' OR Status = 'Reserved')";
                    PreparedStatement checkBookReservationPst = con.prepareStatement(checkBookReservationSql);
                    checkBookReservationPst.setString(1, ISBN);
                    ResultSet bookReservationRs = checkBookReservationPst.executeQuery();

                    if (bookReservationRs.next()) {
                        int copies = bookReservationRs.getInt("Copies");
                        if (copies > 0) {
                            // Check if the number of reservations exceeds the number of available copies
                            String checkReservationsSql = "SELECT COUNT(*) AS Reservations FROM tbl_reservedbook WHERE ISBN = ? AND Reservation_Status = 'Reserved'";
                            PreparedStatement checkReservationsPst = con.prepareStatement(checkReservationsSql);
                            checkReservationsPst.setString(1, ISBN);
                            ResultSet reservationsRs = checkReservationsPst.executeQuery();

                            if (reservationsRs.next()) {
                                int reservations = reservationsRs.getInt("Reservations");
                                if (reservations < copies) {
                                    // Insert the reservation into tbl_reservedbook
                                    String insertReservationSql = "INSERT INTO tbl_reservedbook (NIC, ISBN, Reserved_Date, Reservation_Status) VALUES (?, ?, ?, ?)";
                                    PreparedStatement insertReservationPst = con.prepareStatement(insertReservationSql);
                                    insertReservationPst.setString(1, NIC);
                                    insertReservationPst.setString(2, ISBN);
                                    insertReservationPst.setDate(3, java.sql.Date.valueOf(reservedDate));
                                    insertReservationPst.setString(4, "Reserved");
                                    insertReservationPst.executeUpdate();

                                    // Update the status of one available or pending copy of the book to "Reserved"
                                    String updateStatusSql = "UPDATE tbl_bookcopy SET Status = 'Reserved' WHERE Book_ID = (SELECT MIN(Book_ID) FROM tbl_bookcopy WHERE ISBN = ? AND (Status = 'Available'))";
                                    PreparedStatement updateStatusPst = con.prepareStatement(updateStatusSql);
                                    updateStatusPst.setString(1, ISBN);
                                    updateStatusPst.executeUpdate();

                                    JOptionPane.showMessageDialog(this, "Book reserved successfully.", "", JOptionPane.INFORMATION_MESSAGE);
                                    bookisReserved = true;
                                } else {
                                    JOptionPane.showMessageDialog(this, "The maximum number of reservations for this book has been reached.", "", JOptionPane.WARNING_MESSAGE);
                                    return false;
                                }
                            }
                        } else {
                            JOptionPane.showMessageDialog(this, "The selected book is not available for reservation.", "", JOptionPane.WARNING_MESSAGE);
                            return false;
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid member NIC.", "", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            // Close the database resources
            memberRs.close();
            checkMemberPst.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            bookisReserved = false;
        }
        return bookisReserved;
    }

    public void clearTable() {
        DefaultTableModel model = (DefaultTableModel) tbl_book.getModel();
        model.setRowCount(0);
    }

    public boolean inputValidation() {
        NIC = txt_NIC.getText();
        if (NIC.trim().isEmpty()) {
            return false;
        }
        if (!validateNIC(NIC)) {
            return false;
        }
        return true;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        lbl_Close = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbl_book = new rojeru_san.complementos.RSTableMetro();
        cBox_Search = new javax.swing.JComboBox<>();
        txt_Search = new javax.swing.JTextField();
        btn_Search = new rojerusan.RSMaterialButtonRectangle();
        btn_clear = new rojerusan.RSMaterialButtonRectangle();
        btn_Add5 = new rojerusan.RSMaterialButtonRectangle();
        jLabel3 = new javax.swing.JLabel();
        txt_NIC = new javax.swing.JTextField();
        lbl_reservedtable = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 1, 1, 1, new java.awt.Color(0, 0, 0)));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(0, 0, 0));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setFont(new java.awt.Font("Arial", 1, 16)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Search Book");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 10, 110, 40));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/adminIcons/adminIcons/icons8_Book_26px.png"))); // NOI18N
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 10, 30, 50));

        lbl_Close.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/icons8-close-20.png"))); // NOI18N
        lbl_Close.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbl_CloseMouseClicked(evt);
            }
        });
        jPanel2.add(lbl_Close, new org.netbeans.lib.awtextra.AbsoluteConstraints(1160, 10, 20, 20));

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1190, 70));

        tbl_book.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ISBN", "Book_Name", "Author", "Publisher", "No_of_pages", "Edtion", "Category", "Status"
            }
        ));
        tbl_book.setColorBackgoundHead(new java.awt.Color(0, 51, 102));
        tbl_book.setColorBordeFilas(new java.awt.Color(102, 102, 102));
        tbl_book.setColorBordeHead(new java.awt.Color(102, 102, 102));
        tbl_book.setColorFilasBackgound2(new java.awt.Color(255, 255, 255));
        tbl_book.setRowHeight(25);
        tbl_book.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbl_bookMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tbl_book);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 210, 1110, 230));

        cBox_Search.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        cBox_Search.setForeground(new java.awt.Color(102, 102, 102));
        cBox_Search.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "search by ISBN", "search by book name", "search by caregory", "search by publisher", "search by author" }));
        jPanel1.add(cBox_Search, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 100, 340, 30));

        txt_Search.setFont(new java.awt.Font("Segoe UI Semibold", 0, 16)); // NOI18N
        txt_Search.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(204, 204, 204)));
        jPanel1.add(txt_Search, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 140, 340, 30));

        btn_Search.setBackground(new java.awt.Color(0, 153, 255));
        btn_Search.setText("Search");
        btn_Search.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        btn_Search.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_SearchActionPerformed(evt);
            }
        });
        jPanel1.add(btn_Search, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 120, 110, 50));

        btn_clear.setBackground(new java.awt.Color(0, 153, 255));
        btn_clear.setText("Clear");
        btn_clear.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        btn_clear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_clearActionPerformed(evt);
            }
        });
        jPanel1.add(btn_clear, new org.netbeans.lib.awtextra.AbsoluteConstraints(870, 120, 110, 50));

        btn_Add5.setBackground(new java.awt.Color(0, 153, 255));
        btn_Add5.setText("Reserve");
        btn_Add5.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        btn_Add5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_Add5ActionPerformed(evt);
            }
        });
        jPanel1.add(btn_Add5, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 530, 110, 50));

        jLabel3.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(102, 102, 102));
        jLabel3.setText("Enter member NIC :");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 490, 150, 30));

        txt_NIC.setFont(new java.awt.Font("Segoe UI Semibold", 0, 16)); // NOI18N
        txt_NIC.setForeground(new java.awt.Color(102, 102, 102));
        txt_NIC.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));
        jPanel1.add(txt_NIC, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 490, 200, 30));

        lbl_reservedtable.setFont(new java.awt.Font("Segoe UI Semibold", 1, 18)); // NOI18N
        lbl_reservedtable.setForeground(new java.awt.Color(0, 51, 102));
        lbl_reservedtable.setText("View Reserved Book Table >>");
        lbl_reservedtable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbl_reservedtableMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lbl_reservedtableMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lbl_reservedtableMouseExited(evt);
            }
        });
        jPanel1.add(lbl_reservedtable, new org.netbeans.lib.awtextra.AbsoluteConstraints(910, 550, 260, 30));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1190, 600));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void tbl_bookMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbl_bookMouseClicked

    }//GEN-LAST:event_tbl_bookMouseClicked

    private void btn_Add5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_Add5ActionPerformed
        if (inputValidation()) {
            reserveBook1();
            clearTable();
            setBookDetailsToTable();
        } else {
            JOptionPane.showMessageDialog(this, "Enter NIC.", "", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_btn_Add5ActionPerformed

    private void btn_SearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_SearchActionPerformed
        searchBook();
    }//GEN-LAST:event_btn_SearchActionPerformed

    private void btn_clearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_clearActionPerformed
        //setBookDetailsToTable();
    }//GEN-LAST:event_btn_clearActionPerformed

    private void lbl_CloseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_CloseMouseClicked
        this.dispose();
    }//GEN-LAST:event_lbl_CloseMouseClicked

    private void lbl_reservedtableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_reservedtableMouseClicked
        reservedBook iBook = new reservedBook();
        iBook.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_lbl_reservedtableMouseClicked

    private void lbl_reservedtableMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_reservedtableMouseEntered
        Color c = new Color(102, 102, 102);
        lbl_reservedtable.setForeground(c);
        lbl_reservedtable.setText("<HTML><U>View Reserved Book Table >></U></HTML>");
    }//GEN-LAST:event_lbl_reservedtableMouseEntered

    private void lbl_reservedtableMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_reservedtableMouseExited
        Color darkblue = new Color(0, 51, 102);
        lbl_reservedtable.setForeground(darkblue);

        lbl_reservedtable.setText("View Reserved Book Table >>");
    }//GEN-LAST:event_lbl_reservedtableMouseExited

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SearchBooksAdmin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SearchBooksAdmin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SearchBooksAdmin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SearchBooksAdmin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SearchBooksAdmin().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private rojerusan.RSMaterialButtonRectangle btn_Add5;
    private rojerusan.RSMaterialButtonRectangle btn_Search;
    private rojerusan.RSMaterialButtonRectangle btn_clear;
    private javax.swing.JComboBox<String> cBox_Search;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbl_Close;
    private javax.swing.JLabel lbl_reservedtable;
    private rojeru_san.complementos.RSTableMetro tbl_book;
    private javax.swing.JTextField txt_NIC;
    private javax.swing.JTextField txt_Search;
    // End of variables declaration//GEN-END:variables
}
