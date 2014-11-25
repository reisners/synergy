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

import java.beans.PropertyDescriptor;

import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.FileEditor;

public class RandomChoiceConfigBeanInfo extends BeanInfoSupport {

    // These group names must have .displayName properties
    private static final String DATA_GROUP = "data"; // $NON-NLS-1$
    private static final String OPTIONS_GROUP = "options"; // $NON-NLS-1$
    private static final String RANDOM_GROUP = "random"; // $NON-NLS-1$

    // These variable names must have .displayName properties and agree with the getXXX()/setXXX() methods
    private static final String PER_THREAD = "perThread"; // $NON-NLS-1$
    private static final String RANDOM_SEED = "randomSeed"; // $NON-NLS-1$
    private static final String SEPARATOR_REGEXP = "separatorRegexp"; // $NON-NLS-1$
    private static final String FILE_NAME = "fileName"; // $NON-NLS-1$
    private static final String FIRST_COLUMN_CONTAINS_WEIGHTS = "firstColumnContainsWeights"; // $NON-NLS-1$

    public    RandomChoiceConfigBeanInfo() {
        super(RandomChoiceConfig.class);

        PropertyDescriptor p;

        createPropertyGroup(DATA_GROUP, new String[] { FILE_NAME, SEPARATOR_REGEXP, FIRST_COLUMN_CONTAINS_WEIGHTS, });

        p = property(FILE_NAME);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, ""); // $NON-NLS-1$
        p.setPropertyEditorClass(FileEditor.class);

        p = property(SEPARATOR_REGEXP);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "\\t"); // $NON-NLS-1$

        p = property(FIRST_COLUMN_CONTAINS_WEIGHTS);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(NOT_EXPRESSION, Boolean.TRUE);
        p.setValue(NOT_OTHER, Boolean.TRUE);
        p.setValue(DEFAULT, Boolean.FALSE);

        createPropertyGroup(RANDOM_GROUP,
        new String[] { RANDOM_SEED, });

        p = property(RANDOM_SEED);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, ""); // $NON-NLS-1$

        createPropertyGroup(OPTIONS_GROUP, new String[] { PER_THREAD, });

        p = property(PER_THREAD);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(NOT_EXPRESSION, Boolean.TRUE);
        p.setValue(NOT_OTHER, Boolean.TRUE);
        p.setValue(DEFAULT, Boolean.FALSE);
    }
}
