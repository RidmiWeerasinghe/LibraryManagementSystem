/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jFrame;

import com.mysql.cj.Constants;
import com.toedter.calendar.demo.BirthdayEvaluator;
import diu.swe.habib.JavaToast.JToast;
import static jFrame.Validation.*;
import java.awt.Color;
//email
import static java.awt.PageAttributes.MediaType.A;

import java.sql.Connection;
import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class ManageMember extends javax.swing.JFrame {

    /**
     * Creates new form Members
     */
    String Member_Name, Address, Email, Gender, Age, Contact_no, Username, Password, NIC;
    Date Date_of_birth;
    int Member_ID;

    //designs
    Color white = new Color(255, 255, 255);
    Color lightgray = new Color(153, 153, 153);
    Border borderout = BorderFactory.createMatteBorder(0, 0, 1, 0, lightgray);
    Border border = BorderFactory.createMatteBorder(0, 0, 1, 0, white);
    DefaultTableModel model;

    public ManageMember() {
        initComponents();
        setMemberDetailsToTable();
        //txt_MemberID.setEditable(false);
        txt_Age.setEditable(false);
    }

    public String checkGender() {
        String gen = "";
        if (rdo_Female.isSelected()) {
            gen = "Female";
        }
        if (rdo_Male.isSelected()) {
            gen = "Male";
        }
        return gen;
    }

    public static int setAge(Date dob) {
        LocalDate localDOB = dob.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate currentLDate = LocalDate.now(ZoneId.systemDefault());
        int age = Period.between(localDOB, currentLDate).getYears();

        //System.out.println(age);
        return age;
    }

    //set gender to database
    public void setGender(String gen) {
        if (gen.equals("Female")) {
            rdo_Female.setSelected(true);
            rdo_Male.setSelected(false);

        }
        if (gen.equals("Male")) {
            rdo_Male.setSelected(true);
            rdo_Female.setSelected(false);
        }
    }
    //to set member details to the table

    public void setMemberDetailsToTable() {
        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT NIC,Member_Name,Address,Email,Gender,Date_of_birth,Age,Contact_no FROM tbl_memberdetails");

            // Clear existing rows from the table
            DefaultTableModel model = (DefaultTableModel) tbl_Member.getModel();
            model.setRowCount(0);

            while (rs.next()) {
                String Member_IDt = rs.getString("NIC");
                String Member_Namet = rs.getString("Member_Name");
                String Addresst = rs.getString("Address");
                String Emailt = rs.getString("Email");
                String Gendert = rs.getString("Gender");
                String Date_of_birtht = rs.getString("Date_of_birth");
                String Aget = rs.getString("Age");
                String Contact_not = rs.getString("Contact_no");

                Object[] obj = {Member_IDt, Member_Namet, Addresst, Emailt, Gendert, Date_of_birtht, Aget, Contact_not};
                model.addRow(obj);
            }

            // Close the database resources
            rs.close();
            st.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    public boolean NICExists(){
        try {
            Connection con = DBConnection.getConnection();
            String checkUserSql = "SELECT COUNT(*) FROM tbl_memberdetails WHERE NIC = ?";
            PreparedStatement checkUserPst = con.prepareStatement(checkUserSql);
            checkUserPst.setString(1, NIC);
            ResultSet checkUserRs = checkUserPst.executeQuery();
            if (checkUserRs.next()) {
                int userCount = checkUserRs.getInt(1);
                if (userCount > 0) {
                    // User with the same NIC already exists
                    JOptionPane.showMessageDialog(this, "User with the same NIC already exists");
                    return false;
                }
            
            }
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }*/
    //auto generate username password
    public boolean setUsernamePassword() throws SQLException {
        boolean usernameset = false;
        try {
            Connection con = DBConnection.getConnection();
            String checkUserSql = "SELECT COUNT(*) FROM tbl_user WHERE NIC = ?";
            PreparedStatement checkUserPst = con.prepareStatement(checkUserSql);
            checkUserPst.setString(1, NIC);
            ResultSet checkUserRs = checkUserPst.executeQuery();
            if (checkUserRs.next()) {
                int userCount = checkUserRs.getInt(1);
                if (userCount > 0) {
                    // User with the same NIC already exists
                    JOptionPane.showMessageDialog(this, "User with the same NIC already exists");
                    return false;
                }

            }
            new JToast().makeToast(this, "generating user credentials", 1);
            String NIC = txt_MemberID.getText();
            String username = "user" + NIC;
            String password = txt_MemberPhone.getText();
            System.out.println(username + " " + password);

            txt_username.setText(username);
            txt_password.setText(password);

            String sql = "INSERT INTO tbl_user (username, password, user_Type, NIC) VALUES (?, ?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, username);
            pst.setString(2, password);
            pst.setString(3, "Member");
            pst.setString(4, NIC);

            int rowCount = pst.executeUpdate();
            if (rowCount > 0) {
                // System.out.println("user added to the user table");
                usernameset = true;
            } else {
                // System.out.println("user did not added to the user table");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return usernameset;
    }

    //getting username password from database        //getting username password from database
    public void getUsernamePassword() {
        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            int rowIndex = tbl_Member.getSelectedRow();
            Object NICObj = tbl_Member.getValueAt(rowIndex, 0);
            String NIC = NICObj.toString();

            ResultSet rs = st.executeQuery("SELECT username, password FROM tbl_user WHERE NIC = '" + NIC + "'");

            Username = null;
            Password = null;

            if (rs.next()) {
                Username = rs.getString("username");
                Password = rs.getString("password");

                // Assign the retrieved values to the text fields
                txt_username.setText(Username);
                txt_password.setText(Password);
            } else {
                System.out.println("Invalid NIC");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //getting member id from member table
    public void setMemberID() {
        TableModel model = tbl_Member.getModel();
        int rowCount = model.getRowCount();
        int lastRowIndex = rowCount - 1;
        Object Member_IDobj = model.getValueAt(lastRowIndex, 0);
        //Member_ID = (Integer)Member_ID;
        String Member_IDString = Member_IDobj.toString();
        txt_MemberID.setText(Member_IDString);
    }

    //insert data
    public boolean addMember() {
        boolean isAdded = false;
        NIC = txt_MemberID.getText();
        Member_Name = txt_MemberName.getText();
        Address = txt_MemberAddress.getText();
        Email = txt_Email.getText();
        Gender = checkGender();
        Date_of_birth = date_Dob.getDate();
        Age = txt_Age.getText();
        Contact_no = txt_MemberPhone.getText();

        long l1 = Date_of_birth.getTime();
        java.sql.Date Date_of_birthsql = new java.sql.Date(l1);

        try {
            Connection con = DBConnection.getConnection();

            // Check if a user with the same NIC already exists
            String checkUserSql = "SELECT COUNT(*) FROM tbl_memberdetails WHERE NIC = ?";
            PreparedStatement checkUserPst = con.prepareStatement(checkUserSql);
            checkUserPst.setString(1, NIC);
            ResultSet checkUserRs = checkUserPst.executeQuery();
            if (checkUserRs.next()) {
                int userCount = checkUserRs.getInt(1);
                if (userCount > 0) {
                    // User with the same NIC already exists
                    isAdded = false;
                    JOptionPane.showMessageDialog(this, "User with the same NIC already exists");
                    return isAdded;
                }
            }

            // Insert the new user if it doesn't already exist
            String insertUserSql = "INSERT INTO tbl_memberdetails(NIC, Member_Name, Address, Email, Gender, Date_of_birth, Age, Contact_no) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement insertUserPst = con.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS);
            insertUserPst.setString(1, NIC);
            insertUserPst.setString(2, Member_Name);
            insertUserPst.setString(3, Address);
            insertUserPst.setString(4, Email);
            insertUserPst.setString(5, Gender);
            insertUserPst.setDate(6, Date_of_birthsql);
            insertUserPst.setString(7, Age);
            insertUserPst.setString(8, Contact_no);

            int rowCount = insertUserPst.executeUpdate();

            if (rowCount > 0) {
                isAdded = true;
                System.out.println("Member added to the member table");
            } else {
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return isAdded;
    }

    //update member detals
    public boolean updateMemberDetails() {
        boolean isUpdated = false;
        NIC = txt_MemberID.getText();
        Member_Name = txt_MemberName.getText();
        Address = txt_MemberAddress.getText();
        Email = txt_Email.getText();
        Gender = checkGender();
        Date_of_birth = date_Dob.getDate();
        Age = txt_Age.getText();
        Contact_no = txt_MemberPhone.getText();
        Username = txt_username.getText();
        Password = txt_password.getText();

        long l1 = Date_of_birth.getTime();
        java.sql.Date Date_of_birthsql = new java.sql.Date(l1);

        try {
            Connection con = DBConnection.getConnection();
/*
            String checkUserSql = "SELECT COUNT(*) FROM tbl_memberdetails WHERE NIC = ?";
            PreparedStatement checkUserPst = con.prepareStatement(checkUserSql);
            checkUserPst.setString(1, NIC);
            ResultSet checkUserRs = checkUserPst.executeQuery();
            if (checkUserRs.next()) {
                int userCount = checkUserRs.getInt(1);
                if (userCount > 0) {
                    // User with the same NIC already exists
                    JOptionPane.showMessageDialog(this, "User with the same NIC already exists");
                    return false;
                }

            }*/

            // Check if the username already exists
            String checkUsernameSql = "SELECT COUNT(*) AS username_count FROM tbl_user WHERE username = ? AND NIC != ?";
            PreparedStatement checkUsernamePst = con.prepareStatement(checkUsernameSql);
            checkUsernamePst.setString(1, Username);
            checkUsernamePst.setString(2, NIC);
            ResultSet usernameRs = checkUsernamePst.executeQuery();

            if (usernameRs.next()) {
                int usernameCount = usernameRs.getInt("username_count");

                if (usernameCount > 0) {
                    // Username already exists, display an error message or handle it as needed
                    JOptionPane.showMessageDialog(this, "Username already exists.", "", JOptionPane.WARNING_MESSAGE);
                    isUpdated = false;
                } else {
                    // Update tbl_memberdetails
                    String sql1 = "UPDATE tbl_memberdetails SET Member_Name = ?, Address = ?, Email = ?, Gender = ?, Date_of_birth = ?, Age = ?, Contact_no = ? WHERE NIC = ?";
                    PreparedStatement pst1 = con.prepareStatement(sql1);
                    pst1.setString(1, Member_Name);
                    pst1.setString(2, Address);
                    pst1.setString(3, Email);
                    pst1.setString(4, Gender);
                    pst1.setDate(5, Date_of_birthsql);
                    pst1.setInt(6, Integer.parseInt(Age));
                    pst1.setString(7, Contact_no);
                    pst1.setString(8, NIC);

                    int rowCount1 = pst1.executeUpdate();

                    // Update tbl_user
                    String sql2 = "UPDATE tbl_user SET username = ?, password = ? WHERE NIC = ?";
                    PreparedStatement pst2 = con.prepareStatement(sql2);
                    pst2.setString(1, Username);
                    pst2.setString(2, Password);
                    pst2.setString(3, NIC);

                    int rowCount2 = pst2.executeUpdate();

                    if (rowCount1 > 0 && rowCount2 > 0) {
                        isUpdated = true;
                    } else {
                        isUpdated = false;
                    }
                }
            }

            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isUpdated;
    }

    //search book
    public boolean deleteMember() {
        boolean isDeleted = false;
        NIC = txt_MemberID.getText();
        try {
            Connection con = DBConnection.getConnection();

            // Delete from tbl_memberdetails
            String sql1 = "DELETE FROM tbl_memberdetails WHERE NIC = ?";
            PreparedStatement pst1 = con.prepareStatement(sql1);
            pst1.setString(1, NIC);
            int rowCount1 = pst1.executeUpdate();

            if (rowCount1 > 0) {
                isDeleted = true;
            } else {
                isDeleted = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isDeleted;
    }

    public void clearTable() {
        DefaultTableModel model = (DefaultTableModel) tbl_Member.getModel();
        model.setRowCount(0);
    }

    //validation
    public boolean inputValidation() {
        boolean valid;
        test t = new test();
        NIC = txt_MemberID.getText();
        Date selectedDate = date_Dob.getDate();

        if (txt_MemberID.getText().isEmpty()
                || txt_MemberName.getText().trim().isEmpty()
                || txt_MemberAddress.getText().trim().isEmpty()
                || txt_Email.getText().trim().isEmpty()
                || (!rdo_Female.isSelected() && !rdo_Male.isSelected())
                || selectedDate == null
                || txt_MemberPhone.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled out", "", JOptionPane.WARNING_MESSAGE);
            valid = false;
        } /*else if (!t.isNumber(txt_MemberID.getText())) {
            JOptionPane.showMessageDialog(this,"NIC should only contain numbers");
            return false;
        }*/ else if (!validateNIC(NIC)) {
            JOptionPane.showMessageDialog(this, "please enter valid NIC", "", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (/*!txt_Email.getText().matches("^.+@.+\\..+$"*/!emailValidation(txt_Email.getText())) {
            JOptionPane.showMessageDialog(this, "please enter valid email", "", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!validatePhoneNumber(txt_MemberPhone.getText())) {
            JOptionPane.showMessageDialog(this, "please enter valid contact number", "", JOptionPane.WARNING_MESSAGE);
            return false;
        } else {
            valid = true;
        }

        return valid;
    }

    public void clearTextBoxes() {
        txt_MemberID.setText("");
        txt_MemberName.setText("");
        txt_MemberAddress.setText("");
        txt_Age.setText("");
        txt_Email.setText("");
        txt_MemberPhone.setText("");
        rdo_Female.setSelected(false);
        rdo_Male.setSelected(false);
        date_Dob.setCalendar(null);
        txt_username.setText("");
        txt_password.setText("");
    }

    public void searchBook() {
        Pattern pattern = Pattern.compile("^\\s*$");
        if (!txt_searchbook.getText().isEmpty() && !pattern.matcher(txt_searchbook.getText()).matches()) {
            String searchText = txt_searchbook.getText().trim();
            int choice = cbox_searchbook.getSelectedIndex();
            String columnName = "";

            switch (choice) {
                case 0:
                    columnName = "NIC";
                    break;
                case 1:
                    columnName = "Member_Name";
                    break;
                case 2:
                    columnName = "Contact_no";
                    break;
                default:
                    throw new AssertionError();
            }

            try {
                Connection con = DBConnection.getConnection();
                String sql = "SELECT * FROM tbl_memberdetails WHERE " + columnName + " LIKE ?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setString(1, "%" + searchText + "%");
                ResultSet rs = pst.executeQuery();

                DefaultTableModel model = (DefaultTableModel) tbl_Member.getModel();
                model.setRowCount(0); // Clear previous search results

                while (rs.next()) {
                    // Extract data from the ResultSet and add it to the table model
                    String NIC = rs.getString("NIC");
                    String Name = rs.getString("Member_Name");
                    String Address = rs.getString("Address");
                    String Email = rs.getString("Email");
                    String Gender = rs.getString("Gender");
                    String DOB = rs.getString("Date_of_birth");
                    String Age = rs.getString("Age");
                    String Phone = rs.getString("Contact_no");

                    model.addRow(new Object[]{NIC, Name, Email, Address, Gender, DOB, Age, Phone});
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please enter the search criteria", "", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        rSButtonMetroBeanInfo1 = new rojerusan.RSButtonMetroBeanInfo();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        lbl_Close = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        txt_MemberID = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txt_MemberName = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txt_MemberAddress = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txt_MemberPhone = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        rdo_Female = new javax.swing.JRadioButton();
        rdo_Male = new javax.swing.JRadioButton();
        jLabel11 = new javax.swing.JLabel();
        txt_Age = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        txt_password = new javax.swing.JTextField();
        date_Dob = new com.toedter.calendar.JDateChooser();
        txt_username = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        txt_Email = new javax.swing.JTextField();
        btn_Add1 = new rojerusan.RSMaterialButtonRectangle();
        btn_Cancle = new rojerusan.RSMaterialButtonRectangle();
        btn_Update = new rojerusan.RSMaterialButtonRectangle();
        btn_Delete = new rojerusan.RSMaterialButtonRectangle();
        jLabel14 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbl_Member = new rojeru_san.complementos.RSTableMetro();
        btn_Add5 = new rojerusan.RSMaterialButtonRectangle();
        txt_searchbook = new javax.swing.JTextField();
        cbox_searchbook = new javax.swing.JComboBox<>();
        btn_Add6 = new rojerusan.RSMaterialButtonRectangle();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(0, 0, 0));
        jPanel2.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(102, 102, 102)));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lbl_Close.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/icons8-close-20.png"))); // NOI18N
        lbl_Close.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbl_CloseMouseClicked(evt);
            }
        });
        jPanel2.add(lbl_Close, new org.netbeans.lib.awtextra.AbsoluteConstraints(1410, 10, 20, 20));

        jLabel2.setFont(new java.awt.Font("Segoe UI Semibold", 1, 22)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Member Details");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 10, 210, 40));

        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/user.png"))); // NOI18N
        jPanel2.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 15, 30, 30));

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1440, 60));

        jPanel3.setBackground(new java.awt.Color(0, 51, 102));
        jPanel3.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 1, 1, 0, new java.awt.Color(0, 0, 0)));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("NIC (without 'v')");
        jPanel3.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 120, 30));

        txt_MemberID.setBackground(new java.awt.Color(0, 51, 102));
        txt_MemberID.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        txt_MemberID.setForeground(new java.awt.Color(204, 204, 204));
        txt_MemberID.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));
        txt_MemberID.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txt_MemberIDFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txt_MemberIDFocusLost(evt);
            }
        });
        jPanel3.add(txt_MemberID, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 20, 260, 30));

        jLabel4.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Member Name");
        jPanel3.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 120, 30));

        txt_MemberName.setBackground(new java.awt.Color(0, 51, 102));
        txt_MemberName.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        txt_MemberName.setForeground(new java.awt.Color(204, 204, 204));
        txt_MemberName.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));
        txt_MemberName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txt_MemberNameFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txt_MemberNameFocusLost(evt);
            }
        });
        jPanel3.add(txt_MemberName, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 70, 260, 30));

        jLabel5.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Address");
        jPanel3.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, 120, 30));

        txt_MemberAddress.setBackground(new java.awt.Color(0, 51, 102));
        txt_MemberAddress.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        txt_MemberAddress.setForeground(new java.awt.Color(204, 204, 204));
        txt_MemberAddress.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));
        txt_MemberAddress.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txt_MemberAddressFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txt_MemberAddressFocusLost(evt);
            }
        });
        jPanel3.add(txt_MemberAddress, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 120, 260, 30));

        jLabel6.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Phone No.");
        jPanel3.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 370, 120, 30));

        txt_MemberPhone.setBackground(new java.awt.Color(0, 51, 102));
        txt_MemberPhone.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        txt_MemberPhone.setForeground(new java.awt.Color(204, 204, 204));
        txt_MemberPhone.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));
        txt_MemberPhone.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txt_MemberPhoneFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txt_MemberPhoneFocusLost(evt);
            }
        });
        jPanel3.add(txt_MemberPhone, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 370, 260, 30));

        jLabel7.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Birthday");
        jPanel3.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 270, 120, 30));

        jLabel8.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Age");
        jPanel3.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 320, 120, 30));

        jLabel9.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Gender");
        jPanel3.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 220, 120, 30));

        rdo_Female.setBackground(new java.awt.Color(0, 51, 102));
        rdo_Female.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        rdo_Female.setForeground(new java.awt.Color(255, 255, 255));
        rdo_Female.setText("Female");
        rdo_Female.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdo_FemaleActionPerformed(evt);
            }
        });
        jPanel3.add(rdo_Female, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 220, 90, 30));

        rdo_Male.setBackground(new java.awt.Color(0, 51, 102));
        rdo_Male.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        rdo_Male.setForeground(new java.awt.Color(255, 255, 255));
        rdo_Male.setText("Male");
        rdo_Male.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdo_MaleActionPerformed(evt);
            }
        });
        jPanel3.add(rdo_Male, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 220, 70, 30));

        jLabel11.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("username");
        jPanel3.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 490, 120, 30));

        txt_Age.setBackground(new java.awt.Color(0, 51, 102));
        txt_Age.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        txt_Age.setForeground(new java.awt.Color(204, 204, 204));
        txt_Age.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));
        txt_Age.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txt_AgeFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txt_AgeFocusLost(evt);
            }
        });
        txt_Age.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txt_AgeMouseClicked(evt);
            }
        });
        jPanel3.add(txt_Age, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 320, 260, 30));

        jLabel12.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 255, 255));
        jLabel12.setText("password");
        jPanel3.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 540, 120, 30));

        txt_password.setBackground(new java.awt.Color(0, 51, 102));
        txt_password.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        txt_password.setForeground(new java.awt.Color(204, 204, 204));
        txt_password.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));
        txt_password.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txt_passwordFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txt_passwordFocusLost(evt);
            }
        });
        jPanel3.add(txt_password, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 530, 260, 30));

        date_Dob.setBackground(new java.awt.Color(0, 51, 102));
        date_Dob.setForeground(new java.awt.Color(102, 102, 102));
        date_Dob.setFont(new java.awt.Font("Segoe UI Semibold", 0, 16)); // NOI18N
        date_Dob.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                date_DobFocusLost(evt);
            }
        });
        date_Dob.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                date_DobMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                date_DobMousePressed(evt);
            }
        });
        date_Dob.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                date_DobPropertyChange(evt);
            }
        });
        jPanel3.add(date_Dob, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 270, 250, 30));

        txt_username.setBackground(new java.awt.Color(0, 51, 102));
        txt_username.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        txt_username.setForeground(new java.awt.Color(204, 204, 204));
        txt_username.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));
        txt_username.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txt_usernameFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txt_usernameFocusLost(evt);
            }
        });
        jPanel3.add(txt_username, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 480, 260, 30));

        jLabel13.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(255, 255, 255));
        jLabel13.setText("Email");
        jPanel3.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, 120, 30));

        txt_Email.setBackground(new java.awt.Color(0, 51, 102));
        txt_Email.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        txt_Email.setForeground(new java.awt.Color(204, 204, 204));
        txt_Email.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));
        txt_Email.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txt_EmailFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txt_EmailFocusLost(evt);
            }
        });
        jPanel3.add(txt_Email, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 170, 260, 30));

        btn_Add1.setBackground(new java.awt.Color(0, 153, 255));
        btn_Add1.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 1, new java.awt.Color(255, 255, 255)));
        btn_Add1.setText("Add");
        btn_Add1.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        btn_Add1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_Add1ActionPerformed(evt);
            }
        });
        jPanel3.add(btn_Add1, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 600, 90, 60));

        btn_Cancle.setBackground(new java.awt.Color(0, 153, 255));
        btn_Cancle.setText("cancle");
        btn_Cancle.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        btn_Cancle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_CancleActionPerformed(evt);
            }
        });
        jPanel3.add(btn_Cancle, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 600, 90, 60));

        btn_Update.setBackground(new java.awt.Color(0, 153, 255));
        btn_Update.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 1, new java.awt.Color(255, 255, 255)));
        btn_Update.setText("Update");
        btn_Update.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        btn_Update.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_UpdateActionPerformed(evt);
            }
        });
        jPanel3.add(btn_Update, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 600, 90, 60));

        btn_Delete.setBackground(new java.awt.Color(0, 153, 255));
        btn_Delete.setText("Delete");
        btn_Delete.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        btn_Delete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_DeleteActionPerformed(evt);
            }
        });
        jPanel3.add(btn_Delete, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 600, 90, 60));

        jLabel14.setFont(new java.awt.Font("Segoe UI Semibold", 1, 18)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(255, 255, 255));
        jLabel14.setText("Login Credential");
        jPanel3.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 430, 150, 30));

        jPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 60, 450, 680));

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 1, new java.awt.Color(0, 0, 0)));
        jPanel4.setForeground(new java.awt.Color(255, 255, 255));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tbl_Member.setBackground(new java.awt.Color(0, 153, 255));
        tbl_Member.setForeground(new java.awt.Color(255, 255, 255));
        tbl_Member.setModel(new javax.swing.table.DefaultTableModel(
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
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "NIC", "Name", "Address", "Email", "Gender", "Birthday", "Age", "Phone_No"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, true, false, true, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tbl_Member.setColorBackgoundHead(new java.awt.Color(0, 51, 102));
        tbl_Member.setColorBordeFilas(new java.awt.Color(102, 102, 102));
        tbl_Member.setColorBordeHead(new java.awt.Color(102, 102, 102));
        tbl_Member.setColorFilasBackgound2(new java.awt.Color(255, 255, 255));
        tbl_Member.setColorFilasForeground1(new java.awt.Color(51, 51, 51));
        tbl_Member.setColorFilasForeground2(new java.awt.Color(51, 51, 51));
        tbl_Member.setColorSelBackgound(new java.awt.Color(204, 204, 204));
        tbl_Member.setColorSelForeground(new java.awt.Color(0, 0, 0));
        tbl_Member.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        tbl_Member.setFuenteFilas(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        tbl_Member.setFuenteFilasSelect(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        tbl_Member.setFuenteHead(new java.awt.Font("Segoe UI Semibold", 1, 18)); // NOI18N
        tbl_Member.setRowHeight(25);
        tbl_Member.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbl_MemberMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tbl_Member);

        jPanel4.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 150, 950, 500));

        btn_Add5.setBackground(new java.awt.Color(0, 153, 255));
        btn_Add5.setText("Clear");
        btn_Add5.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        btn_Add5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_Add5ActionPerformed(evt);
            }
        });
        jPanel4.add(btn_Add5, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 40, 90, 50));

        txt_searchbook.setFont(new java.awt.Font("Segoe UI Semibold", 0, 16)); // NOI18N
        txt_searchbook.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(204, 204, 204)));
        jPanel4.add(txt_searchbook, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 60, 340, 30));

        cbox_searchbook.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        cbox_searchbook.setForeground(new java.awt.Color(102, 102, 102));
        cbox_searchbook.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "search by NIC", "search by name", "search by contact number" }));
        jPanel4.add(cbox_searchbook, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 20, 340, 30));

        btn_Add6.setBackground(new java.awt.Color(0, 153, 255));
        btn_Add6.setText("Search");
        btn_Add6.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        btn_Add6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_Add6ActionPerformed(evt);
            }
        });
        jPanel4.add(btn_Add6, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 40, 90, 50));

        jPanel1.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 60, 990, 680));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1440, 740));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void txt_MemberPhoneFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_MemberPhoneFocusGained
        txt_MemberPhone.setBorder(border);
    }//GEN-LAST:event_txt_MemberPhoneFocusGained

    private void tbl_MemberMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbl_MemberMouseClicked
        int rowNo = tbl_Member.getSelectedRow();
        TableModel model = tbl_Member.getModel();
        txt_MemberID.setText(model.getValueAt(rowNo, 0).toString());
        txt_MemberName.setText(model.getValueAt(rowNo, 1).toString());
        txt_MemberAddress.setText(model.getValueAt(rowNo, 2).toString());
        txt_Email.setText(model.getValueAt(rowNo, 3).toString());
        Gender = model.getValueAt(rowNo, 4).toString();
        String dobS = model.getValueAt(rowNo, 5).toString();
        txt_Age.setText(model.getValueAt(rowNo, 6).toString());
        txt_MemberPhone.setText(model.getValueAt(rowNo, 7).toString());
        //System.out.println(gender);
        setGender(Gender);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date dob = dateFormat.parse(dobS);
            date_Dob.setDate(dob);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        getUsernamePassword();
    }//GEN-LAST:event_tbl_MemberMouseClicked

    private void rdo_MaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdo_MaleActionPerformed
        rdo_Female.setSelected(false);
    }//GEN-LAST:event_rdo_MaleActionPerformed

    private void rdo_FemaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdo_FemaleActionPerformed
        rdo_Male.setSelected(false);
    }//GEN-LAST:event_rdo_FemaleActionPerformed

    private void btn_Add1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_Add1ActionPerformed
        if (inputValidation()) {

            //to be continued
            try {
                if (setUsernamePassword()) {
                    if (addMember() == true) {
                        JOptionPane.showMessageDialog(this, "Member added", "", JOptionPane.INFORMATION_MESSAGE);
                        clearTable();
                        setMemberDetailsToTable();
                        //setMemberID();

                    } else {
                        JOptionPane.showMessageDialog(this, "Member addition fail", "", JOptionPane.ERROR_MESSAGE);
                    }
                }

            } catch (SQLException ex) {
                Logger.getLogger(ManageMember.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }//GEN-LAST:event_btn_Add1ActionPerformed

    private void btn_CancleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_CancleActionPerformed
        clearTextBoxes();
    }//GEN-LAST:event_btn_CancleActionPerformed

    private void btn_UpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_UpdateActionPerformed
        if (inputValidation()) {
            if (updateMemberDetails() == true) {
                JOptionPane.showMessageDialog(this, "details updated", "", JOptionPane.INFORMATION_MESSAGE);
                clearTable();
                setMemberDetailsToTable();
            } else {
                JOptionPane.showMessageDialog(this, "update fail", "", JOptionPane.ERROR_MESSAGE);
            }
        }

    }//GEN-LAST:event_btn_UpdateActionPerformed

    private void btn_DeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_DeleteActionPerformed
        if (deleteMember() == true) {
            JOptionPane.showMessageDialog(this, "Member removed", "", JOptionPane.INFORMATION_MESSAGE);
            clearTable();
            setMemberDetailsToTable();
            clearTextBoxes();
        } else {
            JOptionPane.showMessageDialog(this, "remove fail", "", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btn_DeleteActionPerformed

    private void date_DobFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_date_DobFocusLost

    }//GEN-LAST:event_date_DobFocusLost

    private void lbl_CloseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_CloseMouseClicked
        this.dispose();
    }//GEN-LAST:event_lbl_CloseMouseClicked

    private void txt_AgeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txt_AgeMouseClicked

    }//GEN-LAST:event_txt_AgeMouseClicked

    private void date_DobMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_date_DobMousePressed
        // TODO add your handling code here:
    }//GEN-LAST:event_date_DobMousePressed

    private void date_DobMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_date_DobMouseExited

    }//GEN-LAST:event_date_DobMouseExited

    private void date_DobPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_date_DobPropertyChange
        if (date_Dob.getDate() != null) {
            Date dob = date_Dob.getDate();
            //System.out.println("got the age");
            txt_Age.setText(String.valueOf(setAge(dob)));
        }
    }//GEN-LAST:event_date_DobPropertyChange

    private void btn_Add5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_Add5ActionPerformed
        setMemberDetailsToTable();
    }//GEN-LAST:event_btn_Add5ActionPerformed

    private void txt_MemberIDFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_MemberIDFocusGained
        txt_MemberID.setBorder(border);
    }//GEN-LAST:event_txt_MemberIDFocusGained

    private void txt_MemberNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_MemberNameFocusGained
        txt_MemberName.setBorder(border);
    }//GEN-LAST:event_txt_MemberNameFocusGained

    private void txt_MemberAddressFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_MemberAddressFocusGained
        txt_MemberAddress.setBorder(border);

    }//GEN-LAST:event_txt_MemberAddressFocusGained

    private void txt_EmailFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_EmailFocusGained
        txt_Email.setBorder(border);
    }//GEN-LAST:event_txt_EmailFocusGained

    private void txt_AgeFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_AgeFocusGained
        txt_Age.setBorder(border);
    }//GEN-LAST:event_txt_AgeFocusGained

    private void txt_usernameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_usernameFocusGained
        txt_username.setBorder(border);
    }//GEN-LAST:event_txt_usernameFocusGained

    private void txt_passwordFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_passwordFocusGained
        txt_password.setBorder(border);
    }//GEN-LAST:event_txt_passwordFocusGained

    private void txt_MemberIDFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_MemberIDFocusLost
        txt_MemberID.setBorder(borderout);
    }//GEN-LAST:event_txt_MemberIDFocusLost

    private void txt_MemberNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_MemberNameFocusLost
        txt_MemberName.setBorder(borderout);
    }//GEN-LAST:event_txt_MemberNameFocusLost

    private void txt_MemberAddressFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_MemberAddressFocusLost
        txt_MemberAddress.setBorder(borderout);
    }//GEN-LAST:event_txt_MemberAddressFocusLost

    private void txt_EmailFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_EmailFocusLost
        txt_Email.setBorder(borderout);
    }//GEN-LAST:event_txt_EmailFocusLost

    private void txt_AgeFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_AgeFocusLost
        txt_Age.setBorder(borderout);
    }//GEN-LAST:event_txt_AgeFocusLost

    private void txt_MemberPhoneFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_MemberPhoneFocusLost
        txt_MemberPhone.setBorder(borderout);
    }//GEN-LAST:event_txt_MemberPhoneFocusLost

    private void txt_usernameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_usernameFocusLost
        txt_username.setBorder(borderout);
    }//GEN-LAST:event_txt_usernameFocusLost

    private void txt_passwordFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_passwordFocusLost
        txt_password.setBorder(borderout);
    }//GEN-LAST:event_txt_passwordFocusLost

    private void btn_Add6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_Add6ActionPerformed
        searchBook();
    }//GEN-LAST:event_btn_Add6ActionPerformed

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
            java.util.logging.Logger.getLogger(ManageMember.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ManageMember.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ManageMember.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ManageMember.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
                new ManageMember().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private rojerusan.RSMaterialButtonRectangle btn_Add1;
    private rojerusan.RSMaterialButtonRectangle btn_Add5;
    private rojerusan.RSMaterialButtonRectangle btn_Add6;
    private rojerusan.RSMaterialButtonRectangle btn_Cancle;
    private rojerusan.RSMaterialButtonRectangle btn_Delete;
    private rojerusan.RSMaterialButtonRectangle btn_Update;
    private javax.swing.JComboBox<String> cbox_searchbook;
    private com.toedter.calendar.JDateChooser date_Dob;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
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
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbl_Close;
    private rojerusan.RSButtonMetroBeanInfo rSButtonMetroBeanInfo1;
    private javax.swing.JRadioButton rdo_Female;
    private javax.swing.JRadioButton rdo_Male;
    private rojeru_san.complementos.RSTableMetro tbl_Member;
    private javax.swing.JTextField txt_Age;
    private javax.swing.JTextField txt_Email;
    private javax.swing.JTextField txt_MemberAddress;
    private javax.swing.JTextField txt_MemberID;
    private javax.swing.JTextField txt_MemberName;
    private javax.swing.JTextField txt_MemberPhone;
    private javax.swing.JTextField txt_password;
    private javax.swing.JTextField txt_searchbook;
    private javax.swing.JTextField txt_username;
    // End of variables declaration//GEN-END:variables
}
