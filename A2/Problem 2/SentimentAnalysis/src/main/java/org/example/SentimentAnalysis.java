package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SentimentAnalysis class performs sentiment analysis on a collection of news article titles.
 */
public class SentimentAnalysis {

    /**
     * Reads the file content and returns it as string.
     *
     * @param fileName Name of file to read.
     * @return File contents as string
     */
    public static String readFile(String fileName) throws Exception {

        // Read the file contents into a String.
        BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
        StringBuilder sgmFileContentTemp = new StringBuilder();

        // Keep reading until the end of file
        String lineOfFile;
        while ((lineOfFile = bufferedReader.readLine()) != null) {
            sgmFileContentTemp.append(lineOfFile);
        }
        bufferedReader.close();

        // Return the file contents as a string.
        return sgmFileContentTemp.toString();
    }

    /**
     * Reads a file containing a list of words and returns them as List<String>
     *
     * @param fileName Name of file to read.
     * @return List<String> containing the words.
     */
    public static List<String> readWordsFile(String fileName) throws Exception {

        // Read the file contents into a String.
        BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
        List<String> wordList = new ArrayList<String>();
        String word;

        // Keep reading until the end of file
        while ((word = bufferedReader.readLine()) != null) {
            if (word.startsWith(";") || word.isEmpty()) {
                continue;
            }
            wordList.add(word);
        }
        bufferedReader.close();

        // Return the List<String>
        return wordList;
    }

    /**
     * Calculates bag of words from a given title.
     *
     * @param title the title of the news article.
     * @return A Map<String, Integer> as the bag of words with word counts.
     */
    public static Map<String, Integer> getBagOfWords(String title) {

        // Hashmap to store the word counts for each word
        Map<String, Integer> bagOfWords = new HashMap<>();

        String wordsInTitle[] = title.split("\\s+");

        // Count each word from the title.
        for (String word : wordsInTitle) {

            // Get the current count
            int currentCount = bagOfWords.getOrDefault(word, 0);

            // Increase the specific word count in the hashmap.
            bagOfWords.put(word, currentCount + 1);
        }

        return bagOfWords;
    }

    /**
     * Checks bag of words and returns matching words and score as per the positive and negative word lists.
     *
     * @param bagOfWords       The bag of words to check.
     * @param positiveWordList List of positive words.
     * @param negativeWordList List of negative words.
     * @return A Map<String, Object> containing matches and overall sentiment score.
     */
    public static Map<String, Object> getMatchesAndScore(Map<String, Integer> bagOfWords,
                                                         List<String> positiveWordList,
                                                         List<String> negativeWordList) {
        // Create a HashMap to store matches and score
        Map<String, Object> output = new HashMap<>();

        List<String> matches = new ArrayList<>();
        int overallScore = 0;

        outerLoop:
        for (String word : bagOfWords.keySet()) {

            // Check if current word is positive.
            for (String positiveWord : positiveWordList) {
                if (positiveWord.toUpperCase().equals(word.toUpperCase())) {
                    // Add a match
                    matches.add(word);
                    // Add count of that word into overall score.
                    overallScore = overallScore + bagOfWords.get(word);

                    // If the word is positive, no need to check negative list. Just start matching process for next word.
                    continue outerLoop;
                }
            }

            // Check if current word is negative.
            for (String negativeWord : negativeWordList) {
                if (negativeWord.toUpperCase().equals(word.toUpperCase())) {
                    // Add a match
                    matches.add(word);

                    // Subtract count of that word from overall score.
                    overallScore = overallScore - bagOfWords.get(word);
                }
            }

        }

        output.put("Matches", matches);
        output.put("Score", overallScore);

        return output;
    }

    /**
     * The main method that performs sentiment analysis on news article titles.
     */
    public static void main(String[] args) throws Exception {

        // Read the content of both the files.
        String fileContent = "";
        try {
            fileContent = readFile("./reut2-009.sgm");
            fileContent = fileContent + readFile("./reut2-014.sgm");

        } catch (Exception e) {
            System.out.println(e);
        }

        // Load the positive words and negative word in a List of Strings.
        List<String> positiveWordList = null;
        List<String> negativeWordList = null;
        try {

            positiveWordList = readWordsFile("./positive-words.txt");
            negativeWordList = readWordsFile("./negative-words.txt");

        } catch (Exception e) {
            System.out.println(e);
        }

        // Use regex to extract all text TITLE start and end tags, under the parent tag: REUTERS.
        Pattern titlePattern = Pattern.compile("(?i)<REUTERS[^>]*>[\\s\\S]*?<TITLE>([\\s\\S]*?)<\\/TITLE>[\\s\\S]*?<\\/REUTERS>");
        Matcher matcher = titlePattern.matcher(fileContent);

        // Create an excel sheet to store sentiment data
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sentiment Analysis");

        // Create the header row in the Excel sheet.
        String[] headers = {"News#", "Title Content", "match", "score", "Polarity"};
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            row.createCell(i).setCellValue(headers[i]);
        }

        // Process all the titles
        for (int i = 1; matcher.find(); i++) {

            String title = matcher.group(1);

            // Clean the title: uppercase and remove some special characters.
            title = title.toUpperCase().replaceAll("&LT;", "").replaceAll(">", "").replaceAll(",", "");

            // Find bag of words.
            Map<String, Integer> bagOfWords = getBagOfWords(title);

            // Print the news title, along with its bag of words
            System.out.println(String.format("news%d = \"%s\"", i, title));
            System.out.println(String.format("bow%d = %s", i, bagOfWords));

            // Get matches and score
            Map<String, Object> output = getMatchesAndScore(bagOfWords, positiveWordList, negativeWordList);
            String matches = String.join(", ", (List<String>) output.get("Matches"));
            int score = (Integer) output.get("Score");

            // Print the matches and scores.
            System.out.println(String.format("Matches%d = %s", i, matches));
            System.out.println(String.format("Score%d = %s", i, score));

            // Calculate and print polarity
            String polarity = "";
            if (score > 0) {
                polarity = "Positive";
            } else if (score < 0) {
                polarity = "Negative";
            } else {
                polarity = "Neutral";
            }

            System.out.println(String.format("Polarity%d = %s\n", i, polarity));

            // Make the entry of article's sentiment analysis in Excel sheet as row.
            row = sheet.createRow(i);
            row.createCell(0).setCellValue(i);
            row.createCell(1).setCellValue(title);
            row.createCell(2).setCellValue(matches);
            row.createCell(3).setCellValue(score);
            row.createCell(4).setCellValue(polarity);
        }

        // Autosize columns in the Excel sheet before saving
        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }

        // Save the Excel file and close the connection.
        FileOutputStream fileOutputStream = new FileOutputStream(new File("./SentimentAnalysis.xlsx"));
        workbook.write(fileOutputStream);
        fileOutputStream.close();
        System.out.println("Sentiment Analysis Report is generated in file: ./SentimentAnalysis.xlsx");
    }
}