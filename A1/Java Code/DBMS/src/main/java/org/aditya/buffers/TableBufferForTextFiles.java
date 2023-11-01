package org.aditya.buffers;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * The TableBufferForTextFiles class implements the TableBuffer interface for loading and saving table data
 * into buffer, for text files.
 */
public class TableBufferForTextFiles implements TableBuffer {

    /**
     * Load table data from a txt file into buffer(ArrayList of ArrayLists of strings).
     *
     * @param file source txt file, for loading data.
     * @return An ArrayList buffer which has table data. Each row is ArrayLists of strings.
     * @throws IOException if error occurs while file handling.
     */
    public ArrayList<ArrayList<String>> loadBuffer(File file) throws IOException {

        ArrayList<ArrayList<String>> tableData = new ArrayList<>();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String textRow = bufferedReader.readLine();
        while (textRow != null) {
            String[] cells = textRow.split("\\|");
            ArrayList<String> arrayListRow = new ArrayList<>();
            arrayListRow.addAll(Arrays.asList(cells));
            textRow = bufferedReader.readLine();
            tableData.add(arrayListRow);
        }
        bufferedReader.close();

        return tableData;
    }

    /**
     * Save table data to txt file from buffer(ArrayList of ArrayLists of strings).
     *
     * @param tableData buffer containing the Tabular data.
     * @param file destination txt file, for saving data.
     * @throws IOException if error occurs while file handling.
     */
    public void saveBuffer(ArrayList<ArrayList<String>> tableData, File file) throws IOException {
        FileWriter fileWriter = new FileWriter(file);
        for (ArrayList<String> row : tableData) {
            fileWriter.write(String.join("|", row) + "\n");
        }
        fileWriter.close();
    }
}
