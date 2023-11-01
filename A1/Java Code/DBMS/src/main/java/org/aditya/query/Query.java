package org.aditya.query;

import org.aditya.Constants;
import org.aditya.buffers.TableBuffer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Query class has methods to execute SQL queries. Like DDL: CREATE, DML: SELECT, INSERT, UPDATE, DELETE and Transactions: BEGIN, END, COMMIT, ROLLBACK.
 */
public class Query {

    /**
     * Buffer for transaction management.
     */
    TableBuffer tableBuffer;

    /**
     * Constructor which allows to choose a specific buffer type for Query processing for transactions.
     *
     * @param tableBuffer the type of TableBuffer to bes used.
     */
    public Query(TableBuffer tableBuffer) {
        this.tableBuffer = tableBuffer;
    }

    /**
     * Executes a CREATE TABLE query.
     *
     * @param line                query string.
     * @param isInsideTransaction true if inside a transaction, false if outside the transaction. If within transaction, a buffer is used to support commit & rollback.
     * @throws IOException if error occurs while file handling.
     */
    public void executeCreate(String line, boolean isInsideTransaction) throws IOException {
        String currentDB = isInsideTransaction ? Constants.DB_PATH + "_transaction_buffer" : Constants.DB_PATH;
        Pattern p = Pattern.compile("(?i)CREATE\\s+TABLE\\s+(\\w+)\\s*\\(([^;]+)\\);");
        Matcher m = p.matcher(line);

        // Only create the table if the whole CREATE query is correct in syntax.
        if (m.find()) {

            String tableName = m.group(1).toLowerCase();
            File file = new File(currentDB + "/" + tableName + ".txt");

            // Only create the table if the .txt file for that table doesn't exist yet.
            if (file.createNewFile()) {
                FileWriter fileWriter = new FileWriter(file);
                // Extract column name and make them pipe separated.
                String columnNamesHeader = m.group(2).toLowerCase().replaceAll("\\s*[^\\s.]+,\\s*", "|").split("\\s+")[0];
                fileWriter.write(columnNamesHeader + "\n");
                fileWriter.close();
                System.out.println("Table " + tableName + " created successfully.");
            } else {
                System.out.println("ERROR: Table '" + tableName + "' already exists.");
            }
        } else {
            System.out.println("ERROR: Your CREATE TABLE query syntax is incorrect.");
        }
    }

    /**
     * Executes a INSERT into TABLE_NAME query. Values for each column are required.
     *
     * @param line                query string.
     * @param isInsideTransaction true if inside a transaction, false if outside the transaction. If within transaction, a buffer is used to support commit & rollback.
     * @throws IOException if error occurs while file handling.
     */
    public void executeInsert(String line, boolean isInsideTransaction) throws IOException {
        String currentDB = isInsideTransaction ? Constants.DB_PATH + "_transaction_buffer" : Constants.DB_PATH;
        Pattern p = Pattern.compile("(?i)INSERT\\s+INTO\\s+(\\w+)\\s+VALUES\\s*\\(([^)]+)\\);");
        Matcher m = p.matcher(line);

        // Only insert in the table if the whole INSERT query is correct in syntax.
        if (m.find()) {

            String tableName = m.group(1).toLowerCase();
            File file = new File(currentDB + "/" + tableName + ".txt");

            // Only insert in the table if the .txt file for that table exists.
            if (file.exists()) {
                FileWriter fileWriter = new FileWriter(file, true);
                // Get the values and use the delimiter pipe before adding row to file.
                String row = m.group(2).replaceAll("\\s*,\\s*", "|");

                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                String header = bufferedReader.readLine();
                bufferedReader.close();
                // Check column count with value count before inserting.
                if (header.split("\\|").length == row.split("\\|").length) {
                    fileWriter.write(row.replaceAll("'", "") + "\n");
                    fileWriter.close();
                    System.out.println("1 row inserted in Table " + tableName + " successfully.");
                } else {
                    fileWriter.close();
                    System.out.println("ERROR: List of Values doesn't match the Table '" + tableName + "' column count.");
                }

            } else {
                System.out.println("ERROR: Table '" + tableName + "' does not exist.");
            }
        } else {
            System.out.println("ERROR: Your INSERT query syntax is incorrect.");
        }
    }

    /**
     * Executes a SELECT * FROM TABLE_NAME query. WHERE clause is supported with 1 column condition and is optional.
     *
     * @param line                query string.
     * @param isInsideTransaction true if inside a transaction, false if outside the transaction. If within transaction, a buffer is used to support commit & rollback.
     * @throws IOException if error occurs while file handling.
     */
    public void executeSelect(String line, boolean isInsideTransaction) throws IOException {
        String currentDB = isInsideTransaction ? Constants.DB_PATH + "_transaction_buffer" : Constants.DB_PATH;
        Pattern p = Pattern.compile("(?i)SELECT\\s+\\*\\s+FROM\\s+(\\w+)(?:\\sWHERE\\s(\\w+=.*))?;");
        Matcher m = p.matcher(line);

        // Only SELECT the table if the whole SELECT query is correct in syntax.
        if (m.find()) {

            String tableName = m.group(1).toLowerCase();
            String conditionColumn = null;
            String conditionValue = null;
            int conditionColumnIndex = -1;

            if (m.group(2) != null) {
                String[] condition = m.group(2).split("=");
                conditionColumn = condition[0].toLowerCase();
                conditionValue = condition[1].replace("'", "");
            }
            String condition = m.group(2);
            File file = new File(currentDB + "/" + tableName + ".txt");

            // Only SELECT the table if the .txt file for that table exists.
            if (file.exists()) {
                ArrayList<ArrayList<String>> tableData = tableBuffer.loadBuffer(file);

                conditionColumnIndex = tableData.get(0).indexOf(conditionColumn);
                // Throw error if column specified in WHERE is not valid.
                if (conditionColumn != null && conditionColumnIndex == -1) {
                    System.out.println("ERROR: Column '" + conditionColumn + "' in WHERE clause, does not exist.");
                    return;
                }

                System.out.println(tableData.get(0).toString().replace("[", "").replace("]", "").replace(", ", " | "));
                System.out.println("-".repeat(tableData.get(0).toString().length()));

                for (int i = 1; i < tableData.size(); i++) {
                    ArrayList<String> row = tableData.get(i);
                    if (conditionColumnIndex != -1 && !(row.get(conditionColumnIndex).equals(conditionValue))) {
                        continue;
                    }
                    String rowString = row.toString().replace("[", "").replace("]", "").replace(", ", " | ");
                    System.out.println(rowString);
                }
            } else {
                System.out.println("ERROR: Table '" + tableName + "' does not exist.");
            }
        } else {
            System.out.println("ERROR: Your SELECT query syntax is incorrect.");
        }
    }

    /**
     * Executes a UPDATE TABLE_NAME query. SET and WHERE clauses are supported with 1 column condition.
     *
     * @param line                query string.
     * @param isInsideTransaction true if inside a transaction, false if outside the transaction. If within transaction, a buffer is used to support commit & rollback.
     * @throws IOException if error occurs while file handling.
     */
    public void executeUpdate(String line, boolean isInsideTransaction) throws IOException {
        String currentDB = isInsideTransaction ? Constants.DB_PATH + "_transaction_buffer" : Constants.DB_PATH;
        Pattern p = Pattern.compile("(?i)UPDATE\\s+(\\w+)\\s+SET\\s+(\\w+=.+)\\s+WHERE\\s+(\\w+=.+);");
        Matcher m = p.matcher(line);

        // Only UPDATE the table if the whole UPDATE query is correct in syntax.
        if (m.find()) {

            String tableName = m.group(1).toLowerCase();

            String[] condition = m.group(3).split("=");
            String conditionColumn = condition[0].toLowerCase();
            String conditionValue = condition[1].replace("'", "");
            int conditionColumnIndex = -1;

            String[] setPair = m.group(2).split("=");
            String setColumn = setPair[0].toLowerCase();
            String setValue = setPair[1].replace("'", "");
            int setColumnIndex = -1;

            File file = new File(currentDB + "/" + tableName + ".txt");

            // Only UPDATE the table if the .txt file for that table exists.
            if (file.exists()) {
                ArrayList<ArrayList<String>> tableData = tableBuffer.loadBuffer(file);

                setColumnIndex = tableData.get(0).indexOf(setColumn);
                conditionColumnIndex = tableData.get(0).indexOf(conditionColumn);
                // Throw error if column specified in SET OR WHERE is not valid.
                if (setColumnIndex == -1) {
                    System.out.println("ERROR: Column '" + setColumn + "' in SET clause, does not exist.");
                    return;
                }
                if (conditionColumnIndex == -1) {
                    System.out.println("ERROR: Column '" + conditionColumn + "' in WHERE clause, does not exist.");
                    return;
                }
                int count = 0;
                for (int i = 1; i < tableData.size(); i++) {
                    ArrayList<String> row = tableData.get(i);
                    if (row.get(conditionColumnIndex).equals(conditionValue)) {
                        row.set(setColumnIndex, setValue);
                        count++;
                    }
                }
                tableBuffer.saveBuffer(tableData, file);
                System.out.println(count + " rows updated successfully!");
            } else {
                System.out.println("ERROR: Table '" + tableName + "' does not exist.");
            }
        } else {
            System.out.println("ERROR: Your UPDATE query syntax is incorrect.");
        }
    }

    /**
     * Executes a DELETE FROM TABLE_NAME query. WHERE clause is supported with 1 column condition.
     *
     * @param line                query string.
     * @param isInsideTransaction true if inside a transaction, false if outside the transaction. If within transaction, a buffer is used to support commit & rollback.
     * @throws IOException if error occurs while file handling.
     */
    public void executeDelete(String line, boolean isInsideTransaction) throws IOException {
        String currentDB = isInsideTransaction ? Constants.DB_PATH + "_transaction_buffer" : Constants.DB_PATH;
        Pattern p = Pattern.compile("(?i)DELETE\\s+FROM\\s+(\\w+)\\s+WHERE\\s+(\\w+=.+);");
        Matcher m = p.matcher(line);

        // Only DELETE the table if the whole DELETE query is correct in syntax.
        if (m.find()) {

            String tableName = m.group(1).toLowerCase();

            String[] condition = m.group(2).split("=");
            String conditionColumn = condition[0].toLowerCase();
            String conditionValue = condition[1].replace("'", "");
            int conditionColumnIndex = -1;

            File file = new File(currentDB + "/" + tableName + ".txt");

            // Only DELETE the table if the .txt file for that table exists.
            if (file.exists()) {
                ArrayList<ArrayList<String>> tableData = tableBuffer.loadBuffer(file);

                conditionColumnIndex = tableData.get(0).indexOf(conditionColumn);
                // Throw error if column specified in WHERE is not valid.
                if (conditionColumnIndex == -1) {
                    System.out.println("ERROR: Column '" + conditionColumn + "' in WHERE clause, does not exist.");
                    return;
                }
                int count = 0;
                for (int i = 1; i < tableData.size(); i++) {
                    ArrayList<String> row = tableData.get(i);
                    if (row.get(conditionColumnIndex).equals(conditionValue)) {
                        tableData.remove(i);
                        i--;
                        count++;
                    }
                }
                tableBuffer.saveBuffer(tableData, file);
                System.out.println(count + " rows deleted successfully!");
            } else {
                System.out.println("ERROR: Table '" + tableName + "' does not exist.");
            }
        } else {
            System.out.println("ERROR: Your DELETE query syntax is incorrect.");
        }
    }

    /**
     * Starts a database transaction. Initializes a buffer database. Buffer is a copy of actual database, where all further transactions will take place.
     *
     * @throws IOException if error occurs while file handling.
     */
    public void beginTransaction() throws IOException {
        System.out.println("You have entered the Transaction.");
        System.out.println("Run END TRANSACTION; to exit the transaction flow.");
        System.out.println("Run commit; to save your changes.");
        System.out.println("Run rollback; to save your changes.");

        // Copy db to a buffer.
        File actualDB = new File(Constants.DB_PATH);
        File bufferDB = new File(Constants.DB_PATH + "_transaction_buffer");
        bufferDB.mkdir();
        for (File f : actualDB.listFiles()) {
            Files.copy(f.toPath(), bufferDB.toPath().resolve(f.getName()), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * End a database transaction. The buffer database is deleted. All the un-committed work will be lost.
     *
     * @throws IOException if error occurs while file handling.
     */
    public void endTransaction() throws IOException {
        System.out.println("You have exited the Transaction.");

        File bufferDB = new File(Constants.DB_PATH + "_transaction_buffer");

        // Delete buffer;
        for (File f : bufferDB.listFiles()) {
            f.delete();
        }
        bufferDB.delete();
    }

    /**
     * Executes a COMMIT command. Saves all the buffer changes to the actual database.
     *
     * @param isInsideTransaction true if inside a transaction, false if outside the transaction. If within transaction, commit is allowed, else not allowed.
     * @throws IOException if error occurs while file handling.
     */
    public void executeCommit(boolean isInsideTransaction) throws IOException {
        if (!isInsideTransaction) {
            System.out.println("ERR: can't commit outside transaction.");
            return;
        }
        // Copy buffer to DB;
        File actualDB = new File(Constants.DB_PATH);
        File bufferDB = new File(Constants.DB_PATH + "_transaction_buffer");
        for (File f : bufferDB.listFiles()) {
            Files.copy(f.toPath(), actualDB.toPath().resolve(f.getName()), StandardCopyOption.REPLACE_EXISTING);
        }
        System.out.println("Commit Successfully completed.");
    }

    /**
     * Executes a ROLLBACK command. Remove all buffer changes by over-writing the actual database content to buffer.
     *
     * @param isInsideTransaction true if inside a transaction, false if outside the transaction. If within transaction, rollback is allowed, else not allowed.
     * @throws IOException if error occurs while file handling.
     */
    public void executeRollback(boolean isInsideTransaction) throws IOException {
        if (!isInsideTransaction) {
            System.out.println("ERR: can't rollback outside transaction.");
            return;
        }

        // Copy db to a buffer.
        File actualDB = new File(Constants.DB_PATH);
        File bufferDB = new File(Constants.DB_PATH + "_transaction_buffer");
        bufferDB.mkdir();
        for (File f : actualDB.listFiles()) {
            Files.copy(f.toPath(), bufferDB.toPath().resolve(f.getName()), StandardCopyOption.REPLACE_EXISTING);
        }
        System.out.println("Rollback Successfully completed.");
    }

}
