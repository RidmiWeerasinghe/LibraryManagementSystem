/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jFrame;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author upeks
 */
public class Validation {

    public static boolean emailValidation(String email) {
        boolean isValid = false;
        String emailPattern = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.com)$";
        //String regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"+ "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

        // Check if the email matches the pattern
        if (email.matches(emailPattern)) {
            isValid = true;
        }
        return isValid;
    }

    public static boolean validatePhoneNumber(String phoneNumber) {
        String pattern = "^0[0-9]{9}$";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(phoneNumber);
        return matcher.matches();
    }

    public static boolean validateNIC(String nicNumber) {
        String pattern = "^[0-9]{9}([0-9]{3})?$";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(nicNumber);
        return matcher.matches();
    }

    public static boolean validateISBN(String isbn) {
        String pattern = "^[0-9]{10}|[0-9]{13}$";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(isbn);
        return matcher.matches();
    }

    public static boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isDouble(String input) {
        try {
            Double.parseDouble(input);
            String[] parts = input.split("\\.");
            return parts.length == 2 && parts[1].length() == 2;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public static boolean validateBookID(String bookID) {
        String pattern = "^(\\d{10}|\\d{13})C\\d+$";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(bookID);
        return matcher.matches();
    }
}
