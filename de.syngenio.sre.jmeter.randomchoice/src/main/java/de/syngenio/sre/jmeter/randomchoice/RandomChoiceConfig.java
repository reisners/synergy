/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package de.syngenio.sre.jmeter.randomchoice;

import java.io.IOException;
import java.util.Random;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.engine.util.NoConfigMerge;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class RandomChoiceConfig extends ConfigTestElement
    implements TestBean, LoopIterationListener, NoThreadClone, NoConfigMerge
{
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long serialVersionUID = 233L;

    /*
     *  N.B. this class is shared between threads (NoThreadClone) so all access to variables
     *  needs to be protected by a lock (either sync. or volatile) to ensure safe publication.
     */

    private String fileName;

    private String separatorRegexp;
    
    private boolean firstColumnContainsWeights;

    private String randomSeed;

    private boolean perThread;

    // This class is not cloned per thread, so this is shared
    private Random globalRandom = null;

    // Used for per-thread/user numbers
    // Cannot be static, as random numbers are not to be shared between instances
    private transient ThreadLocal<Random> perThreadRandom = initThreadLocal();

    private ThreadLocal<Random> initThreadLocal() {
        return new ThreadLocal<Random>() {
                @Override
                protected Random initialValue() {
                    init();
                    return new Random(getRandomSeedAsLong());
                }};
    }

    private RandomChoiceData<String> data;

    private Object readResolve(){
        perThreadRandom = initThreadLocal();
        return this;
    }

    /*
     * nextInt(n) returns values in the range [0,n),
     * so n must be set to max-min+1
     */
    private void init(){
        try {
            data = RandomChoiceData.read(fileName, separatorRegexp, firstColumnContainsWeights);
        } catch (IOException e) {
            log.error(fileName, e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void iterationStart(LoopIterationEvent iterEvent) {
        Random randGen=null;
        if (getPerThread()){
            randGen = perThreadRandom.get();
        } else {
            synchronized(this){
                if (globalRandom == null){
                    init();
                    globalRandom = new Random(getRandomSeedAsLong());
                }
                randGen=globalRandom;
            }
        }
        JMeterVariables variables = JMeterContextService.getContext().getVariables();
        String[] randomChoice = data.randomChoice(randGen);
        for (int i = 0; i < data.getColumnNames().length; ++i) {
            variables.put(data.getColumnNames()[i], randomChoice[i]);
        }
    }

    /**
     * @return the variableName
     */
    public synchronized String getFileName() {
        return fileName;
    }

    /**
     * @param variableName the variableName to set
     */
    public synchronized void setFileName(String variableName) {
        this.fileName = variableName;
    }

    public String getSeparatorRegexp()
    {
        return separatorRegexp;
    }

    public void setSeparatorRegexp(String separatorRegexp)
    {
        this.separatorRegexp = separatorRegexp;
    }

    /**
     * @return the randomSeed
     */
    public synchronized String getRandomSeed() {
        return randomSeed;
    }

    /**
     * @return the randomSeed as a long
     */
    private synchronized long getRandomSeedAsLong() {
        long seed = 0;
        if (randomSeed.length()==0){
            seed = System.currentTimeMillis();
        }  else {
            try {
                seed = Long.parseLong(randomSeed);
            } catch (NumberFormatException e) {
                seed = System.currentTimeMillis();
                log.warn("Cannot parse seed "+e.getLocalizedMessage());
            }
        }
        return seed;
    }

    /**
     * @param randomSeed the randomSeed to set
     */
    public synchronized void setRandomSeed(String randomSeed) {
        this.randomSeed = randomSeed;
    }

    /**
     * @return the perThread
     */
    public synchronized boolean getPerThread() {
        return perThread;
    }

    /**
     * @param perThread the perThread to set
     */
    public synchronized void setPerThread(boolean perThread) {
        this.perThread = perThread;
    }

    public boolean getFirstColumnContainsWeights()
    {
        return firstColumnContainsWeights;
    }

    public void setFirstColumnContainsWeights(boolean firstColumnContainsWeights)
    {
        this.firstColumnContainsWeights = firstColumnContainsWeights;
    }
}
