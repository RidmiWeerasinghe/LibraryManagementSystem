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
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
public class ManageBook extends javax.swing.JFrame {

    /**
     * Creates new form ManageBook
     */
    String ISBN, Book_name, Author, Publisher, Edition, Catagory, Book_Id;
    int No_of_pages, Quantity;
    double Price;
    //borders of textboxes
    Color white = new Color(255, 255, 255);
    Color lightgray = new Color(153, 153, 153);
    Border borderout = BorderFactory.createMatteBorder(0, 0, 1, 0, lightgray);
    Border border = BorderFactory.createMatteBorder(0, 0, 1, 0, white);

    public ManageBook() {
        initComponents();
        setBookDetailsToTable();
        //txt_BookID.setEditable(false);
    }

    public boolean addBook() {
        boolean isAdded = false;
        ISBN = txt_ISBN.getText();
        Book_name = txt_BookName.getText();
        Author = txt_Author.getText();
        Publisher = txt_Publisher.getText();
        Edition = txt_Publisher.getText();
        Catagory = txt_Catagory.getText();
        No_of_pages = Integer.parseInt(txt_Pages.getText());
        Quantity = Integer.parseInt(txt_Quantity.getText());
        Price = Double.parseDouble(txt_Price.getText());

        try {
            Connection con = DBConnection.getConnection();

            String checkSql = "SELECT * FROM tbl_book WHERE ISBN = ?";
            PreparedStatement checkPst = con.prepareStatement(checkSql);
            checkPst.setString(1, ISBN);
            ResultSet checkRs = checkPst.executeQuery();

            if (checkRs.next()) {
                // Book already exists
                JOptionPane.showMessageDialog(this, "Book with ISBN " + ISBN + " already exists.");
            } else {
                String sql = "INSERT INTO tbl_book(ISBN,Book_name,Author,Publisher,Edition,Category,No_of_pages,Quantity,Price) values(?,?,?,?,?,?,?,?,?)";
                PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                pst.setString(1, ISBN);
                pst.setString(2, Book_name);
                pst.setString(3, Author);
                pst.setString(4, Publisher);
                pst.setString(5, Edition);
                pst.setString(6, Catagory);
                pst.setInt(7, No_of_pages);
                pst.setInt(8, Quantity);
                pst.setDouble(9, Price);

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
                    String bookCopySql = "INSERT INTO tbl_BookCopy (Book_ID,ISBN , Status) VALUES (?, ?, ?)";
                    PreparedStatement bookCopyPst = con.prepareStatement(bookCopySql);

                    for (int i = 1; i <= Quantity; i++) {
                        bookCopyPst.setString(1, ISBN + "C" + i);
                        bookCopyPst.setString(2, ISBN);
                        bookCopyPst.setString(3, "Available");
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

    //my code
    /*
    public boolean updateBook() {
        boolean isUpdated = false;
        ISBN = txt_ISBN.getText();
        Book_name = txt_BookName.getText();
        Author = txt_Author.getText();
        Publisher = txt_Publisher.getText();
        Edition = txt_Publisher.getText();
        Catagory = txt_Catagory.getText();
        No_of_pages = Integer.parseInt(txt_Pages.getText());
        Quantity = Integer.parseInt(txt_Quantity.getText());

        try {
            Connection con = DBConnection.getConnection();
            String sql = "INSERT INTO tbl_book(ISBN,Book_name,Author,Publisher,Edition,Catagory,No_of_pages,Quantity) values(?,?,?,?,?,?,?,?)";
            PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pst.setString(1, ISBN);
            pst.setString(2, Book_name);
            pst.setString(3, Author);
            pst.setString(4, Publisher);
            pst.setString(5, Edition);
            pst.setString(6, Catagory);
            pst.setInt(7, No_of_pages);
            pst.setInt(8, Quantity);

            int rowCount = pst.executeUpdate();
            /*if (rowCount > 0) {
                isAdded = true;
                System.out.println("book added to the book table");
            } else {
                isAdded = false;
            }*/
    // Get the generated Book_ID
    /*int bookID = -1;
            ResultSet generatedKeys = pst.getGeneratedKeys();
            if (generatedKeys.next()) {
                bookID = generatedKeys.getInt(1);
            }
            if (rowCount > 0 && bookID != -1) {

                String bookCopySql = "INSERT INTO tbl_BookCopy (Book_ID,Status) VALUES (?, ?)";
                PreparedStatement bookCopyPst = con.prepareStatement(bookCopySql);

                for (int i = 1; i <= Quantity; i++) {
                    bookCopyPst.setInt(1, bookID);
                    bookCopyPst.setString(2, "Available");
                    bookCopyPst.executeUpdate();
                }

                isUpdated = true;
            } else {
                isUpdated = false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return isUpdated;
    }*/
    public boolean updateBook() {
        boolean isUpdated = false;
        ISBN = txt_ISBN.getText();
        //Book_Id = String.valueOf(getBookIdByISBN(ISBN));
        Book_name = txt_BookName.getText();
        Author = txt_Author.getText();
        Publisher = txt_Publisher.getText();
        Edition = txt_Edition.getText();
        Catagory = txt_Catagory.getText();
        No_of_pages = Integer.parseInt(txt_Pages.getText());
        Quantity = Integer.parseInt(txt_Quantity.getText());
        Price = Double.parseDouble(txt_Price.getText());

        try {
            Connection con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // Check if Quantity has changed
            String selectQuantitySql = "SELECT Quantity FROM tbl_book WHERE ISBN=?";
            PreparedStatement selectQuantityPst = con.prepareStatement(selectQuantitySql);
            selectQuantityPst.setString(1, ISBN);
            ResultSet quantityRs = selectQuantityPst.executeQuery();

            if (quantityRs.next()) {
                int oldQuantity = quantityRs.getInt("Quantity");
                //System.out.println("q" + oldQuantity);

                // Update tbl_book
                String updateSql = "UPDATE tbl_book SET Book_name=?, Author=?, Publisher=?, Edition=?, Category=?, No_of_pages=?, Quantity=?, Price=? WHERE ISBN=?";
                PreparedStatement updatePst = con.prepareStatement(updateSql);
                //updatePst.setString(1, ISBN);
                updatePst.setString(1, Book_name);
                updatePst.setString(2, Author);
                updatePst.setString(3, Publisher);
                updatePst.setString(4, Edition);
                updatePst.setString(5, Catagory);
                updatePst.setInt(6, No_of_pages);
                updatePst.setInt(7, Quantity);
                updatePst.setDouble(8, Price);
                updatePst.setString(9, ISBN);

                int rowCount = updatePst.executeUpdate();

                if (rowCount > 0) {
                    if (oldQuantity > Quantity) {
                        // Delete book copies
                        int copiesToDelete = oldQuantity - Quantity;
                        String deleteBookCopySql = "DELETE FROM tbl_BookCopy WHERE ISBN=? AND Status='Available' LIMIT ?";
                        PreparedStatement deleteBookCopyPst = con.prepareStatement(deleteBookCopySql);
                        deleteBookCopyPst.setString(1, ISBN);
                        deleteBookCopyPst.setInt(2, copiesToDelete);
                        deleteBookCopyPst.executeUpdate();

                    } else if (oldQuantity < Quantity) {
                        // Add book copies
                        String selectQuantitySql2 = "SELECT Book_ID FROM tbl_bookcopy WHERE ISBN=? ORDER BY Book_ID DESC LIMIT 1";
                        PreparedStatement selectQuantityPst2 = con.prepareStatement(selectQuantitySql2);
                        selectQuantityPst2.setString(1, ISBN);
                        ResultSet quantityRs2 = selectQuantityPst2.executeQuery();

                        if (quantityRs2.next()) {
                            String lastBookID = quantityRs2.getString("Book_ID");
                            int C = lastBookID.indexOf('C');
                            int Booknumber = Integer.parseInt(lastBookID.substring(C + 1));
                            System.out.println(Booknumber);
                            // Use the lastBookID as needed
                            quantityRs2.close();

                            int copiesToAdd = Quantity - oldQuantity;
                            String addBookCopySql = "INSERT INTO tbl_BookCopy(Book_ID,ISBN, Status) VALUES (?,?, 'Available')";
                            PreparedStatement addBookCopyPst = con.prepareStatement(addBookCopySql);

                            for (int i = 0; i < copiesToAdd; i++) {
                                Book_Id = ISBN + "C" + (Booknumber + 1);
                                addBookCopyPst.setString(1, Book_Id);
                                addBookCopyPst.setString(2, ISBN);
                                addBookCopyPst.executeUpdate();
                                Booknumber++;
                            }
                        }

                    }

                    con.commit();
                    isUpdated = true;
                } else {
                    con.rollback();
                    isUpdated = false;
                }
            }

            con.setAutoCommit(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isUpdated;
    }

    public boolean isValidInput() {
        boolean valid;
        test t = new test();
        ISBN = txt_ISBN.getText();
        if (/*txt_MemberID.getText().isEmpty()
                ||*/txt_ISBN.getText().trim().isEmpty()
                || txt_BookName.getText().trim().isEmpty()
                || txt_Author.getText().trim().isEmpty()
                || txt_Publisher.getText().trim().isEmpty()
                || txt_Edition.getText().trim().isEmpty()
                || txt_Pages.getText().trim().isEmpty()
                || txt_Quantity.getText().trim().isEmpty()
                || txt_Catagory.getText().trim().isEmpty()
                || txt_Price.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled out", "", JOptionPane.WARNING_MESSAGE);
            valid = false;
        } /*else if (!t.isNumber(txt_MemberID.getText())) {
            JOptionPane.showMessageDialog(this,"NIC should only contain numbers");
            return false;
        }*/ else if (!validateISBN(ISBN)) {
            JOptionPane.showMessageDialog(this, "please enter valid ISBN", "", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!isInteger(txt_Quantity.getText())) {
            JOptionPane.showMessageDialog(this, "Enter a valid quantity", "", JOptionPane.WARNING_MESSAGE);
            valid = false;
        } else if (txt_Quantity.getText().equals("0")) {
            JOptionPane.showMessageDialog(this, "quantity can not be zero", "", JOptionPane.WARNING_MESSAGE);
            valid = false;
        } else if (!isInteger(txt_Pages.getText())) {
            JOptionPane.showMessageDialog(this, "Enter valid umber of pages", "", JOptionPane.WARNING_MESSAGE);
            valid = false;
        } else if (!isDouble(txt_Price.getText())) {
            JOptionPane.showMessageDialog(this, "Enter valid price", "", JOptionPane.WARNING_MESSAGE);
            valid = false;
        } else {
            valid = true;
        }

        return valid;
    }

    //getting data from database
    public void setBookDetailsToTable() {
        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT ISBN, Book_name, Author, Publisher, No_of_pages, Edition,Quantity, Category, Price FROM tbl_book");

            // Clear existing rows from the table
            DefaultTableModel model = (DefaultTableModel) tbl_book.getModel();
            model.setRowCount(0);

            while (rs.next()) {
                ISBN = rs.getString("ISBN");
                Book_name = rs.getString("Book_name");
                Author = rs.getString("Author");
                Publisher = rs.getString("Publisher");
                String No_of_pages = rs.getString("No_of_pages");
                Edition = rs.getString("Edition");
                String Quantity = rs.getString("Quantity");
                Catagory = rs.getString("Category");
                Price = rs.getDouble("Price");

                Object[] obj = {ISBN, Book_name, Author, Publisher, No_of_pages, Edition, Quantity, Catagory, Price};
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

    public void clearTextBoxes() {
        txt_Author.setText("");
        txt_BookName.setText("");
        txt_Catagory.setText("");
        txt_Edition.setText("");
        txt_ISBN.setText("");
        txt_Pages.setText("");
        txt_Publisher.setText("");
        txt_Quantity.setText("");
        txt_Price.setText("");
    }

    /*public int getBookIdByISBN(String isbn) {
        int bookId = -1;

        try {
            Connection con = DBConnection.getConnection();
            String sql = "SELECT Book_ID FROM tbl_book WHERE ISBN = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, isbn);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                bookId = rs.getInt("Book_ID");
            }

            rs.close();
            pst.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bookId;
    }*/
    public void clearTable() {
        DefaultTableModel model = (DefaultTableModel) tbl_book.getModel();
        model.setRowCount(0);
    }

    //deleter book
    public boolean deleteBook() {
        boolean isDeleted = false;
        ISBN = txt_ISBN.getText();
        //int bookId = getBookIdByISBN(ISBN);
        try {
            Connection con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // Delete from tbl_BookCopy
            String deleteBookCopySql = "DELETE FROM tbl_BookCopy WHERE ISBN=?";
            PreparedStatement deleteBookCopyPst = con.prepareStatement(deleteBookCopySql);
            deleteBookCopyPst.setString(1, ISBN);
            int rowCount2 = deleteBookCopyPst.executeUpdate();

            // Delete from tbl_book
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

            con.setAutoCommit(true);
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
                    columnName = "Category";
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
                    String category = rs.getString("Category");
                    String publisher = rs.getString("Publisher");
                    String author = rs.getString("Author");
                    String pages = rs.getString("No_of_pages");
                    String quantity = rs.getString("Quantity");
                    String edition = rs.getString("Edition");
                    String price = rs.getString("Price");

                    model.addRow(new Object[]{ISBN, bookName, author, publisher, pages, edition, quantity, category,price});
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

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        lbl_Close = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        txt_ISBN = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txt_BookName = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txt_Author = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txt_Pages = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txt_Edition = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txt_Quantity = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txt_Catagory = new javax.swing.JTextField();
        btn_Cancle = new rojerusan.RSMaterialButtonRectangle();
        btn_Update = new rojerusan.RSMaterialButtonRectangle();
        btn_Add = new rojerusan.RSMaterialButtonRectangle();
        btn_Delete = new rojerusan.RSMaterialButtonRectangle();
        jLabel10 = new javax.swing.JLabel();
        txt_Publisher = new javax.swing.JTextField();
        txt_Price = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbl_book = new rojeru_san.complementos.RSTableMetro();
        cbox_searchbook = new javax.swing.JComboBox<>();
        txt_searchbook = new javax.swing.JTextField();
        btn_clear = new rojerusan.RSMaterialButtonRectangle();
        btn_search = new rojerusan.RSMaterialButtonRectangle();

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
        jLabel2.setText("Book Details");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 0, 120, 60));

        lbl_Close.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/icons8-close-20.png"))); // NOI18N
        lbl_Close.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbl_CloseMouseClicked(evt);
            }
        });
        jPanel2.add(lbl_Close, new org.netbeans.lib.awtextra.AbsoluteConstraints(1400, 10, 20, 20));

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1430, -1));

        jPanel3.setBackground(new java.awt.Color(0, 51, 102));
        jPanel3.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 1, 1, 0, new java.awt.Color(0, 0, 0)));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("ISBN");
        jPanel3.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 110, 30));

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
        jPanel3.add(txt_ISBN, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 30, 270, 30));

        jLabel4.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Book Name");
        jPanel3.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, 110, 30));

        txt_BookName.setBackground(new java.awt.Color(0, 51, 102));
        txt_BookName.setFont(new java.awt.Font("Segoe UI Semibold", 0, 16)); // NOI18N
        txt_BookName.setForeground(new java.awt.Color(204, 204, 204));
        txt_BookName.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));
        txt_BookName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txt_BookNameFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txt_BookNameFocusLost(evt);
            }
        });
        jPanel3.add(txt_BookName, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 80, 270, 30));

        jLabel5.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Author");
        jPanel3.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 130, 110, 30));

        txt_Author.setBackground(new java.awt.Color(0, 51, 102));
        txt_Author.setFont(new java.awt.Font("Segoe UI Semibold", 0, 16)); // NOI18N
        txt_Author.setForeground(new java.awt.Color(204, 204, 204));
        txt_Author.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));
        txt_Author.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txt_AuthorFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txt_AuthorFocusLost(evt);
            }
        });
        jPanel3.add(txt_Author, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 130, 270, 30));

        jLabel6.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("No. of pages");
        jPanel3.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 230, 110, 30));

        txt_Pages.setBackground(new java.awt.Color(0, 51, 102));
        txt_Pages.setFont(new java.awt.Font("Segoe UI Semibold", 0, 16)); // NOI18N
        txt_Pages.setForeground(new java.awt.Color(204, 204, 204));
        txt_Pages.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));
        txt_Pages.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txt_PagesFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txt_PagesFocusLost(evt);
            }
        });
        jPanel3.add(txt_Pages, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 230, 270, 30));

        jLabel7.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Edtion");
        jPanel3.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 280, 110, 30));

        txt_Edition.setBackground(new java.awt.Color(0, 51, 102));
        txt_Edition.setFont(new java.awt.Font("Segoe UI Semibold", 0, 16)); // NOI18N
        txt_Edition.setForeground(new java.awt.Color(204, 204, 204));
        txt_Edition.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));
        txt_Edition.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txt_EditionFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txt_EditionFocusLost(evt);
            }
        });
        jPanel3.add(txt_Edition, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 280, 270, 30));

        jLabel8.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Quantity");
        jPanel3.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 330, 110, 30));

        txt_Quantity.setBackground(new java.awt.Color(0, 51, 102));
        txt_Quantity.setFont(new java.awt.Font("Segoe UI Semibold", 0, 16)); // NOI18N
        txt_Quantity.setForeground(new java.awt.Color(204, 204, 204));
        txt_Quantity.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));
        txt_Quantity.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txt_QuantityFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txt_QuantityFocusLost(evt);
            }
        });
        jPanel3.add(txt_Quantity, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 330, 270, 30));

        jLabel9.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Category");
        jPanel3.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 380, 110, 30));

        txt_Catagory.setBackground(new java.awt.Color(0, 51, 102));
        txt_Catagory.setFont(new java.awt.Font("Segoe UI Semibold", 0, 16)); // NOI18N
        txt_Catagory.setForeground(new java.awt.Color(204, 204, 204));
        txt_Catagory.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));
        txt_Catagory.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txt_CatagoryFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txt_CatagoryFocusLost(evt);
            }
        });
        jPanel3.add(txt_Catagory, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 380, 270, 30));

        btn_Cancle.setBackground(new java.awt.Color(0, 153, 255));
        btn_Cancle.setText("Cancle");
        btn_Cancle.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        btn_Cancle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_CancleActionPerformed(evt);
            }
        });
        jPanel3.add(btn_Cancle, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 500, 90, 60));

        btn_Update.setBackground(new java.awt.Color(0, 153, 255));
        btn_Update.setText("Update");
        btn_Update.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        btn_Update.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_UpdateActionPerformed(evt);
            }
        });
        jPanel3.add(btn_Update, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 500, 90, 60));

        btn_Add.setBackground(new java.awt.Color(0, 153, 255));
        btn_Add.setText("Add");
        btn_Add.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        btn_Add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_AddActionPerformed(evt);
            }
        });
        jPanel3.add(btn_Add, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 500, 90, 60));

        btn_Delete.setBackground(new java.awt.Color(0, 153, 255));
        btn_Delete.setText("Delete");
        btn_Delete.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        btn_Delete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_DeleteActionPerformed(evt);
            }
        });
        jPanel3.add(btn_Delete, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 500, 90, 60));

        jLabel10.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("Publisher");
        jPanel3.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 180, 110, 30));

        txt_Publisher.setBackground(new java.awt.Color(0, 51, 102));
        txt_Publisher.setFont(new java.awt.Font("Segoe UI Semibold", 0, 16)); // NOI18N
        txt_Publisher.setForeground(new java.awt.Color(204, 204, 204));
        txt_Publisher.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));
        txt_Publisher.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txt_PublisherFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txt_PublisherFocusLost(evt);
            }
        });
        jPanel3.add(txt_Publisher, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 180, 270, 30));

        txt_Price.setBackground(new java.awt.Color(0, 51, 102));
        txt_Price.setFont(new java.awt.Font("Segoe UI Semibold", 0, 16)); // NOI18N
        txt_Price.setForeground(new java.awt.Color(204, 204, 204));
        txt_Price.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));
        txt_Price.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txt_PriceFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txt_PriceFocusLost(evt);
            }
        });
        jPanel3.add(txt_Price, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 430, 270, 30));

        jLabel11.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("Price");
        jPanel3.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 430, 110, 30));

        jPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 60, 450, 580));

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 1, new java.awt.Color(0, 51, 102)));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tbl_book.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ISBN", "Book_Name", "Author", "Publisher", "No_of_pages", "Edtion", "Quantity", "Category", "Price"
            }
        ));
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
        if (tbl_book.getColumnModel().getColumnCount() > 0) {
            tbl_book.getColumnModel().getColumn(3).setResizable(false);
            tbl_book.getColumnModel().getColumn(7).setResizable(false);
            tbl_book.getColumnModel().getColumn(8).setResizable(false);
        }

        jPanel4.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 130, 920, 420));

        cbox_searchbook.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        cbox_searchbook.setForeground(new java.awt.Color(102, 102, 102));
        cbox_searchbook.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "search by ISBN", "search by book name", "search by category", "search by publisher", "search by author" }));
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

        jPanel1.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 60, 980, 580));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1430, 650));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void lbl_CloseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_CloseMouseClicked
        this.dispose();
    }//GEN-LAST:event_lbl_CloseMouseClicked

    private void btn_CancleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_CancleActionPerformed
        clearTextBoxes();
    }//GEN-LAST:event_btn_CancleActionPerformed

    private void btn_UpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_UpdateActionPerformed
        if (isValidInput()) {
            if (updateBook() == true) {
                JOptionPane.showMessageDialog(this, "details updated", "", JOptionPane.INFORMATION_MESSAGE);
                clearTable();
                setBookDetailsToTable();
            } else {
                JOptionPane.showMessageDialog(this, "update fail", "", JOptionPane.ERROR_MESSAGE);
            }
        }

    }//GEN-LAST:event_btn_UpdateActionPerformed

    private void btn_AddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_AddActionPerformed
        if (isValidInput()) {
            if (addBook() == true) {
                JOptionPane.showMessageDialog(this, "Book added", "", JOptionPane.INFORMATION_MESSAGE);
                clearTable();
                setBookDetailsToTable();
                //setMemberID();
                //setUsernamePassword();
            } else {
                JOptionPane.showMessageDialog(this, "Book addition fail", "", JOptionPane.ERROR_MESSAGE);
            }
        }

    }//GEN-LAST:event_btn_AddActionPerformed

    private void btn_DeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_DeleteActionPerformed
        if (deleteBook() == true) {
            JOptionPane.showMessageDialog(this, "Book removed", "", JOptionPane.INFORMATION_MESSAGE);
            clearTable();
            setBookDetailsToTable();
            clearTextBoxes();
        } else {
            JOptionPane.showMessageDialog(this, "remove fail", "", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_btn_DeleteActionPerformed

    private void tbl_bookMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbl_bookMouseClicked
        int rowNo = tbl_book.getSelectedRow();
        TableModel model = tbl_book.getModel();
        txt_ISBN.setText(model.getValueAt(rowNo, 0).toString());
        txt_BookName.setText(model.getValueAt(rowNo, 1).toString());
        txt_Author.setText(model.getValueAt(rowNo, 2).toString());
        txt_Publisher.setText(model.getValueAt(rowNo, 3).toString());
        txt_Pages.setText(model.getValueAt(rowNo, 4).toString());
        txt_Edition.setText(model.getValueAt(rowNo, 5).toString());
        txt_Quantity.setText(model.getValueAt(rowNo, 6).toString());
        txt_Catagory.setText(model.getValueAt(rowNo, 7).toString());
        txt_Price.setText(model.getValueAt(rowNo, 8).toString());
        //txt_BookID.setText(String.valueOf(getBookIdByISBN(ISBN)));
        //System.out.println(gender);
    }//GEN-LAST:event_tbl_bookMouseClicked

    private void txt_ISBNFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_ISBNFocusGained
        txt_ISBN.setBorder(border);
    }//GEN-LAST:event_txt_ISBNFocusGained

    private void txt_ISBNFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_ISBNFocusLost
        txt_ISBN.setBorder(borderout);
    }//GEN-LAST:event_txt_ISBNFocusLost

    private void txt_BookNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_BookNameFocusGained
        txt_BookName.setBorder(border);
    }//GEN-LAST:event_txt_BookNameFocusGained

    private void txt_BookNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_BookNameFocusLost
        txt_BookName.setBorder(borderout);
    }//GEN-LAST:event_txt_BookNameFocusLost

    private void txt_AuthorFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_AuthorFocusGained
        txt_Author.setBorder(border);
    }//GEN-LAST:event_txt_AuthorFocusGained

    private void txt_AuthorFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_AuthorFocusLost
        txt_Author.setBorder(borderout);
    }//GEN-LAST:event_txt_AuthorFocusLost

    private void txt_PublisherFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_PublisherFocusGained
        txt_Publisher.setBorder(border);
    }//GEN-LAST:event_txt_PublisherFocusGained

    private void txt_PublisherFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_PublisherFocusLost
        txt_Publisher.setBorder(borderout);
    }//GEN-LAST:event_txt_PublisherFocusLost

    private void txt_PagesFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_PagesFocusGained
        txt_Pages.setBorder(border);
    }//GEN-LAST:event_txt_PagesFocusGained

    private void txt_PagesFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_PagesFocusLost
        txt_Pages.setBorder(borderout);
    }//GEN-LAST:event_txt_PagesFocusLost

    private void txt_EditionFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_EditionFocusGained
        txt_Edition.setBorder(border);
    }//GEN-LAST:event_txt_EditionFocusGained

    private void txt_EditionFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_EditionFocusLost
        txt_Edition.setBorder(borderout);
    }//GEN-LAST:event_txt_EditionFocusLost

    private void txt_QuantityFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_QuantityFocusGained
        txt_Quantity.setBorder(border);
    }//GEN-LAST:event_txt_QuantityFocusGained

    private void txt_QuantityFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_QuantityFocusLost
        txt_Quantity.setBorder(borderout);
    }//GEN-LAST:event_txt_QuantityFocusLost

    private void txt_CatagoryFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_CatagoryFocusGained
        txt_Catagory.setBorder(border);
    }//GEN-LAST:event_txt_CatagoryFocusGained

    private void txt_CatagoryFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_CatagoryFocusLost
        txt_Catagory.setBorder(borderout);
    }//GEN-LAST:event_txt_CatagoryFocusLost

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

    private void txt_PriceFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_PriceFocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_PriceFocusGained

    private void txt_PriceFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_PriceFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_PriceFocusLost

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
            java.util.logging.Logger.getLogger(ManageBook.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ManageBook.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ManageBook.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ManageBook.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ManageBook().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private rojerusan.RSMaterialButtonRectangle btn_Add;
    private rojerusan.RSMaterialButtonRectangle btn_Cancle;
    private rojerusan.RSMaterialButtonRectangle btn_Delete;
    private rojerusan.RSMaterialButtonRectangle btn_Update;
    private rojerusan.RSMaterialButtonRectangle btn_clear;
    private rojerusan.RSMaterialButtonRectangle btn_search;
    private javax.swing.JComboBox<String> cbox_searchbook;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
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
    private rojeru_san.complementos.RSTableMetro tbl_book;
    private javax.swing.JTextField txt_Author;
    private javax.swing.JTextField txt_BookName;
    private javax.swing.JTextField txt_Catagory;
    private javax.swing.JTextField txt_Edition;
    private javax.swing.JTextField txt_ISBN;
    private javax.swing.JTextField txt_Pages;
    private javax.swing.JTextField txt_Price;
    private javax.swing.JTextField txt_Publisher;
    private javax.swing.JTextField txt_Quantity;
    private javax.swing.JTextField txt_searchbook;
    // End of variables declaration//GEN-END:variables
}
