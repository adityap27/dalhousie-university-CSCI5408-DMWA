package org.aditya.captcha;

/**
 * This class implements the CaptchaGenerator interface to generate and validate simple string-based CAPTCHA challenges.
 */
public class SimpleStringCaptchaGenerator implements CaptchaGenerator {
    String currentCaptcha;

    /**
     * Generates a random string CAPTCHA which has alternating lowercase & uppercase letters.
     *
     * @return A string containing the CAPTCHA.
     */
    @Override
    public String generateCaptcha() {
        currentCaptcha = "";

        for (int i = 0; i < 3; i++) {
            currentCaptcha += String.valueOf((char) ('a' + Math.random() * 26));
            currentCaptcha += String.valueOf((char) ('A' + Math.random() * 26));
        }
        return currentCaptcha;
    }

    /**
     * Validates user input against a CAPTCHA.
     *
     * @param userInput string input of user, to be validated.
     * @return true if the input of user matches CAPTCHA else false.
     */
    @Override
    public boolean validateCaptcha(String userInput) {
        return currentCaptcha.equals(userInput);
    }
}
