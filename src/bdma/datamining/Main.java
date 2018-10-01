/**
 * Université Libre de Bruxelles (ULB)
 * Master Program in Big Data Management & Analytics (BDMA)
 *
 * Course: Data mining
 * Assignment 1: ID3 Algorithm implementation.
 * Author: Sokratis Papadopoulos
 * Date: 29 September 2018
 */

package bdma.datamining;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Main {

    private static int rowCount = 0; //total rows of input data file
    private static int colCount = 0; //total columns of input data file
    private static String[][] data; //data structure to store initial table
    private static String path; //filepath for input data file
    private static int classCol; //specifies the label column
    private static HashMap<String, Integer> allClassValues = new HashMap<>(); //stores all the class values along with the occurrences on initial table.
    private static ArrayList<String> initialHeader; //specifies initial input attributes

    public static void main(String[] args) throws IOException {
        //classCol = 6; path = "car.data";

        if(args.length != 2){
            System.out.println("Please enter 2 arguments: <class column number> and <file path> of input data.");
            System.exit(0);
        } else {
            try {
                classCol = Integer.parseInt(args[0]);
            } catch (NumberFormatException ex){
                System.out.println("Please enter a valid number.");
                System.exit(0);
            }
            path = args[1]; // performs path check on the readData() function.
        }

        readData(); insertData(); initializeHeader(); // reading Input
        if(classCol>=colCount){ //check if given class column exists in the given table
            System.out.println("Class column must be between 0 and " + (colCount-1) + " starting counting from 0");
            System.exit(0);
        }
        allClassValues = countValuesInColumn(data,classCol); //stores all the class values along with the occurrences on initial table.
        id3(initialHeader,classCol,data, 0); //executing the algorithm
    }


    /**
     * Main function of the program, computes the ID3 out of the input header, label attribute and input table.
     * It also uses an extra 'level' parameter to print out the decision tree properly.
     * @param header
     * @param classCol
     * @param subdata
     * @param level
     */
    private static void id3(ArrayList<String> header, int classCol, String[][] subdata, int level){

        if (subdata.length == 0){ //Training data is empty.
            for(int k=0;k<level; k++){System.out.print("| ");}
            System.out.println("Training data empty.");
            return;
        }

        if (classSimilarityInNode(subdata)){ // Training data is homogeneous (have one class value and only)
            String otherClassValues = "";
            for (Map.Entry<String, Integer> entry : allClassValues.entrySet()) {
                if(!entry.getKey().equals(subdata[0][classCol])){
                    otherClassValues += entry.getKey() + "=0, ";
                }
            }
            otherClassValues = otherClassValues.substring(0,otherClassValues.length()-2) + "}";

            for(int k=0;k<level; k++){System.out.print("| ");}
            System.out.println("Leaf: {" + subdata[0][classCol] + "=" + subdata.length + ", " + otherClassValues);
            return;
        }

        if(header.isEmpty()){ // Input column is empty.
            HashMap<String, Integer> values = countValuesInColumn(subdata, classCol);
            String maxClass = null;
            Integer maxValue = 0;
            String leafValues = "{";

            for (Map.Entry<String, Integer> entry : values.entrySet()) {
                if(entry.getValue() > maxValue){
                    maxClass = entry.getKey();
                    maxValue = entry.getValue();
                }
                leafValues += entry.getKey() + "=" + entry.getValue() + ", ";
            }

            leafValues = leafValues.substring(0,leafValues.length()-2) + "}";


            for(int k=0;k<level; k++){System.out.print("| ");}
            System.out.println("Leaf: " + leafValues + " (No more Input, dominant class: " + maxClass + ")");
            return;
        }

        double entropy = calculateEntropy(subdata);
        int winningColumn = compareGains(entropy, subdata, header);
        HashMap<String, Integer> maxGainColValues = countValuesInColumn(subdata, winningColumn);

        String removalColumn = "attr" + winningColumn;
        header.remove(removalColumn); //removes split column from the current Input
        for (Map.Entry<String, Integer> entry : maxGainColValues.entrySet()) {
            String[][] tempData = new String[entry.getValue()][colCount];
            int cnt = 0;
            for (int t=0; t<subdata.length; t++){
                if(subdata[t][winningColumn].equals(entry.getKey())) {
                    tempData[cnt++] = subdata[t];
                }
            }
            for(int k=0;k<level; k++){System.out.print("| ");}
            System.out.println("attr" + winningColumn + "=" + entry.getKey());

            id3(header,classCol,tempData, level+1);
        }
        header.add("attr"+winningColumn); //adds header back to the data structure for the rest root attribute cases to execute.
    }

    /**
     * Checks if all records in node have the same class value.
     * @param subdata
     * @return
     */
    private static boolean classSimilarityInNode(String[][] subdata){
        if(subdata.length == 1){
            return true;
        } else {
            String value = subdata[0][classCol];
            for(int k=0; k<subdata.length; k++){
                if (!value.equals(subdata[k][classCol])){
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * calculates the gain for each potential column-split and decides which is the best split.
     * @param entropy
     * @param subdata
     * @return the input column (attribute) number, based on which the split has best gain.
     */
    private static int compareGains(double entropy, String[][] subdata, ArrayList<String> header) {
        HashMap<Integer,Double> gains = new HashMap<>();
        for (int j=0; j<colCount; j++){
            if(j!= classCol && header.contains("attr"+j)) {
                gains.put(j, calculateGain(entropy, subdata, j));
            }
        }

        //to be used to compare gains from all columns/attr
        int maxGainColumn = -100;
        double maxGain = -100;
        for (Map.Entry<Integer, Double> entry : gains.entrySet()) {
            if (entry.getValue() > maxGain){
                maxGain = entry.getValue();
                maxGainColumn = entry.getKey();
            }
            //System.out.println("Gain(" + entry.getKey() + "):" + entry.getValue() );
        }

        return maxGainColumn;
    }

    /**
     * Stores in a hashmap the unique class values along with the timesOccured and calculates entropy.
     * @param subdata
     * @return the calculated entropy of class value in the given table.
     */
    private static double calculateEntropy(String[][] subdata) {

        double entropy = 0;
        HashMap<String, Integer> classValues = countValuesInColumn(subdata,classCol);

        for (Map.Entry<String, Integer> entry : classValues.entrySet()) {
            double fraction = (double) entry.getValue() / subdata.length;
            entropy += (-1)* fraction*Math.log(fraction)/Math.log(2);
        }
        return entropy;
    }

    /**
     * Calculates the gain of a given table and split-column
     * @param entropy
     * @param subdata
     * @param column
     * @return calculated gain for the give table & split column
     */
    private static double calculateGain(double entropy, String[][] subdata, int column) {
        double gain = 0;
        double calcEntropySplit = 0;

        HashMap<String, Integer> columnValues = countValuesInColumn(subdata,column);
        HashMap<String, Double> entropySplit = new HashMap<>();

        for (String key : columnValues.keySet()) {
            String[][] tempSubData = new String[columnValues.get(key)][colCount];
            int counter = 0;
            for (int k = 0; k < subdata.length; k++) {
                if (subdata[k][column].equals(key)){
                    tempSubData[counter++] = subdata[k];
                }
            }
            columnValues.put(key,counter);
            entropySplit.put(key,calculateEntropy(tempSubData));
        }

        for (Map.Entry<String, Double> es : entropySplit.entrySet()) {
            double temp = columnValues.get(es.getKey());
            double temp2 = subdata.length;
            double temp3 = entropySplit.get(es.getKey());
            calcEntropySplit += (temp/temp2) *temp3;
        }

        gain = entropy - calcEntropySplit;
        return gain;
    }

    /**
     * Calculates the values of column requested (not class) along with the number of times each value is presented.
     * @param subdata
     * @param column
     * @return the unique values of given table column along with number of times occurred.
     */
    private static HashMap<String, Integer> countValuesInColumn(String[][] subdata, int column){
        HashMap<String, Integer> values = new HashMap<>();
        for (int i=0; i<subdata.length; i++){
            if(!values.containsKey(subdata[i][column])){
                values.put(subdata[i][column],1);
            } else {
                values.replace(subdata[i][column], values.get(subdata[i][column])+1);
            }
        }
        return values;
    }

    /**
     * Initiating a boolean array for each column as a flag of whether its used on splitting or not.
     * I exclude the class value by signing it as used.
     */
    private static void initializeHeader() {
        initialHeader = new ArrayList<>();
        for(int u=0; u<colCount; u++){
            if(u!=classCol) {
                String temp = "attr"+u;
                initialHeader.add(temp);
            }
        }
    }

    /**
         * Inserts the data into a data structure (2d array)
         * @throws IOException
         */
    private static void insertData() throws IOException {
        String line;
        data = new String[rowCount][colCount];

        BufferedReader br = new BufferedReader(new FileReader(path));

        for(int i=0; i<rowCount; i++){
            line = br.readLine();
            String[] attr = line.split(",");
            for (int j=0;j<colCount; j++) {
                data[i][j] = attr[j];
            }
        }
        br.close();
    }

    /**
     * Reads the data from the input file and count rows + columns
     * @throws IOException
     */
    private static void readData(){
        String line= "";
        try(BufferedReader br = new BufferedReader(new FileReader(path))) {
            //read first line to get column count and starting row count.
            if ((line = br.readLine()) != null) {
                rowCount++;
                String[] attr = line.split(",");
                colCount = attr.length;
            }

            //count the rows of the input file
            while ((line = br.readLine()) != null) {
                rowCount++;
            }
            br.close();
        } catch (IOException ex){
            System.out.println("Please enter a valid filepath.");
            System.exit(0);
        }
    }

}
