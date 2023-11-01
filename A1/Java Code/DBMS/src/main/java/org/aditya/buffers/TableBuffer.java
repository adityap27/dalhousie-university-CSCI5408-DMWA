package org.aditya.buffers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * It declares methods for loading and saving table data using a ArrayList buffer.
 */
public interface TableBuffer {

    /**
     * Load table data from a file into buffer(ArrayList of ArrayLists of strings).
     *
     * @param file source file, for loading data.
     * @return An ArrayList buffer which has table data. Each row is ArrayLists of strings.
     * @throws IOException if error occurs while file handling.
     */
    ArrayList<ArrayList<String>> loadBuffer(File file) throws IOException;

    /**
     * Save table data to a file from buffer(ArrayList of ArrayLists of strings).
     *
     * @param tableData buffer containing the Tabular data.
     * @param file destination file, for saving data.
     * @throws IOException if error occurs while file handling.
     */
    void saveBuffer(ArrayList<ArrayList<String>> tableData, File file) throws IOException;
}
