/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jFrame;

import static jFrame.Validation.*;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.border.Border;

/**
 *
 * @author Senath Chandira
 */
public class IssueBook extends javax.swing.JFrame {

    String Book_ID, NIC, ISBN;

    /**
     * Creates new form IssueBook
     */
    Color white = new Color(255, 255, 255);
    Color lightgray = new Color(153, 153, 153);
    Border borderout = BorderFactory.createMatteBorder(0, 0, 1, 0, lightgray);
    Border border = BorderFactory.createMatteBorder(0, 0, 1, 0, white);

    public IssueBook() {
        initComponents();
        txt_BookName.setEditable(false);
        txt_Category.setEditable(false);
        txt_Name.setEditable(false);
        //Date_IssueDate.setEnabled(false);
    }

    // check
    public boolean checkAvailability1() throws SQLException {
        boolean canIssue = false;
        NIC = txt_NIC.getText();
        Book_ID = txt_ISBN.getText();

        try {
            Connection con = DBConnection.getConnection();
            con.setAutoCommit(false);

            String checkMemberSql = "SELECT COUNT(*) AS member_count FROM tbl_memberdetails WHERE NIC = ?";
            PreparedStatement checkMemberPst = con.prepareStatement(checkMemberSql);
            checkMemberPst.setString(1, NIC);
            ResultSet memberRs1 = checkMemberPst.executeQuery();

            if (memberRs1.next()) {
                int memberCount = memberRs1.getInt("member_count");
                if (memberCount > 0) {
                    // The NIC is registered in tbl_member
                } else {
                    // The NIC is not registered in tbl_member
                    JOptionPane.showMessageDialog(this, "Not a registered NIC", "", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            }

            // Check if the member already has two pending books
            String checkPendingBooksSql = "SELECT COUNT(*) AS pending_count FROM tbl_issuebook WHERE NIC = ? AND Return_date IS NULL";
            PreparedStatement checkPendingBooksPst = con.prepareStatement(checkPendingBooksSql);
            checkPendingBooksPst.setString(1, NIC);
            ResultSet pendingBooksRs = checkPendingBooksPst.executeQuery();

            int pendingCount = 0;

            if (pendingBooksRs.next()) {
                pendingCount = pendingBooksRs.getInt("pending_count");
            }

            if (pendingCount >= 2) {
                // The member already has two pending books
                JOptionPane.showMessageDialog(this, "The member already has two pending books. Cannot issue another book.", "", JOptionPane.WARNING_MESSAGE);
                clearTextbox();
                return false;
            }

            // Check availability of the book
            String checkBookAvailabilitySql = "SELECT Status FROM tbl_BookCopy WHERE Book_ID = ?";
            PreparedStatement checkBookAvailabilityPst = con.prepareStatement(checkBookAvailabilitySql);
            checkBookAvailabilityPst.setString(1, Book_ID);
            ResultSet bookAvailabilityRs = checkBookAvailabilityPst.executeQuery();

            if (bookAvailabilityRs.next()) {
                String status = bookAvailabilityRs.getString("Status");

                if (status.equals("Available")) {
                    // Book is available
                    // ...
                    String retrieveMemberSql = "SELECT Member_Name FROM tbl_memberdetails WHERE NIC = ?";
                    PreparedStatement retrieveMemberPst = con.prepareStatement(retrieveMemberSql);
                    retrieveMemberPst.setString(1, NIC);
                    ResultSet memberRs = retrieveMemberPst.executeQuery();

                    // Retrieve the ISBN from tbl_BookCopy
                    String retrieveISBNSql = "SELECT ISBN FROM tbl_BookCopy WHERE Book_ID = ? AND Status = 'Available'";
                    PreparedStatement retrieveISBnPst = con.prepareStatement(retrieveISBNSql);
                    retrieveISBnPst.setString(1, Book_ID);
                    ResultSet isbnRs = retrieveISBnPst.executeQuery();

                    if (memberRs.next() && isbnRs.next()) {
                        String memberName = memberRs.getString("Member_Name");
                        ISBN = isbnRs.getString("ISBN");

                        // Retrieve book details using the actual ISBN
                        String retrieveBookSql = "SELECT Book_name, Category FROM tbl_book WHERE ISBN = ?";
                        PreparedStatement retrieveBookPst = con.prepareStatement(retrieveBookSql);
                        retrieveBookPst.setString(1, ISBN);
                        ResultSet bookRs = retrieveBookPst.executeQuery();

                        if (bookRs.next()) {
                            String bookName = bookRs.getString("Book_name");
                            String category = bookRs.getString("Category");

                            // Display member and book details in the text fields
                            txt_Name.setText(memberName);
                            txt_BookName.setText(bookName);
                            txt_Category.setText(category);

                            // date calculation and set the return date
                            LocalDate currentLDate = LocalDate.now(ZoneId.systemDefault());
                            Date issueDate = Date.from(currentLDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                            Date_IssueDate.setDate(issueDate);

                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(issueDate);
                            calendar.add(Calendar.WEEK_OF_YEAR, 2);
                            Date returnDate = calendar.getTime();

                            // Set the return date in the Date_IssueDate field
                            Date_ReturnDate.setDate(returnDate);

                            canIssue = true;
                        }
                    }
                } else if (status.equals("Reserved")) {
                    // Check if the book is reserved by the member

                    // Retrieve the ISBN from tbl_BookCopy
                    String retrieveISBNSql = "SELECT ISBN FROM tbl_BookCopy WHERE Book_ID = ? AND Status = 'Reserved'";
                    PreparedStatement retrieveISBnPst = con.prepareStatement(retrieveISBNSql);
                    retrieveISBnPst.setString(1, Book_ID);
                    ResultSet isbnRs = retrieveISBnPst.executeQuery();

                    if (isbnRs.next()) {
                        ISBN = isbnRs.getString("ISBN");
                        System.out.println("NIC" + NIC);
                        System.out.println("ISBN" + ISBN);

                        // Check if the book is reserved by the member
                        String checkReservationSql = "SELECT COUNT(*) AS reservation_count FROM tbl_reservedbook WHERE ISBN = ? AND NIC = ? AND Reservation_Status = 'Reserved'";
                        PreparedStatement checkReservationPst = con.prepareStatement(checkReservationSql);
                        checkReservationPst.setString(1, ISBN);
                        checkReservationPst.setString(2, NIC);
                        ResultSet reservationRs = checkReservationPst.executeQuery();

                        if (reservationRs.next()) {
                            int reservationCount = reservationRs.getInt("reservation_count");
                            System.out.println("reservationCount " + reservationCount);
                            if (reservationCount > 0) {
                                // The member has reserved the book
                                // The member who reserved the book is trying to issue it
                                // ...
                                String retrieveMemberSql = "SELECT Member_Name FROM tbl_memberdetails WHERE NIC = ?";
                                PreparedStatement retrieveMemberPst = con.prepareStatement(retrieveMemberSql);
                                retrieveMemberPst.setString(1, NIC);
                                ResultSet memberRs = retrieveMemberPst.executeQuery();

                                // Retrieve book details using the actual ISBN
                                String retrieveBookSql = "SELECT Book_name, Category FROM tbl_book WHERE ISBN = ?";
                                PreparedStatement retrieveBookPst = con.prepareStatement(retrieveBookSql);
                                retrieveBookPst.setString(1, ISBN);
                                ResultSet bookRs = retrieveBookPst.executeQuery();

                                if (memberRs.next() && bookRs.next()) {
                                    String memberName = memberRs.getString("Member_Name");
                                    String bookName = bookRs.getString("Book_name");
                                    String category = bookRs.getString("Category");

                                    // Display member and book details in the text fields
                                    txt_Name.setText(memberName);
                                    txt_BookName.setText(bookName);
                                    txt_Category.setText(category);

                                    // date calculation and set the return date
                                    LocalDate currentLDate = LocalDate.now(ZoneId.systemDefault());

                                    Date issueDate = Date.from(currentLDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                                    Date_IssueDate.setDate(issueDate);

                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTime(issueDate);
                                    calendar.add(Calendar.WEEK_OF_YEAR, 2);
                                    Date returnDate = calendar.getTime();

                                    // Set the return date in the Date_IssueDate field
                                    Date_ReturnDate.setDate(returnDate);

                                    canIssue = true;
                                } else {
                                    // The member has not reserved the book
                                    JOptionPane.showMessageDialog(this, "The requested book is reserved by another member.", "", JOptionPane.WARNING_MESSAGE);
                                    return false;
                                }
                            } else {
                                JOptionPane.showMessageDialog(this, "The requested book is reserved by another member.", "", JOptionPane.WARNING_MESSAGE);
                                return false;
                            }
                        } else {
                            System.out.println("2");
                            return false;
                        }
                    }

                } else {
                    // The requested book is reserved by another member
                    JOptionPane.showMessageDialog(this, "The requested book is not available", "", JOptionPane.WARNING_MESSAGE);
                    return false;
                }

                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return canIssue;
    }

    

    //get the BookCopy_ID of an available book
    private String getAvailableCopyID(String Book_ID) {
        try {
            Connection con = DBConnection.getConnection();

            String selectIsbnSql = "SELECT ISBN FROM tbl_bookcopy WHERE Book_ID = ?";
            PreparedStatement selectIsbnPst = con.prepareStatement(selectIsbnSql);
            selectIsbnPst.setString(1, Book_ID);
            ResultSet selectIsbnRs = selectIsbnPst.executeQuery();

            if (selectIsbnRs.next()) {
                String ISBN = selectIsbnRs.getString("ISBN");

                String selectCopyIdSql = "SELECT Book_ID FROM tbl_bookcopy WHERE ISBN = ? AND Status = 'Available' LIMIT 1";
                PreparedStatement selectCopyIdPst = con.prepareStatement(selectCopyIdSql);
                selectCopyIdPst.setString(1, ISBN);
                ResultSet selectCopyIdRs = selectCopyIdPst.executeQuery();

                if (selectCopyIdRs.next()) {
                    return selectCopyIdRs.getString("Book_ID");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean issueBook() {
        boolean isIssued = false;
        String NIC = txt_NIC.getText();
        Book_ID = txt_ISBN.getText();
        Date issDateUtil = Date_IssueDate.getDate();
        Date returnDateUtil = Date_ReturnDate.getDate();

        long l1 = issDateUtil.getTime();
        java.sql.Date issDatesql = new java.sql.Date(l1);
        long l2 = returnDateUtil.getTime();
        java.sql.Date returnDatesql = new java.sql.Date(l2);

        try {
            Connection con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // Update the status of the book copy to 'Pending'
            //String bookCopyID = getAvailableCopyID(ISBN);
            //if (bookCopyID != null) {
            String updateBookCopyStatusSql = "UPDATE tbl_bookcopy SET Status = 'Pending' WHERE Book_ID = ?";
            PreparedStatement updateBookCopyStatusPst = con.prepareStatement(updateBookCopyStatusSql);
            updateBookCopyStatusPst.setString(1, Book_ID);
            updateBookCopyStatusPst.executeUpdate();

            // Update the issue table
            String insertIssueSql = "INSERT INTO tbl_issuebook(NIC, Issue_Date, Due_Date, Book_ID) VALUES (?, ?, ?, ?)";
            PreparedStatement insertIssuePst = con.prepareStatement(insertIssueSql);
            insertIssuePst.setString(1, NIC);
            insertIssuePst.setDate(2, issDatesql);
            insertIssuePst.setDate(3, returnDatesql);
            insertIssuePst.setString(4, Book_ID);
            insertIssuePst.executeUpdate();

            String retrieveISBNSql = "SELECT ISBN FROM tbl_BookCopy WHERE Book_ID = ?";
            PreparedStatement retrieveISBnPst = con.prepareStatement(retrieveISBNSql);
            retrieveISBnPst.setString(1, Book_ID);
            ResultSet isbnRs = retrieveISBnPst.executeQuery();

            if (isbnRs.next()) {
                ISBN = isbnRs.getString("ISBN");
            }

            String cancelReservationSql = "UPDATE tbl_reservedbook SET Reservation_Status = 'Cancelled' WHERE ISBN = ? AND NIC = ? AND Reservation_Status = 'Reserved'";
            PreparedStatement cancelReservationPst = con.prepareStatement(cancelReservationSql);
            cancelReservationPst.setString(1, ISBN);
            cancelReservationPst.setString(2, NIC);
            cancelReservationPst.executeUpdate();

            con.commit();
            isIssued = true;
            //}
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isIssued;
    }

    public boolean isValidInput() {
        boolean valid;
        test t = new test();
        Book_ID = txt_ISBN.getText();
        NIC = txt_NIC.getText();
        if (txt_ISBN.getText().trim().isEmpty()
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

    public boolean isAvailable() {
        boolean valid;
        if (txt_BookName.getText().trim().isEmpty()
                || txt_Category.getText().trim().isEmpty()
                || txt_Name.getText().trim().isEmpty()
                || Date_IssueDate == null) {
            JOptionPane.showMessageDialog(this, "Check availability first", "", JOptionPane.WARNING_MESSAGE);
            valid = false;
        } else {
            valid = true;
        }

        return valid;
    }

    public void clearTextbox() {
        txt_BookName.setText("");
        txt_Name.setText("");
        txt_Category.setText("");
        Date_IssueDate.setDate(null);
        Date_ReturnDate.setDate(null);

    }

    public void clearAllTextbox() {
        txt_BookName.setText("");
        txt_Name.setText("");
        txt_Category.setText("");
        txt_ISBN.setText("");
        txt_NIC.setText("");
        Date_IssueDate.setDate(null);
        Date_ReturnDate.setDate(null);

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
        txt_ISBN = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txt_Category = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        Date_ReturnDate = new com.toedter.calendar.JDateChooser();
        Date_IssueDate = new com.toedter.calendar.JDateChooser();
        btn_Check = new rojerusan.RSMaterialButtonRectangle();
        txt_NIC = new javax.swing.JTextField();
        txt_Name = new javax.swing.JTextField();
        btn_Add6 = new rojerusan.RSMaterialButtonRectangle();
        btn_Ok = new rojerusan.RSMaterialButtonRectangle();
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
        jLabel5.setText("BookID");
        jPanel3.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 110, 30));

        txt_ISBN.setBackground(new java.awt.Color(0, 51, 102));
        txt_ISBN.setFont(new java.awt.Font("Segoe UI Semibold", 0, 16)); // NOI18N
        txt_ISBN.setForeground(new java.awt.Color(204, 204, 204));
        txt_ISBN.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));
        txt_ISBN.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txt_ISBNFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txt_ISBNFocusLost(evt);
            }
        });
        jPanel3.add(txt_ISBN, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 70, 270, 30));

        jLabel6.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Book Name");
        jPanel3.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, 110, 30));

        jLabel7.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Category");
        jPanel3.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 220, 110, 30));

        txt_Category.setBackground(new java.awt.Color(0, 51, 102));
        txt_Category.setFont(new java.awt.Font("Segoe UI Semibold", 0, 16)); // NOI18N
        txt_Category.setForeground(new java.awt.Color(204, 204, 204));
        txt_Category.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));
        jPanel3.add(txt_Category, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 220, 270, 30));

        jLabel8.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Issue Date");
        jPanel3.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 270, 110, 30));

        jLabel9.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Return date");
        jPanel3.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 320, 110, 30));

        Date_ReturnDate.setFont(new java.awt.Font("Segoe UI Semibold", 0, 16)); // NOI18N
        jPanel3.add(Date_ReturnDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 320, 270, 30));

        Date_IssueDate.setFont(new java.awt.Font("Segoe UI Semibold", 0, 16)); // NOI18N
        Date_IssueDate.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                Date_IssueDatePropertyChange(evt);
            }
        });
        jPanel3.add(Date_IssueDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 270, 270, 30));

        btn_Check.setBackground(new java.awt.Color(0, 153, 255));
        btn_Check.setText("Check");
        btn_Check.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        btn_Check.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_CheckActionPerformed(evt);
            }
        });
        jPanel3.add(btn_Check, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 400, 120, 60));

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

        btn_Add6.setBackground(new java.awt.Color(0, 153, 255));
        btn_Add6.setText("Cancle");
        btn_Add6.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        btn_Add6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_Add6ActionPerformed(evt);
            }
        });
        jPanel3.add(btn_Add6, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 400, 120, 60));

        btn_Ok.setBackground(new java.awt.Color(0, 153, 255));
        btn_Ok.setText("Issue");
        btn_Ok.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        btn_Ok.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_OkActionPerformed(evt);
            }
        });
        jPanel3.add(btn_Ok, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 400, 120, 60));

        jPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 60, 470, 490));

        jPanel2.setBackground(new java.awt.Color(0, 0, 0));
        jPanel2.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 0, 1, new java.awt.Color(0, 0, 0)));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/bookDetailsGray.png"))); // NOI18N
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 70, 60));

        jLabel2.setFont(new java.awt.Font("Arial", 1, 16)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Issue Book");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 0, 110, 60));

        lbl_Close.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/icons8-close-20.png"))); // NOI18N
        lbl_Close.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbl_CloseMouseClicked(evt);
            }
        });
        jPanel2.add(lbl_Close, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 10, 20, 20));

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 470, 60));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 470, 550));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void Date_IssueDatePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_Date_IssueDatePropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_Date_IssueDatePropertyChange

    private void lbl_CloseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_CloseMouseClicked
        this.dispose();
    }//GEN-LAST:event_lbl_CloseMouseClicked

    private void btn_CheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_CheckActionPerformed
        if (isValidInput()) {
            try {
                if (checkAvailability1()) {
                    //JOptionPane.showMessageDialog(this, "The member does not have any pending books.", "", JOptionPane.WARNING_MESSAGE);
                } else {
                    //JOptionPane.showMessageDialog(this, "Book Issuing Fail", "", JOptionPane.WARNING_MESSAGE);
                }
            } catch (SQLException ex) {
                Logger.getLogger(IssueBook.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_btn_CheckActionPerformed

    private void btn_Add6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_Add6ActionPerformed
        clearAllTextbox();
    }//GEN-LAST:event_btn_Add6ActionPerformed

    private void btn_OkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_OkActionPerformed
        if (isValidInput() && isAvailable()) {
            try {
                if (checkAvailability1()) {
                    if (issueBook()) {
                        JOptionPane.showMessageDialog(this, "Book issued successfully", "", JOptionPane.INFORMATION_MESSAGE);

                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(IssueBook.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }

    }//GEN-LAST:event_btn_OkActionPerformed

    private void txt_NICFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_NICFocusGained
        txt_NIC.setBorder(border);
    }//GEN-LAST:event_txt_NICFocusGained

    private void txt_ISBNFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_ISBNFocusGained
        txt_ISBN.setBorder(border);
    }//GEN-LAST:event_txt_ISBNFocusGained

    private void txt_NICFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_NICFocusLost
        txt_NIC.setBorder(borderout);
    }//GEN-LAST:event_txt_NICFocusLost

    private void txt_ISBNFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_ISBNFocusLost
        txt_ISBN.setBorder(borderout);
    }//GEN-LAST:event_txt_ISBNFocusLost

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
            java.util.logging.Logger.getLogger(IssueBook.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(IssueBook.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(IssueBook.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(IssueBook.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new IssueBook().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.toedter.calendar.JDateChooser Date_IssueDate;
    private com.toedter.calendar.JDateChooser Date_ReturnDate;
    private rojerusan.RSMaterialButtonRectangle btn_Add6;
    private rojerusan.RSMaterialButtonRectangle btn_Check;
    private rojerusan.RSMaterialButtonRectangle btn_Ok;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel lbl_Close;
    private javax.swing.JTextField txt_BookName;
    private javax.swing.JTextField txt_Category;
    private javax.swing.JTextField txt_ISBN;
    private javax.swing.JTextField txt_NIC;
    private javax.swing.JTextField txt_Name;
    // End of variables declaration//GEN-END:variables
}
