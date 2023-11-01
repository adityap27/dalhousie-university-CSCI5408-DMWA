package org.aditya.authentication;

/**
 * The Authenticator declares methods for user authentication and sign-up.
 * The implementation class can choose the sign-up and login flow.
 */
public interface Authenticator {

    /**
     * Authenticates a user based on some credentials.
     *
     * @return true if valid credentials else false.
     */
    boolean authenticate();

    /**
     * Registers a new user.
     */
    void signUp();

}
