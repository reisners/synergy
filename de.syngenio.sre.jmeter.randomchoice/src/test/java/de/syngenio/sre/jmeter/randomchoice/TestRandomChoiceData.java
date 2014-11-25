package de.syngenio.sre.jmeter.randomchoice;

import java.util.Arrays;
import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

public class TestRandomChoiceData
{

    @Test
    public void test()
    {
        RandomChoiceData<String> rcd = new RandomChoiceData<String>();
        rcd.setColumnNames(new String[] {"Size"});
        rcd.setChoices(new String[][] {new String[] {"1KB"}, new String[] {"10KB"}, new String[] {"100KB"}, new String[] {"1MB"}, new String[] {"5MB"}, new String[] {"50MB"}, new String[] {"100MB"}, new String[] {"1GB"}});
        rcd.setWeights(new double[] {1000000, 100000, 10000, 1000, 200, 20, 10, 1});
        int[] sample = sample(rcd, new Random(), 1000000);
        System.out.println("sample="+Arrays.asList(ArrayUtils.toObject(sample)).toString());
    }

    private int[] sample(RandomChoiceData<String> rcd, Random random, int n)
    {
        int[] counts = new int[rcd.getChoices().length];
        
        for (int i = 0; i < n; ++i) {
            String[] randomChoice = rcd.randomChoice(random);
            for (int j = 0; j < rcd.getChoices().length; ++j) {
                if (randomChoice[0].equals(rcd.getChoices()[j][0])) {
                    ++counts[j];
                }
            }
        }
        return counts;
    }

}
