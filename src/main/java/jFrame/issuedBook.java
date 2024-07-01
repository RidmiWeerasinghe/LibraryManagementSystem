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
import java.util.Date;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author Senath Chandira
 */
public class issuedBook extends javax.swing.JFrame {

    /**
     * Creates new form ManageBook
     */
    String ISBN, Book_name, Status, Book_Id, NIC;
    Date issue_date, due_date, return_date;
    //borders of textboxes
    Color white = new Color(255, 255, 255);
    Color lightgray = new Color(153, 153, 153);
    Border borderout = BorderFactory.createMatteBorder(0, 0, 1, 0, lightgray);
    Border border = BorderFactory.createMatteBorder(0, 0, 1, 0, white);

    public issuedBook() {
        initComponents();
        setBookDetailsToTable();
        //txt_BookID.setEditable(false);
    }

    //getting data from database
    public void setBookDetailsToTable() {
        LocalDate currentLDate = LocalDate.now(ZoneId.systemDefault());
        Date currentDate = Date.from(currentLDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        try {
            Connection con = DBConnection.getConnection();
            Statement st1 = con.createStatement();
            Statement st2 = con.createStatement();
            Statement st3 = con.createStatement();

            // Retrieve book ID, status, and ISBN from tbl_bookcopy
            ResultSet rs1 = st1.executeQuery("SELECT Book_ID, NIC, Issue_Date, Due_Date, Return_Date FROM tbl_issuebook");

            // Clear existing rows from the table
            DefaultTableModel model = (DefaultTableModel) tbl_book.getModel();
            model.setRowCount(0);

            while (rs1.next()) {
                Book_Id = rs1.getString("Book_ID");
                NIC = rs1.getString("NIC");
                issue_date = rs1.getDate("Issue_Date");
                due_date = rs1.getDate("Due_Date");
                return_date = rs1.getDate("Return_Date");
                //System.out.println("return date " + due_date);

                if (due_date != null && return_date == null && due_date.before(currentDate)) {
                    // Due date has passed, perform necessary actions
                    // For example, you can set the status to "Overdue" or change the row color
                    Status = "Overdue";
                } else if (return_date != null) {
                    Status = "Returned";
                } else {
                    // Due date is still valid, perform necessary actions
                    Status = "Pending";
                }

                // Retrieve ISBN from tbl_bookcopy using the Book_ID
                ResultSet rs2 = st2.executeQuery("SELECT ISBN FROM tbl_bookcopy WHERE Book_ID = '" + Book_Id + "'");
                if (rs2.next()) {
                    ISBN = rs2.getString("ISBN");

                    // Retrieve book name from tbl_book using the ISBN
                    ResultSet rs3 = st3.executeQuery("SELECT Book_name FROM tbl_book WHERE ISBN = '" + ISBN + "'");
                    if (rs3.next()) {
                        Book_name = rs3.getString("Book_name");
                    }

                    rs3.close();
                }

                rs2.close();

                Object[] obj = {Book_Id, ISBN, Book_name, NIC, issue_date, due_date, Status};
                model.addRow(obj);
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
    public boolean deleteBook() {
        boolean isDeleted = false;
        int selectedRow = tbl_book.getSelectedRow();
        Book_Id = tbl_book.getValueAt(selectedRow, 0).toString();
        ISBN = tbl_book.getValueAt(selectedRow, 1).toString();// Variable to store retrieved ISBN

        try {
            Connection con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // Retrieve ISBN and Quantity from tbl_book using the selected Book_ID
            String retrieveBookDetailsSql = "SELECT Quantity FROM tbl_book WHERE ISBN = ?";
            PreparedStatement retrieveBookDetailsPst = con.prepareStatement(retrieveBookDetailsSql);
            retrieveBookDetailsPst.setString(1, ISBN);
            ResultSet bookDetailsRs = retrieveBookDetailsPst.executeQuery();

            if (bookDetailsRs.next()) {
                int quantity = bookDetailsRs.getInt("Quantity");

                // Delete from tbl_BookCopy
                String deleteBookCopySql = "DELETE FROM tbl_BookCopy WHERE Book_ID=?";
                PreparedStatement deleteBookCopyPst = con.prepareStatement(deleteBookCopySql);
                deleteBookCopyPst.setString(1, Book_Id);
                int rowCount2 = deleteBookCopyPst.executeUpdate();

                if (quantity == 1) {
                    // If quantity is 1, delete from tbl_book as well
                    String deleteBookSql = "DELETE FROM tbl_book WHERE ISBN=?";
                    PreparedStatement deleteBookPst = con.prepareStatement(deleteBookSql);
                    deleteBookPst.setString(1, ISBN);
                    int rowCount1 = deleteBookPst.executeUpdate();

                    if (rowCount1 > 0 && rowCount2 > 0) {
                        con.commit();
                        isDeleted = true;
                    } else {
                        con.rollback();
                        isDeleted = false;
                    }
                } else {
                    // Decrease quantity in tbl_book
                    String decreaseQuantitySql = "UPDATE tbl_book SET Quantity = Quantity - 1 WHERE ISBN=?";
                    PreparedStatement decreaseQuantityPst = con.prepareStatement(decreaseQuantitySql);
                    decreaseQuantityPst.setString(1, ISBN);
                    int rowCount1 = decreaseQuantityPst.executeUpdate();

                    if (rowCount1 > 0 && rowCount2 > 0) {
                        con.commit();
                        isDeleted = true;
                    } else {
                        con.rollback();
                        isDeleted = false;
                    }
                }
            }

            con.setAutoCommit(true);
            bookDetailsRs.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isDeleted;
    }

    public void searchBook() {
        Pattern pattern = Pattern.compile("^\\s*$");
        if (!txt_searchbook.getText().isEmpty() && !pattern.matcher(txt_searchbook.getText()).matches()) {
            String searchText = txt_searchbook.getText().trim();
            int choice = cbox_searchbook.getSelectedIndex();
            String columnName = "";

            switch (choice) {
                case 0:
                    columnName = "ISBN";
                    break;
                case 1:
                    columnName = "Book_name";
                    break;
                case 2:
                    columnName = "Book_ID";
                    break;
                case 3:
                    columnName = "Status";
                    break;
                case 4:
                    columnName = "NIC";
                    break;
                default:
                    throw new AssertionError();
            }

            try {
                Connection con = DBConnection.getConnection();
                String sql = "SELECT b.ISBN, b.Book_name, i.Book_ID, i.NIC, i.Issue_Date, i.Due_Date FROM tbl_book b JOIN tbl_issuebook i ON b.ISBN = i.ISBN WHERE b." + columnName + " LIKE ?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setString(1, "%" + searchText + "%");
                ResultSet rs = pst.executeQuery();

                DefaultTableModel model = (DefaultTableModel) tbl_book.getModel();
                model.setRowCount(0); // Clear previous search results

                while (rs.next()) {
                    // Extract data from the ResultSet and add it to the table model
                    ISBN = rs.getString("ISBN");
                    Book_name = rs.getString("Book_name");
                    Book_Id = rs.getString("Book_ID");
                    NIC = rs.getString("NIC");
                    issue_date = rs.getDate("Issue_Date");
                    due_date = rs.getDate("Due_Date");

                    model.addRow(new Object[]{Book_Id, ISBN, Book_name, NIC, issue_date, due_date, Status});
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            JOptionPane.showMessageDialog(this, "Please enter the search criteria", "", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void clearTable() {
        DefaultTableModel model = (DefaultTableModel) tbl_book.getModel();
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
        tbl_book = new rojeru_san.complementos.RSTableMetro();
        cbox_searchbook = new javax.swing.JComboBox<>();
        txt_searchbook = new javax.swing.JTextField();
        btn_clear = new rojerusan.RSMaterialButtonRectangle();
        btn_search = new rojerusan.RSMaterialButtonRectangle();
        btn_Cancle = new rojerusan.RSMaterialButtonRectangle();
        lbl_admin1 = new javax.swing.JLabel();
        lbl_reservedtable1 = new javax.swing.JLabel();

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
        jLabel2.setText("Book Status");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 0, 120, 60));

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

        tbl_book.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "BookID", "ISBN", "Book_Name", "NIC", "Issue Date", "Due Date", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tbl_book.setColorBackgoundHead(new java.awt.Color(0, 51, 102));
        tbl_book.setColorBordeFilas(new java.awt.Color(102, 102, 102));
        tbl_book.setColorBordeHead(new java.awt.Color(102, 102, 102));
        tbl_book.setColorFilasBackgound2(new java.awt.Color(255, 255, 255));
        tbl_book.setColorFilasForeground1(new java.awt.Color(51, 51, 51));
        tbl_book.setColorFilasForeground2(new java.awt.Color(51, 51, 51));
        tbl_book.setColorSelBackgound(new java.awt.Color(204, 204, 204));
        tbl_book.setColorSelForeground(new java.awt.Color(0, 0, 0));
        tbl_book.setFuenteFilas(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        tbl_book.setFuenteFilasSelect(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        tbl_book.setFuenteHead(new java.awt.Font("Segoe UI Semibold", 0, 18)); // NOI18N
        tbl_book.setRowHeight(25);
        tbl_book.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbl_bookMouseClicked(evt);
            }
        });
        tbl_book.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbl_bookKeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(tbl_book);

        jPanel4.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 130, 1120, 340));

        cbox_searchbook.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        cbox_searchbook.setForeground(new java.awt.Color(102, 102, 102));
        cbox_searchbook.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "search by ISBN", "search by book name", "search by Book_ID" }));
        cbox_searchbook.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbox_searchbookActionPerformed(evt);
            }
        });
        jPanel4.add(cbox_searchbook, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 20, 340, 30));

        txt_searchbook.setFont(new java.awt.Font("Segoe UI Semibold", 0, 16)); // NOI18N
        txt_searchbook.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(204, 204, 204)));
        jPanel4.add(txt_searchbook, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 60, 340, 30));

        btn_clear.setBackground(new java.awt.Color(0, 153, 255));
        btn_clear.setText("Clear");
        btn_clear.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        btn_clear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_clearActionPerformed(evt);
            }
        });
        jPanel4.add(btn_clear, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 40, 90, 50));

        btn_search.setBackground(new java.awt.Color(0, 153, 255));
        btn_search.setText("Search");
        btn_search.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        btn_search.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_searchActionPerformed(evt);
            }
        });
        jPanel4.add(btn_search, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 40, 90, 50));

        btn_Cancle.setBackground(new java.awt.Color(0, 153, 255));
        btn_Cancle.setText("Cancle");
        btn_Cancle.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        btn_Cancle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_CancleActionPerformed(evt);
            }
        });
        jPanel4.add(btn_Cancle, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 490, 90, 60));

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

        lbl_reservedtable1.setFont(new java.awt.Font("Segoe UI Semibold", 1, 18)); // NOI18N
        lbl_reservedtable1.setForeground(new java.awt.Color(0, 51, 102));
        lbl_reservedtable1.setText("<< Back");
        lbl_reservedtable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbl_reservedtable1MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lbl_reservedtable1MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lbl_reservedtable1MouseExited(evt);
            }
        });
        jPanel4.add(lbl_reservedtable1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 510, 100, 30));

        jPanel1.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 60, 1180, 560));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1180, 620));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void lbl_CloseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_CloseMouseClicked
        this.dispose();
    }//GEN-LAST:event_lbl_CloseMouseClicked

    private void tbl_bookMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbl_bookMouseClicked
        int rowNo = tbl_book.getSelectedRow();
        TableModel model = tbl_book.getModel();
    }//GEN-LAST:event_tbl_bookMouseClicked

    private void btn_clearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_clearActionPerformed
        setBookDetailsToTable();
    }//GEN-LAST:event_btn_clearActionPerformed

    private void btn_searchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_searchActionPerformed
        searchBook();
    }//GEN-LAST:event_btn_searchActionPerformed

    private void tbl_bookKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbl_bookKeyPressed

    }//GEN-LAST:event_tbl_bookKeyPressed

    private void cbox_searchbookActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbox_searchbookActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbox_searchbookActionPerformed

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
        tbl_book.clearSelection();
    }//GEN-LAST:event_btn_CancleActionPerformed

    private void lbl_reservedtable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_reservedtable1MouseClicked
       BookStatus bookStatus = new BookStatus();
       bookStatus.setVisible(true);
       this.dispose();
    }//GEN-LAST:event_lbl_reservedtable1MouseClicked

    private void lbl_reservedtable1MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_reservedtable1MouseEntered
        lbl_reservedtable1.setText("<HTML><U>&lt;&lt; Back</U></HTML>");
    }//GEN-LAST:event_lbl_reservedtable1MouseEntered

    private void lbl_reservedtable1MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_reservedtable1MouseExited
       lbl_reservedtable1.setText("<< Back");
    }//GEN-LAST:event_lbl_reservedtable1MouseExited

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
            java.util.logging.Logger.getLogger(issuedBook.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(issuedBook.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(issuedBook.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(issuedBook.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
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
                new issuedBook().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private rojerusan.RSMaterialButtonRectangle btn_Cancle;
    private rojerusan.RSMaterialButtonRectangle btn_clear;
    private rojerusan.RSMaterialButtonRectangle btn_search;
    private javax.swing.JComboBox<String> cbox_searchbook;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbl_Close;
    private javax.swing.JLabel lbl_admin1;
    private javax.swing.JLabel lbl_reservedtable1;
    private rojeru_san.complementos.RSTableMetro tbl_book;
    private javax.swing.JTextField txt_searchbook;
    // End of variables declaration//GEN-END:variables
}
