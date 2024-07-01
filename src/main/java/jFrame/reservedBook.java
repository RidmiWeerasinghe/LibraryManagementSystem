/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jFrame;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.View;

/**
 *
 * @author Senath Chandira
 */
public class reservedBook extends javax.swing.JFrame {

    /**
     * Creates new form ManageBook
     */
    String ISBN, Book_name, Status, Book_Id, NIC, ReservationStatus;
    Date ReservewdDate;
    //borders of textboxes
    Color white = new Color(255, 255, 255);
    Color lightgray = new Color(153, 153, 153);
    Border borderout = BorderFactory.createMatteBorder(0, 0, 1, 0, lightgray);
    Border border = BorderFactory.createMatteBorder(0, 0, 1, 0, white);

    public reservedBook() {
        initComponents();
        setBookDetailsToTable1();
        //txt_BookID.setEditable(false);
    }

    //getting data from database
    public void setBookDetailsToTable() {
        try {
            Connection con = DBConnection.getConnection();
            Statement st1 = con.createStatement();
            Statement st2 = con.createStatement();
            Statement st3 = con.createStatement();

            // Retrieve book ID, status, and ISBN from tbl_bookcopy
            ResultSet rs1 = st1.executeQuery("SELECT Book_ID, Status, ISBN FROM tbl_bookcopy");

            // Clear existing rows from the table
            DefaultTableModel model = (DefaultTableModel) tbl_rbook.getModel();
            model.setRowCount(0);

            while (rs1.next()) {
                Book_Id = rs1.getString("Book_ID");
                Status = rs1.getString("Status");
                ISBN = rs1.getString("ISBN");

                // Retrieve book name from tbl_book using the ISBN from rs1
                ResultSet rs2 = st2.executeQuery("SELECT Book_name FROM tbl_book WHERE ISBN = '" + ISBN + "'");

                if (rs2.next()) {
                    Book_name = rs2.getString("Book_name");
                }

                rs2.close();

                // Retrieve Reserved_Date from tbl_reservedbook using the Book_ID from rs1
                ResultSet rs3 = st3.executeQuery("SELECT Reserved_Date, NIC FROM tbl_reservedbook WHERE Book_ID = '" + Book_Id + "'");

                if (rs3.next()) {
                    java.sql.Date reservedDate = rs3.getDate("Reserved_Date");
                    NIC = rs3.getString("NIC");
                    ReservationStatus = rs3.getString("Reservation_Status");
                    // Calculate number of days since reservation
                    LocalDate currentDate = LocalDate.now();
                    LocalDate reservationDate = reservedDate.toLocalDate();
                    long daysSinceReservation = ChronoUnit.DAYS.between(reservationDate, currentDate);

                    Object[] obj = {Book_Id, ISBN, Book_name, NIC, reservedDate, daysSinceReservation, ReservationStatus};
                    model.addRow(obj);
                }

                rs3.close();
            }

            // Close the database resources
            rs1.close();
            st1.close();
            st2.close();
            st3.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //COPY
    public void setBookDetailsToTable1() {
        try {
            Connection con = DBConnection.getConnection();
            Statement st1 = con.createStatement();
            Statement st2 = con.createStatement();
            Statement st3 = con.createStatement();

            DefaultTableModel model = (DefaultTableModel) tbl_rbook.getModel();
            model.setRowCount(0);

            ResultSet rs1 = st1.executeQuery("SELECT DISTINCT ISBN FROM tbl_RESERVEDBOOK");
            while (rs1.next()) {
                ISBN = rs1.getString("ISBN");
                ResultSet rs2 = st2.executeQuery("SELECT Book_name FROM tbl_book WHERE ISBN = '" + ISBN + "'");

                if (rs2.next()) {
                    Book_name = rs2.getString("Book_name");
                }
                rs2.close();

                ResultSet rs3 = st3.executeQuery("SELECT Reserved_Date, NIC,Reservation_Status FROM tbl_reservedbook WHERE ISBN = '" + ISBN + "'");
                while (rs3.next()) {
                    java.sql.Date reservedDate = rs3.getDate("Reserved_Date");
                    NIC = rs3.getString("NIC");
                    ReservationStatus = rs3.getString("Reservation_Status");

                    // Calculate number of days since reservation
                    LocalDate currentDate = LocalDate.now();
                    LocalDate reservationDate = reservedDate.toLocalDate();
                    long daysSinceReservation = ChronoUnit.DAYS.between(reservationDate, currentDate);

                    Object[] obj = {ISBN, Book_name, NIC, reservedDate, daysSinceReservation, ReservationStatus};
                    model.addRow(obj);
                }
                rs3.close();
            }

            // Close the database resources
            rs1.close();
            st1.close();
            st2.close();
            st3.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//deleter book
    public boolean cancelReservation() {
        boolean isCancelled = false;
        int selectedRow = tbl_rbook.getSelectedRow();
        String ISBN = tbl_rbook.getValueAt(selectedRow, 0).toString(); // Variable to store retrieved ISBN

        try {
            Connection con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // Retrieve the Book_ID from tbl_bookcopy with status "Pending" or "Reserved"
            String retrieveBookIdSql = "SELECT Book_ID FROM tbl_bookcopy WHERE ISBN = ? AND (Status = 'Pending' OR Status = 'Reserved') LIMIT 1";
            PreparedStatement retrieveBookIdPst = con.prepareStatement(retrieveBookIdSql);
            retrieveBookIdPst.setString(1, ISBN);
            ResultSet bookIdRs = retrieveBookIdPst.executeQuery();

            if (bookIdRs.next()) {
                String bookId = bookIdRs.getString("Book_ID");

                // Update Reservation_Status in tbl_reservedbook
                String updateReservationStatusSql = "UPDATE tbl_reservedbook SET Reservation_Status = 'Cancelled' WHERE ISBN = ? AND NIC = ?";
                PreparedStatement updateReservationStatusPst = con.prepareStatement(updateReservationStatusSql);
                updateReservationStatusPst.setString(1, ISBN);
                updateReservationStatusPst.setString(2, NIC);
                int rowCount1 = updateReservationStatusPst.executeUpdate();

                // Update Status in tbl_bookcopy
                String updateBookCopyStatusSql = "UPDATE tbl_bookcopy SET Status = CASE WHEN Status = 'Reserved' THEN 'Available' ELSE Status END WHERE Book_ID = ?";
                PreparedStatement updateBookCopyStatusPst = con.prepareStatement(updateBookCopyStatusSql);
                updateBookCopyStatusPst.setString(1, bookId);
                int rowCount2 = updateBookCopyStatusPst.executeUpdate();

                if (rowCount1 > 0 && rowCount2 > 0) {
                    con.commit();
                    isCancelled = true;
                } else {
                    con.rollback();
                    isCancelled = false;
                }
            } else {
                // No book with the given ISBN and appropriate status found in tbl_bookcopy
                JOptionPane.showMessageDialog(this, "No book available for cancellation.", "", JOptionPane.WARNING_MESSAGE);
            }

            con.setAutoCommit(true);
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isCancelled;
    }

    public void clearTable() {
        DefaultTableModel model = (DefaultTableModel) tbl_rbook.getModel();
        model.setRowCount(0);
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
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        lbl_Close = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbl_rbook = new rojeru_san.complementos.RSTableMetro();
        btn_Cancle = new rojerusan.RSMaterialButtonRectangle();
        btn_Delete = new rojerusan.RSMaterialButtonRectangle();
        lbl_admin1 = new javax.swing.JLabel();
        lbl_back = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(0, 0, 0));
        jPanel2.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(51, 51, 51)));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/bookDetailsGray.png"))); // NOI18N
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 40, 40));

        jLabel2.setFont(new java.awt.Font("Segoe UI Semibold", 1, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Reserved Books");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 0, 150, 60));

        lbl_Close.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/icons8-close-20.png"))); // NOI18N
        lbl_Close.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbl_CloseMouseClicked(evt);
            }
        });
        jPanel2.add(lbl_Close, new org.netbeans.lib.awtextra.AbsoluteConstraints(1150, 10, 20, 20));

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1180, -1));

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 1, 1, 1, new java.awt.Color(0, 51, 102)));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tbl_rbook.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ISBN", "Book Name", "NIC", "Reserved Date", "Days Since Reservation", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tbl_rbook.setColorBackgoundHead(new java.awt.Color(0, 51, 102));
        tbl_rbook.setColorBordeFilas(new java.awt.Color(102, 102, 102));
        tbl_rbook.setColorBordeHead(new java.awt.Color(102, 102, 102));
        tbl_rbook.setColorFilasBackgound2(new java.awt.Color(255, 255, 255));
        tbl_rbook.setColorFilasForeground1(new java.awt.Color(51, 51, 51));
        tbl_rbook.setColorFilasForeground2(new java.awt.Color(51, 51, 51));
        tbl_rbook.setColorSelBackgound(new java.awt.Color(204, 204, 204));
        tbl_rbook.setColorSelForeground(new java.awt.Color(0, 0, 0));
        tbl_rbook.setFuenteFilas(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        tbl_rbook.setFuenteFilasSelect(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        tbl_rbook.setFuenteHead(new java.awt.Font("Segoe UI Semibold", 0, 18)); // NOI18N
        tbl_rbook.setRowHeight(25);
        tbl_rbook.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbl_rbookMouseClicked(evt);
            }
        });
        tbl_rbook.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbl_rbookKeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(tbl_rbook);

        jPanel4.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 50, 1120, 400));

        btn_Cancle.setBackground(new java.awt.Color(0, 153, 255));
        btn_Cancle.setText("Clear");
        btn_Cancle.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        btn_Cancle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_CancleActionPerformed(evt);
            }
        });
        jPanel4.add(btn_Cancle, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 470, 200, 60));

        btn_Delete.setBackground(new java.awt.Color(0, 153, 255));
        btn_Delete.setText("Cancle Reservation");
        btn_Delete.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        btn_Delete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_DeleteActionPerformed(evt);
            }
        });
        jPanel4.add(btn_Delete, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 470, 200, 60));

        lbl_admin1.setFont(new java.awt.Font("Segoe UI Semibold", 1, 18)); // NOI18N
        lbl_admin1.setForeground(new java.awt.Color(255, 255, 255));
        lbl_admin1.setText("Change admin login credentials");
        lbl_admin1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbl_admin1MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lbl_admin1MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lbl_admin1MouseExited(evt);
            }
        });
        jPanel4.add(lbl_admin1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1070, -30, 100, 650));

        lbl_back.setFont(new java.awt.Font("Segoe UI Semibold", 1, 18)); // NOI18N
        lbl_back.setForeground(new java.awt.Color(0, 51, 102));
        lbl_back.setText("<< Back");
        lbl_back.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbl_backMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lbl_backMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lbl_backMouseExited(evt);
            }
        });
        jPanel4.add(lbl_back, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 510, 130, 40));

        jPanel1.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 60, 1180, 560));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 620));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void lbl_CloseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_CloseMouseClicked
        this.dispose();
    }//GEN-LAST:event_lbl_CloseMouseClicked

    private void tbl_rbookMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbl_rbookMouseClicked
        int rowNo = tbl_rbook.getSelectedRow();
        TableModel model = tbl_rbook.getModel();
    }//GEN-LAST:event_tbl_rbookMouseClicked

    private void tbl_rbookKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbl_rbookKeyPressed

    }//GEN-LAST:event_tbl_rbookKeyPressed

    private void btn_DeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_DeleteActionPerformed
        if (cancelReservation() == true) {
            JOptionPane.showMessageDialog(this, "Reservation Calceled", "", JOptionPane.INFORMATION_MESSAGE);
            clearTable();
            setBookDetailsToTable1();
        } else {
            JOptionPane.showMessageDialog(this, "Fail to cancle the reservation", "", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_btn_DeleteActionPerformed

    private void lbl_admin1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_admin1MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_lbl_admin1MouseClicked

    private void lbl_admin1MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_admin1MouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_lbl_admin1MouseEntered

    private void lbl_admin1MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_admin1MouseExited
        // TODO add your handling code here:
    }//GEN-LAST:event_lbl_admin1MouseExited

    private void btn_CancleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_CancleActionPerformed
        tbl_rbook.clearSelection();
    }//GEN-LAST:event_btn_CancleActionPerformed

    private void lbl_backMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_backMouseClicked
        SearchBooksAdmin searchBooksAdmin = new SearchBooksAdmin();
        searchBooksAdmin.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_lbl_backMouseClicked

    private void lbl_backMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_backMouseEntered
        lbl_back.setText("<HTML><U>&lt;&lt; Back</U></HTML>");
    }//GEN-LAST:event_lbl_backMouseEntered

    private void lbl_backMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_backMouseExited
        lbl_back.setText("<< Back");
    }//GEN-LAST:event_lbl_backMouseExited

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
            java.util.logging.Logger.getLogger(reservedBook.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(reservedBook.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(reservedBook.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(reservedBook.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new reservedBook().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private rojerusan.RSMaterialButtonRectangle btn_Cancle;
    private rojerusan.RSMaterialButtonRectangle btn_Delete;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbl_Close;
    private javax.swing.JLabel lbl_admin1;
    private javax.swing.JLabel lbl_back;
    private rojeru_san.complementos.RSTableMetro tbl_rbook;
    // End of variables declaration//GEN-END:variables
}
