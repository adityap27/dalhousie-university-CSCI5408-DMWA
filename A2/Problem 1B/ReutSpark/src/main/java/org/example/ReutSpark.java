package org.example;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.spark.sql.functions.col;

/**
 * Spark app for finding word frequencies.
 */
public class ReutSpark {

    /**
     * Reads a stop words file and returns a list of stop words.
     *
     * @param fileName Name of the file that contains stop words
     * @return List of String of stop words.
     * @throws Exception If an error occurs during file reading.
     */
    public static List<String> readStopWordsFile(String fileName) throws Exception {

        // Read the file contents into a String.
        BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
        List<String> wordList = new ArrayList<>();
        String word;

        // Take a word from each line
        while ((word = bufferedReader.readLine()) != null) {
            if (word.startsWith("#") || word.isEmpty()) {
                continue;
            }
            wordList.add(word.toUpperCase());
        }
        bufferedReader.close();

        // Return the list of stop words.
        return wordList;
    }

    /**
     * Cleans a line, splits it into words, and removes stop words.
     *
     * @param line A String line to be cleaned.
     * @return A String array of non-stop words from the input String line.
     */
    private static String[] cleanAndSplitIntoWords(String line) {
        // Make all chars uppercase
        line = line.toUpperCase();

        // Replace tags with spaces
        line = line.replaceAll("<[^>]+>", " ");

        // Remove all special symbols
        line = line.replaceAll("[^A-Z\\s]", "");

        // Split into words
        String[] words = line.split("\\s+");

        // Load all the stop words from the file
        List<String> stopWords = null;
        try {
            stopWords = readStopWordsFile("./stopwords.txt");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // List to store non-stop words
        List<String> filteredWordsList = new ArrayList<>();

        // Filter out any stop words
        for (String word : words) {
            if (!stopWords.contains(word) && !word.isEmpty()) {
                filteredWordsList.add(word);
            }
        }

        // Return string array of non-stop words in upper case, without any special chars or tags.
        return filteredWordsList.toArray(String[]::new);
    }

    /**
     * Main method for running Spark app.
     */
    public static void main(String[] args) {
        // Create Spark configuration and context
        SparkConf sparkConf = new SparkConf().setAppName("ReutSpark").setMaster("local[*]");
        JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);

        // Create a Spark session
        SparkSession spark = SparkSession.builder().appName("ReutSpark").getOrCreate();

        // Read text file into a Dataset of strings
        Dataset<String> lines = spark.read().textFile("file:///home/adityapurohit27/reut2-009.sgm");

        // Split each line into words
        Dataset<String> words = lines.flatMap((FlatMapFunction<String, String>) line -> Arrays.asList(cleanAndSplitIntoWords(line)).iterator(), Encoders.STRING());

        // Group by word and count frequency.
        Dataset<Row> wordCounts = words.groupBy("value").count();

        // Print all word frequencies.
        System.out.println("All word counts:");
        wordCounts.show(Integer.MAX_VALUE, false);

        // Get the highest count
        System.out.println("\nHighest Frequency word:");
        System.out.println(wordCounts.orderBy(col("count").desc()).first());

        // Get the lowest count
        System.out.println("\nLowest Frequency word:");
        System.out.println(wordCounts.orderBy(col("count").asc(), col("value").asc()).first());

        // Stop the Spark session and context
        spark.stop();
        sparkContext.stop();
    }
}
