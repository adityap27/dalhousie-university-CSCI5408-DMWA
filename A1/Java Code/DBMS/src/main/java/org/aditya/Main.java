package org.aditya;

import org.aditya.authentication.Authenticator;
import org.aditya.authentication.TwoFactorAuthenticator;
import org.aditya.buffers.TableBufferForTextFiles;
import org.aditya.captcha.SimpleStringCaptchaGenerator;
import org.aditya.hashing.MD5;
import org.aditya.query.Query;

import java.io.IOException;
import java.util.Scanner;

/**
 * Main Method class - Entry point class of DBMS Application.
 */
public class Main {

    /**
     * Main method - Entry point method of DBMS Application.
     *
     * @param args command-line arguments, un-used in this DBMS application.
     * @throws IOException if an error occurs during file handling.
     */
    public static void main(String[] args) throws IOException {
        Scanner s = new Scanner(System.in);
        int choice;
        Authenticator auth = new TwoFactorAuthenticator(new SimpleStringCaptchaGenerator(), new MD5());
        boolean loginSuccess = false;
        do {
            System.out.println("\n");
            System.out.println("1. Sign-up");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Enter Choice:");
            choice = Integer.parseInt(s.nextLine());
            switch (choice) {
                case 1:
                    auth.signUp();
                    break;
                case 2:
                    loginSuccess=auth.authenticate();
                    break;
                case 3:
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice!");
            }
        } while (!loginSuccess);

        boolean isInsideTransaction = false;
        System.out.println("Welcome to DBMS. Type exit and press enter to exit.");
        while (true) {
            System.out.print("sql>");
            String line = s.nextLine();

            Query q = new Query(new TableBufferForTextFiles());
            if (line.equalsIgnoreCase("EXIT")) {
                System.exit(0);
            }
            if (line.toUpperCase().startsWith("CREATE")) {
                q.executeCreate(line, isInsideTransaction);
            } else if (line.toUpperCase().startsWith("INSERT")) {
                q.executeInsert(line, isInsideTransaction);
            } else if (line.toUpperCase().startsWith("SELECT")) {
                q.executeSelect(line, isInsideTransaction);
            } else if (line.toUpperCase().startsWith("UPDATE")) {
                q.executeUpdate(line, isInsideTransaction);
            } else if (line.toUpperCase().startsWith("DELETE")) {
                q.executeDelete(line, isInsideTransaction);
            } else if (line.equalsIgnoreCase("BEGIN TRANSACTION;")) {
                isInsideTransaction = true;
                q.beginTransaction();
            } else if (line.equalsIgnoreCase("END TRANSACTION;")) {
                isInsideTransaction = false;
                q.endTransaction();
            } else if (line.equalsIgnoreCase("COMMIT;")) {
                q.executeCommit(isInsideTransaction);
            } else if (line.equalsIgnoreCase("ROLLBACK;")) {
                q.executeRollback(isInsideTransaction);
            } else {
                System.out.println("Invalid Query! " + line);
            }

        }
    }
}