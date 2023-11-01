package org.aditya.authentication;

import org.aditya.Constants;
import org.aditya.captcha.CaptchaGenerator;
import org.aditya.hashing.HashingAlgorithm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * Implementation of Authenticator interface with two-factor sign-up and login flow. It uses a captcha for both flows and stores password in hash.
 */
public class TwoFactorAuthenticator implements Authenticator {

    /**
     * The generator for creating and validating captcha.
     */
    CaptchaGenerator captchaGenerator;

    /**
     * The hash algorithm for password hashing.
     */
    HashingAlgorithm hashingAlgorithm;

    /**
     * Creates a new TwoFactorAuthenticator object with provided CaptchaGenerator and HashingAlgorithm.
     *
     * @param captchaGenerator The CaptchaGenerator for CAPTCHA operations.
     * @param hashingAlgorithm The HashingAlgorithm for password hashing.
     */
    public TwoFactorAuthenticator(CaptchaGenerator captchaGenerator, HashingAlgorithm hashingAlgorithm) {
        this.captchaGenerator = captchaGenerator;
        this.hashingAlgorithm = hashingAlgorithm;
    }

    /**
     * Authenticates a user using password as well as captcha.
     *
     * @return true if the user is successfully authenticated, false otherwise.
     */
    @Override
    public boolean authenticate() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Username:");
        String username = scanner.nextLine();

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(Constants.USERS_FILE_PATH));
            String userEntry = bufferedReader.readLine();
            boolean userfound = false;
            while (userEntry != null) {
                String[] userDetails = userEntry.split("\\|");
                if (userDetails[0].equalsIgnoreCase(username)) {
                    userfound = true;
                    break;
                }
                userEntry = bufferedReader.readLine();
            }
            if(!userfound)
            {
                System.out.print("ERR: Username does not exist.");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.print("Enter Password:");
        String password = scanner.nextLine();

        while (true) {
            System.out.println("Captcha:" + captchaGenerator.generateCaptcha());
            System.out.print("Enter Captcha:");
            String userWrittenCaptcha = scanner.nextLine();
            if (captchaGenerator.validateCaptcha(userWrittenCaptcha)) {
                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(Constants.USERS_FILE_PATH));
                    String userEntry = bufferedReader.readLine();
                    boolean userValid = false;
                    while (userEntry != null) {
                        String[] userDetails = userEntry.split("\\|");
                        if (userDetails[0].equals(username) && userDetails[1].equals(hashingAlgorithm.getHash(password))) {
                            userValid = true;
                            break;
                        }
                        userEntry = bufferedReader.readLine();
                    }
                    if(!userValid)
                    {
                        System.out.print("ERR: Invalid Password.");
                        return false;
                    }
                    else {
                        return true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.print("Incorrect Captcha! Try Again.\n");
            }
        }
    }

    /**
     * Creates a new user. Username, password and captcha will be asked from the user.
     */
    @Override
    public void signUp() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Username:");
        String username = scanner.nextLine();

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(Constants.USERS_FILE_PATH));
            String userEntry = bufferedReader.readLine();
            while (userEntry != null) {
                String[] userDetails = userEntry.split("\\|");
                if (userDetails[0].equalsIgnoreCase(username)) {
                    System.out.print("ERR: Username already exists.");
                    return;
                }
                userEntry = bufferedReader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.print("Enter Password:");
        String password = scanner.nextLine();
        while (true) {
            System.out.println("Captcha:" + captchaGenerator.generateCaptcha());
            System.out.print("Enter Captcha:");
            String userWrittenCaptcha = scanner.nextLine();
            if (captchaGenerator.validateCaptcha(userWrittenCaptcha)) {
                try {
                    FileWriter fileWriter = new FileWriter(Constants.USERS_FILE_PATH, true);
                    String passwordHash = hashingAlgorithm.getHash(password);
                    fileWriter.write(username + "|" + passwordHash + "\n");
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Signup successfully completed!");
                return;
            } else {
                System.out.print("Incorrect Captcha! Try Again.\n");
            }
        }

    }
}
