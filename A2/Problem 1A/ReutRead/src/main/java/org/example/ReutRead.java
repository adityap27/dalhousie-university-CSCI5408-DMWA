package org.example;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import com.mongodb.MongoClient;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The ReutRead class provides method to read news articles from *.sgm files,
 * extract title and body using regular expressions, and store that in MongoDB database.
 * The main method creates connection to MongoDB Server, creates a database and collection,
 * then adds all news articles from 2 .sgm files, to the database.
 */
public class ReutRead {

    /**
     * Adds all news articles from a REUTERS file to a MongoDB database.
     *
     * @param fileName      Path to the file containing news articles.
     * @param mongoDatabase A MongoDB database object, which is connected.
     */
    public static void addArticlesToMongoDB(String fileName, MongoDatabase mongoDatabase) {
        try {
            // Read the file contents into a String.
            BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
            StringBuilder sgmFileContentTemp = new StringBuilder();

            String lineOfFile;
            while ((lineOfFile = bufferedReader.readLine()) != null) {
                sgmFileContentTemp.append(lineOfFile);
            }
            bufferedReader.close();

            String sgmFileContent = sgmFileContentTemp.toString();

            // Use regex to extract all text between all TITLE and BODY tags, under the parent tag: REUTERS.
            Pattern titleAndBodyPattern = Pattern.compile("(?i)<REUTERS[^>]*>[\\s\\S]*?<TITLE>([\\s\\S]*?)<\\/TITLE>[\\s\\S]*?<BODY>([\\s\\S]*?)<\\/BODY>[\\s\\S]*?<\\/REUTERS>");
            Matcher matcher = titleAndBodyPattern.matcher(sgmFileContent);

            // Create a list of article documents, where each document has an article title and body.
            List<Document> documentList = new ArrayList<Document>();
            while (matcher.find()) {
                Document document = new Document();
                document.append("title", matcher.group(1));
                document.append("body", matcher.group(2));
                documentList.add(document);
            }

            // Add all articles(list of documents) to the MongoDB.
            mongoDatabase.getCollection("articles").insertMany(documentList);

            // Print the success message with the count of articles added to the database.
            System.out.println("***********************************************************************");
            System.out.println("Successfully added " + documentList.size() + " news articles into MongoDB Database '" + mongoDatabase.getName() + "' from file " + fileName);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Entry point of the ReutRead Class. This creates a database connection and adds news articles to it using helper method.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {

        // Connect with MonoDB, create database and collection.
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        MongoDatabase mongoDatabase = mongoClient.getDatabase("ReuterDb");
        mongoDatabase.createCollection("articles");

        // Add articles from reut2-009.sgm file to ReuterDb mongoDB Database.
        ReutRead.addArticlesToMongoDB("./reut2-009.sgm", mongoDatabase);

        // Add articles from reut2-014.sgm file to ReuterDb mongoDB Database.
        ReutRead.addArticlesToMongoDB("./reut2-014.sgm", mongoDatabase);

    }
}