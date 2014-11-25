package de.syngenio.sre.jmeter.randomchoice;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;

public class RandomChoiceData<T>
{
    private String[] columnNames;
    private T[][] Choices;
    private double[] weights;
    private double[] normalizedWeights;
    private boolean initialized = false;;
    
    String[] getColumnNames()
    {
        return columnNames;
    }
    void setColumnNames(String[] columnNames)
    {
        this.columnNames = columnNames;
    }
    Object[][] getChoices()
    {
        return Choices;
    }
    void setChoices(T[][] choices)
    {
        this.Choices = choices;
    }
    double[] getWeights()
    {
        return weights;
    }
    void setWeights(double[] weights)
    {
        this.weights = weights;
    }
    
    T[] randomChoice(Random random) {
        return Choices[randomChoiceIndex(random)];
    }
    
    private int randomChoiceIndex(Random random)
    {
        initializeLazily();
        double r = random.nextDouble();
        for (int i = 0; i < normalizedWeights.length; ++i) {
            if (r < normalizedWeights[i]) {
                return i;
            }
            r -= normalizedWeights[i];
        }
        throw new Error("should not occur");
    }
    
    private void initializeLazily()
    {
        if (initialized ) {
            return;
        }
        if (weights.length != Choices.length) {
            throw new IllegalStateException(weights.length+" weights != "+Choices.length+" choices");
        }
        normalizeWeights();
    }
    
    private void normalizeWeights()
    {
        double total = 0d;
        for (double weight : weights) {
            total += weight;
        }
        if (total <= 0d) {
            throw new IllegalStateException("total weight "+total+" <= 0");
        }
        normalizedWeights = new double[weights.length];
        for (int i = 0; i < weights.length; ++i) {
            normalizedWeights[i] = weights[i]/total;
        }
    }
    
    /**
     * Create a new instance of RandomChoiceData and populates it from a file.
     * The first line in the file must contains column names separated by sep (a regular expression for {@link java.lang.String#split})  
     * @param fileName
     * @param sep
     * @param firstColumnContainsWeights 
     * @return
     * @throws IOException 
     */
    public static RandomChoiceData<String> read(String fileName, String sep, boolean firstColumnContainsWeights) throws IOException
    {
        String[][] rows = readFully(fileName, sep);
        return firstColumnContainsWeights ? createNonuniformRandomChoiceData(rows) : createUniformRandomChoiceData(rows);
    }

    private static RandomChoiceData<String> createNonuniformRandomChoiceData(String[][] rows)
    {
        RandomChoiceData<String> data = new RandomChoiceData<String>();
        data.setWeights(new double[rows.length-1]);
        data.setColumnNames(ArrayUtils.subarray(rows[0], 1, rows[0].length));
        List<String[]> choices = new ArrayList<String[]>();
        for (int iRow = 1; iRow < rows.length; ++iRow) {
            choices.add(ArrayUtils.subarray(rows[iRow], 1, rows[iRow].length));
            data.getWeights()[iRow-1] = Double.parseDouble(rows[iRow][0]);
        }
        data.setChoices(choices.toArray(new String[0][]));
        return data;
    }
    
    private static RandomChoiceData<String> createUniformRandomChoiceData(String[][] rows)
    {
        RandomChoiceData<String> data = new RandomChoiceData<String>();
        data.setColumnNames(rows[0]);
        data.setChoices(ArrayUtils.subarray(rows, 1, rows.length));
        data.setWeights(new double[rows.length-1]);
        Arrays.fill(data.getWeights(), 1d);
        return data;
    }
    
    private static String[][] readFully(String fileName, String sep) throws IOException
    {
        List<String[]> rows = new ArrayList<String[]>();
        try (BufferedReader r = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] fields = line.split(sep);
                rows.add(fields);
            }
            return rows.toArray(new String[0][]);
        }
    }
}
