package org.aditya.captcha;

/**
 * This interface declares methods for generating and validating CAPTCHA.
 */
public interface CaptchaGenerator {
    /**
     * Generates a random CAPTCHA.
     *
     * @return A string containing the CAPTCHA.
     */
    String generateCaptcha();

    /**
     * Validates user input against a CAPTCHA.
     *
     * @param userInput Input of user, to be validated.
     * @return true if the input of user matches CAPTCHA else false.
     */
    boolean validateCaptcha(String userInput);
}
