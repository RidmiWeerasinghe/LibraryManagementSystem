/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jFrame;

import static jFrame.DBConnection.con;
import static jFrame.Validation.validateBookID;
import static jFrame.Validation.validateNIC;
import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Senath Chandira
 */
public class ReturnBook extends javax.swing.JFrame {

    /**
     * Creates new form IssueBook
     */
    Color white = new Color(255, 255, 255);
    Color lightgray = new Color(153, 153, 153);
    Border borderout = BorderFactory.createMatteBorder(0, 0, 1, 0, lightgray);
    Border border = BorderFactory.createMatteBorder(0, 0, 1, 0, white);

    public ReturnBook() {
        initComponents();
    }

    String NIC, BookID, MName, BName, ISBN;
    double fine;
    Date issueDate, dueDate;

    public double calculateFine(int daydiff) {
        double fine = 0;
        double ChargePerDay = 5;
        fine = daydiff * ChargePerDay;
        return fine;
    }

    public void setFine() throws SQLException {
        String NIC = txt_NIC.getText();
        String BookID = txt_BookID.getText();
        Date utilDueDate = null;
        Date utilIssueDate = null;
        int intValueDayDiff = 0;
        LocalDate currentLDateLD = LocalDate.now(ZoneId.systemDefault());
        Date returnDate = Date.from(currentLDateLD.atStartOfDay(ZoneId.systemDefault()).toInstant());

        try {
            Connection con = DBConnection.getConnection();
            String selectQuantitySql = "SELECT Due_Date, Issue_Date FROM tbl_issuebook WHERE NIC=? AND Book_ID=?";
            PreparedStatement selectQuantityPst = con.prepareStatement(selectQuantitySql);
            selectQuantityPst.setString(1, NIC);
            selectQuantityPst.setString(2, BookID);
            ResultSet rs = selectQuantityPst.executeQuery();

            if (rs.next()) {
                java.sql.Date sqlDueDate = rs.getDate("Due_Date");
                utilDueDate = new java.util.Date(sqlDueDate.getTime());
                java.sql.Date sqlissDate = rs.getDate("Issue_Date");
                utilIssueDate = new java.util.Date(sqlissDate.getTime());

                String selectQuantitySql1 = "SELECT ISBN FROM tbl_bookcopy WHERE Book_ID=?";
                PreparedStatement selectQuantityPst1 = con.prepareStatement(selectQuantitySql1);
                selectQuantityPst1.setString(1, BookID);
                ResultSet rs1 = selectQuantityPst1.executeQuery();

                if (rs1.next()) {
                    ISBN = rs1.getString("ISBN");
                    //System.out.println("isbn " + ISBN);
                    String selectQuantitySql2 = "SELECT Book_Name FROM tbl_book WHERE ISBN=?";
                    PreparedStatement selectQuantityPst2 = con.prepareStatement(selectQuantitySql2);
                    selectQuantityPst2.setString(1, ISBN);
                    ResultSet rs2 = selectQuantityPst2.executeQuery();

                    if (rs2.next()) {
                        BName = rs2.getString("Book_Name");
                        //System.out.println("isbn " + ISBN);
                    }
                }
                String selectQuantitySql3 = "SELECT Member_Name FROM tbl_memberdetails WHERE NIC=?";
                PreparedStatement selectQuantityPst3 = con.prepareStatement(selectQuantitySql3);
                selectQuantityPst3.setString(1, NIC);
                ResultSet rs3 = selectQuantityPst3.executeQuery();

                if (rs3.next()) {
                    MName = rs3.getString("Member_Name");
                }
                if (utilDueDate.before(returnDate)) {
                    // Calculate the day difference
                    long differenceInMillis = returnDate.getTime() - utilDueDate.getTime();
                    long differenceInDays = TimeUnit.MILLISECONDS.toDays(differenceInMillis);
                    intValueDayDiff = (int) differenceInDays;
                    //System.out.println("The book is overdue by " + differenceInDays + " days.");
                    fine = calculateFine(intValueDayDiff);
                    txt_Fine.setText(Double.toString(fine));
                    txt_overdueDays.setText(Integer.toString(intValueDayDiff));
                    Date_Issue.setDate(utilIssueDate);
                    Date_dueDate.setDate(utilDueDate);
                    txt_BookName.setText(BName);
                    txt_Name.setText(MName);

                } else {
                    txt_Fine.setText("0");
                    txt_overdueDays.setText("Not over due");
                    Date_Issue.setDate(utilIssueDate);
                    Date_dueDate.setDate(utilDueDate);
                    txt_Name.setText(MName);
                    txt_BookName.setText(BName);
                }

            } else {
                JOptionPane.showMessageDialog(this, "No record found", "", JOptionPane.WARNING_MESSAGE);
            }

            // Close the database resources
            rs.close();
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean validation() {
        NIC = txt_NIC.getText();
        BookID = txt_BookName.getText();
        test t = new test();

        if (NIC.trim().isEmpty() && BookID.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "NIC and Book ID fields can not be empty", "", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (NIC.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "NIC can not be empty", "", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (BookID.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Book ID can not be empty", "", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!t.isNumber(NIC) || !(NIC.length() == 12 || NIC.length() == 9)) {
            JOptionPane.showMessageDialog(this, "Please enter valid NIC", "", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!(BookID.length() == 15 || BookID.length() == 12)) {
            JOptionPane.showMessageDialog(this, "Please enter valid Book ID", "", JOptionPane.WARNING_MESSAGE);
            return false;
        } else {
            return true;
        }
    }

    public boolean ok() {
        LocalDate currentLDateLD = LocalDate.now(ZoneId.systemDefault());
        java.sql.Date returnDate = java.sql.Date.valueOf(currentLDateLD);
        fine = Double.parseDouble(txt_Fine.getText());
        BookID = txt_BookID.getText();

        try {
            Connection con = DBConnection.getConnection();

            // Retrieve the ISBN of the book from tbl_bookcopy using Book_ID
            String retrieveISBNSql = "SELECT ISBN FROM tbl_bookcopy WHERE Book_ID = ?";
            PreparedStatement retrieveISBnPst = con.prepareStatement(retrieveISBNSql);
            retrieveISBnPst.setString(1, BookID);
            ResultSet isbnRs = retrieveISBnPst.executeQuery();

            if (isbnRs.next()) {
                String ISBN = isbnRs.getString("ISBN");

                // Check the number of reserved books in tbl_reservedbook with status "Reserved" for the relevant ISBN
                String checkReservedBooksSql = "SELECT COUNT(*) AS reserved_count FROM tbl_reservedbook WHERE ISBN = ? AND Reservation_Status = 'Reserved'";
                PreparedStatement checkReservedBooksPst = con.prepareStatement(checkReservedBooksSql);
                checkReservedBooksPst.setString(1, ISBN);
                ResultSet reservedBooksRs = checkReservedBooksPst.executeQuery();

                // Check the number of books in tbl_bookcopy with status "Reserved" for the relevant ISBN
                String checkBookCopyStatusSql = "SELECT COUNT(*) AS reserved_count FROM tbl_bookcopy WHERE ISBN = ? AND Status = 'Reserved'";
                PreparedStatement checkBookCopyStatusPst = con.prepareStatement(checkBookCopyStatusSql);
                checkBookCopyStatusPst.setString(1, ISBN);
                ResultSet bookCopyStatusRs = checkBookCopyStatusPst.executeQuery();

                int reservedCount = 0;
                int bookCopyReservedCount = 0;

                if (reservedBooksRs.next()) {
                    reservedCount = reservedBooksRs.getInt("reserved_count");
                }

                if (bookCopyStatusRs.next()) {
                    bookCopyReservedCount = bookCopyStatusRs.getInt("reserved_count");
                }

                if (reservedCount > bookCopyReservedCount) {
                    // Book is reserved, update the status in tbl_bookcopy to "Reserved"
                    String updateStatusSql = "UPDATE tbl_bookcopy SET Status = 'Reserved' WHERE Book_ID = ?";
                    PreparedStatement updateStatusPst = con.prepareStatement(updateStatusSql);
                    updateStatusPst.setString(1, BookID);
                    updateStatusPst.executeUpdate();
                } else {
                    // Book is not reserved, update the status in tbl_bookcopy to "Available"
                    String updateStatusSql = "UPDATE tbl_bookcopy SET Status = 'Available' WHERE Book_ID = ?";
                    PreparedStatement updateStatusPst = con.prepareStatement(updateStatusSql);
                    updateStatusPst.setString(1, BookID);
                    updateStatusPst.executeUpdate();
                }

                // Update return date and fine in tbl_issuebook
                String updateIssueBookSql = "UPDATE tbl_issuebook SET Return_Date=?, Fines=? WHERE Book_ID = ?";
                PreparedStatement pst = con.prepareStatement(updateIssueBookSql);
                pst.setDate(1, returnDate);
                pst.setDouble(2, fine);
                pst.setString(3, BookID);
                pst.executeUpdate();

                // Close the database resources
                isbnRs.close();
                retrieveISBnPst.close();
                reservedBooksRs.close();
                checkReservedBooksPst.close();
                bookCopyStatusRs.close();
                checkBookCopyStatusPst.close();
                pst.close();
                con.close();
                return true;
            } else {
                // No book found with the given Book_ID
                JOptionPane.showMessageDialog(this, "Invalid Book ID.", "", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isValidInput() {
        boolean valid;
        test t = new test();
        String Book_ID = txt_BookID.getText();
        NIC = txt_NIC.getText();
        if (txt_BookID.getText().trim().isEmpty()
                || txt_NIC.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "NIC and Book ID fiels must be filled out", "", JOptionPane.WARNING_MESSAGE);
            valid = false;
        } else if (!validateBookID(Book_ID)) {
            JOptionPane.showMessageDialog(this, "please enter valid Book ID", "", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!validateNIC(NIC)) {
            JOptionPane.showMessageDialog(this, "please enter valid NIC", "", JOptionPane.WARNING_MESSAGE);
            valid = false;
        } else {
            valid = true;
        }

        return valid;
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
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        txt_BookName = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txt_BookID = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txt_overdueDays = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        Date_Issue = new com.toedter.calendar.JDateChooser();
        btn_checkFine = new rojerusan.RSMaterialButtonRectangle();
        txt_NIC = new javax.swing.JTextField();
        txt_Name = new javax.swing.JTextField();
        btn_Cancle = new rojerusan.RSMaterialButtonRectangle();
        jLabel10 = new javax.swing.JLabel();
        txt_Fine = new javax.swing.JTextField();
        btn_ok = new rojerusan.RSMaterialButtonRectangle();
        jLabel11 = new javax.swing.JLabel();
        Date_dueDate = new com.toedter.calendar.JDateChooser();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        lbl_Close = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setForeground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel3.setBackground(new java.awt.Color(0, 51, 102));
        jPanel3.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 1, 1, 1, new java.awt.Color(0, 0, 0)));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("NIC");
        jPanel3.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 110, 30));

        txt_BookName.setBackground(new java.awt.Color(0, 51, 102));
        txt_BookName.setFont(new java.awt.Font("Segoe UI Semibold", 0, 16)); // NOI18N
        txt_BookName.setForeground(new java.awt.Color(204, 204, 204));
        txt_BookName.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));
        jPanel3.add(txt_BookName, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 170, 270, 30));

        jLabel4.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Member Name");
        jLabel4.setToolTipText("");
        jPanel3.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, 110, 30));

        jLabel5.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Book Id");
        jPanel3.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 110, 30));

        txt_BookID.setBackground(new java.awt.Color(0, 51, 102));
        txt_BookID.setFont(new java.awt.Font("Segoe UI Semibold", 0, 16)); // NOI18N
        txt_BookID.setForeground(new java.awt.Color(204, 204, 204));
        txt_BookID.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));
        txt_BookID.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txt_BookIDFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txt_BookIDFocusLost(evt);
            }
        });
        jPanel3.add(txt_BookID, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 70, 270, 30));

        jLabel6.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Book Name");
        jPanel3.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, 110, 30));

        txt_overdueDays.setBackground(new java.awt.Color(0, 51, 102));
        txt_overdueDays.setFont(new java.awt.Font("Segoe UI Semibold", 0, 16)); // NOI18N
        txt_overdueDays.setForeground(new java.awt.Color(204, 204, 204));
        txt_overdueDays.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));
        jPanel3.add(txt_overdueDays, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 330, 270, 30));

        jLabel8.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("overdue days");
        jPanel3.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 330, 110, 30));

        jLabel9.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Issue date");
        jPanel3.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 230, 110, 30));

        Date_Issue.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                Date_IssuePropertyChange(evt);
            }
        });
        jPanel3.add(Date_Issue, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 230, 270, 30));

        btn_checkFine.setBackground(new java.awt.Color(0, 153, 255));
        btn_checkFine.setText("Check Fine");
        btn_checkFine.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        btn_checkFine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_checkFineActionPerformed(evt);
            }
        });
        jPanel3.add(btn_checkFine, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 450, 120, 60));

        txt_NIC.setBackground(new java.awt.Color(0, 51, 102));
        txt_NIC.setFont(new java.awt.Font("Segoe UI Semibold", 0, 16)); // NOI18N
        txt_NIC.setForeground(new java.awt.Color(204, 204, 204));
        txt_NIC.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));
        txt_NIC.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txt_NICFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txt_NICFocusLost(evt);
            }
        });
        jPanel3.add(txt_NIC, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 20, 270, 30));

        txt_Name.setBackground(new java.awt.Color(0, 51, 102));
        txt_Name.setFont(new java.awt.Font("Segoe UI Semibold", 0, 16)); // NOI18N
        txt_Name.setForeground(new java.awt.Color(204, 204, 204));
        txt_Name.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));
        jPanel3.add(txt_Name, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 120, 270, 30));

        btn_Cancle.setBackground(new java.awt.Color(0, 153, 255));
        btn_Cancle.setText("Cancle");
        btn_Cancle.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        btn_Cancle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_CancleActionPerformed(evt);
            }
        });
        jPanel3.add(btn_Cancle, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 450, 120, 60));

        jLabel10.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("Fine");
        jPanel3.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 380, 110, 30));

        txt_Fine.setBackground(new java.awt.Color(0, 51, 102));
        txt_Fine.setFont(new java.awt.Font("Segoe UI Semibold", 0, 16)); // NOI18N
        txt_Fine.setForeground(new java.awt.Color(204, 204, 204));
        txt_Fine.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));
        jPanel3.add(txt_Fine, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 380, 270, 30));

        btn_ok.setBackground(new java.awt.Color(0, 153, 255));
        btn_ok.setText("OK");
        btn_ok.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        btn_ok.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_okActionPerformed(evt);
            }
        });
        jPanel3.add(btn_ok, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 450, 120, 60));

        jLabel11.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("Due date");
        jPanel3.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 280, 110, 30));

        Date_dueDate.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                Date_dueDatePropertyChange(evt);
            }
        });
        jPanel3.add(Date_dueDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 280, 270, 30));

        jPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 60, 470, 530));

        jPanel2.setBackground(new java.awt.Color(0, 0, 0));
        jPanel2.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 0, 1, new java.awt.Color(0, 0, 0)));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/icons8-return-40.png"))); // NOI18N
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 60, 60));

        jLabel2.setFont(new java.awt.Font("Arial", 1, 16)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Return Book");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 0, 110, 60));

        lbl_Close.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/icons8-close-20.png"))); // NOI18N
        lbl_Close.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbl_CloseMouseClicked(evt);
            }
        });
        jPanel2.add(lbl_Close, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 10, 20, 20));

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 470, 60));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 470, -1));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void Date_IssuePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_Date_IssuePropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_Date_IssuePropertyChange

    private void lbl_CloseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_CloseMouseClicked
        this.dispose();
    }//GEN-LAST:event_lbl_CloseMouseClicked

    private void btn_checkFineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_checkFineActionPerformed
        if (isValidInput()) {
            try {
                setFine();
            } catch (SQLException ex) {
                Logger.getLogger(ReturnBook.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }//GEN-LAST:event_btn_checkFineActionPerformed

    private void btn_CancleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_CancleActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btn_CancleActionPerformed

    private void btn_okActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_okActionPerformed
        if (isValidInput()) {
            if (ok()) {
                JOptionPane.showMessageDialog(this, "Book returned Successfully", "", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Book returned fail", "", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }//GEN-LAST:event_btn_okActionPerformed

    private void Date_dueDatePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_Date_dueDatePropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_Date_dueDatePropertyChange

    private void txt_BookIDFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_BookIDFocusLost
        txt_BookID.setBorder(borderout);
    }//GEN-LAST:event_txt_BookIDFocusLost

    private void txt_BookIDFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_BookIDFocusGained
        txt_BookID.setBorder(border);
    }//GEN-LAST:event_txt_BookIDFocusGained

    private void txt_NICFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_NICFocusGained
        txt_NIC.setBorder(border);
    }//GEN-LAST:event_txt_NICFocusGained

    private void txt_NICFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_NICFocusLost
        txt_NIC.setBorder(borderout);
    }//GEN-LAST:event_txt_NICFocusLost

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
            java.util.logging.Logger.getLogger(ReturnBook.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ReturnBook.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ReturnBook.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ReturnBook.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ReturnBook().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.toedter.calendar.JDateChooser Date_Issue;
    private com.toedter.calendar.JDateChooser Date_dueDate;
    private rojerusan.RSMaterialButtonRectangle btn_Cancle;
    private rojerusan.RSMaterialButtonRectangle btn_checkFine;
    private rojerusan.RSMaterialButtonRectangle btn_ok;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel lbl_Close;
    private javax.swing.JTextField txt_BookID;
    private javax.swing.JTextField txt_BookName;
    private javax.swing.JTextField txt_Fine;
    private javax.swing.JTextField txt_NIC;
    private javax.swing.JTextField txt_Name;
    private javax.swing.JTextField txt_overdueDays;
    // End of variables declaration//GEN-END:variables
}
