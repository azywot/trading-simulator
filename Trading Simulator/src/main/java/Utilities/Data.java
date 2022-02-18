package Utilities;

import java.io.*;
import java.util.ArrayList;

/**
 * The singleton class representing data needed for the simulation.
 */
public class Data {
    private ArrayList<String> data = new ArrayList<String>();

    /**
     * Creates a Data structure i.e. an array list containing all lines from a given file.
     * @param filename String representing the filename.
     */
    public Data(String filename) {
        InputStream is = this.getClass().getResourceAsStream(filename);
        assert is != null;
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        String line;
        try{
            while ((line=r.readLine()) != null) {
                data.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getData() {
        return data;
    }
}
