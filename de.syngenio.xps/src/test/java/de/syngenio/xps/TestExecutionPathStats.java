/***********************************************************************
 * (C)opyright 2014 by syngenio AG München, Germany
 * [All rights reserved]. This product and related documentation are
 * protected by copyright restricting its use, copying, distribution,
 * and decompilation. No part of this product or related documentation
 * may be reproduced in any form by any means without prior written
 * authorization of syngenio or its partners, if any. Unless otherwise
 * arranged, third parties may not have access to this product or
 * related documentation.
 **********************************************************************/

/***********************************************************************
 *    $Author$
 *   $RCSfile$
 *  $Revision$
 *        $Id$
 **********************************************************************/

package de.syngenio.xps;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.neo4j.visualization.graphviz.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph;

import de.syngenio.xps.GraphAnalyzer.CountingTree;
import de.syngenio.xps.GraphAnalyzer.Result;
import de.syngenio.xps.XPS.Checkpoint;

public class TestExecutionPathStats
{
    private final static Logger log = LoggerFactory.getLogger(TestExecutionPathStats.class);

    @Rule
    public TestWatcher watchman = new TestWatcher() {
        public void starting(Description description)
        {
            log.info("Run Test {}...", description);
        }

        public void succeeded(Description description)
        {
            log.info("Test {} succeeded.", description);
        }

        public void failed(Throwable e, Description description)
        {
            log.error("Test {} failed with {}.", description, e);
        }
    };

    private final Random random = new Random();

    enum ExecutionPoint {
        A {
            @Override
            public ExecutionPoint next()
            {
                return B;
            }
        },
        B {
            @Override
            public ExecutionPoint next()
            {
                return C;
            }
        },
        C {
            @Override
            public ExecutionPoint next()
            {
                return D;
            }
        },
        D {
            @Override
            public ExecutionPoint next()
            {
                return E;
            }
        },
        E {
            @Override
            public ExecutionPoint next()
            {
                return F;
            }
        },
        F {
            @Override
            public ExecutionPoint next()
            {
                return G;
            }
        },
        G {
            @Override
            public ExecutionPoint next()
            {
                return H;
            }
        },
        H {
            @Override
            public ExecutionPoint next()
            {
                return finish;
            }
        },
        finish {
            @Override
            public ExecutionPoint next()
            {
                return null;
            }
        },
        error {
            @Override
            public ExecutionPoint next()
            {
                return null;
            }
        };

        public abstract ExecutionPoint next();
    };

    @Test
    public void testChronicleLogger() throws IOException, JSONException, ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        ChronicleRecordLogger recordLogger = new ChronicleRecordLogger();
        XPS.configure(recordLogger);
        XPS.point("input");
        XPS.point("output");
        
//        recordLogger.close();
        
        // transfer all records from Chronicle into Neo4J
        Neo4j2Graph graph = GraphUtil.load(recordLogger, "graphs/graphChronicleLogger");
        final Result result = GraphAnalyzer.analyze(graph);
        for (CountingTree<String> countingTree : result.values()) {
            countingTree.dump(0);
        }
        graph.shutdown();
    }
    
    @Test
    public void testSplitJoin() throws IOException, JSONException, ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        ChronicleRecordLogger recordLogger = new ChronicleRecordLogger();
        XPS.configure(recordLogger);

        // "input" marks the opening of an input container with six items
        Checkpoint input = XPS.point("input"); // keep a reference to this point

        // item #1
        XPS.split(input); // explicitly connect to point "start" (must work even
                          // across threads)
        XPS.point("A");
        XPS.point("B");
        XPS.point("C");
        XPS.done(input);
        // item is now fully processed and has been put into some output
        // container

        // item #2
        XPS.split(input); // explicitly connect to point "start"
        XPS.point("A");
        XPS.point("B"); // implicitly connects to the previous point
        XPS.point("C");
        XPS.done(input);
        // item is now fully processed and has been put into some output
        // container

        // item #3
        XPS.split(input); // make the connection before anything can go wrong
        XPS.point("B");

        // here a sub-iteration starts
        Checkpoint d = XPS.point("D");
        XPS.split(d);
        XPS.point("E").set("varA", 1);
        XPS.done(d);
        XPS.split(d);
        XPS.point("E").set("varA", 2);
        XPS.done(d);
        // sub-iteration done
        XPS.join(d);
        // this is the common successor of all sub-iteration branches
        // (even if the join on d never takes place (e.g. because an exception
        // occurred),
        // the structure must stay intact
        XPS.point("C");
        // branch ends abruptly
        // XPS.done(input);
        // item is now fully processed and has been put into some output
        // container

        // item #4
        XPS.split(input);
        XPS.point("A");
        XPS.point("error");
        // item is discarded because of error

        // item #5
        XPS.split(input);
        XPS.point("A");
        XPS.point("B");
        XPS.point("C");
        XPS.done(input);

        // item #6
        XPS.split(input); // make the connection before anything can go wrong
        XPS.point("error");
        // item might not even have been obtained due to error

        // the output container that has been filled with several items is now
        // being closed and output
        XPS.join(input); // all branches originating from input are now
                         // terminated
        XPS.point("output");

        // transfer all records from Chronicle into Neo4J
        Neo4j2Graph graph = GraphUtil.load(recordLogger, "graphs/graphSplitJoin");
        Result analysisResult = GraphAnalyzer.analyze(graph);
        graph.shutdown();
        
        analysisResult.toCSV(new File("testSplitJoin.csv"));

        Script script = Script.initialize(Script.class, "graphs/graphSplitJoin");
        script.emit(new File("graph.dot"));
    }

    @Test
    public void test() throws IOException, JSONException, ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        ChronicleRecordLogger recordLogger = new ChronicleRecordLogger();
        XPS.configure(recordLogger);

        Checkpoint start = XPS.point("start");

        int n = 1000;
        for (int i = 0; i < n; ++i)
        {
            XPS.split(start);

            ExecutionPoint xp = null;
            path: while (true)
            {
                xp = nextExecutionPoint(xp);
                XPS.point(xp.name());
                switch (xp)
                {
                case finish:
                    XPS.done(start);
                case error:
                    break path;
                default:
                    continue;
                }
            }
        }
        XPS.join(start);
        XPS.point("end");

        // transfer all records from Chronicle into Neo4J
        Neo4j2Graph graph = GraphUtil.load(recordLogger, "graphs/graphTest");

        final Result result = GraphAnalyzer.analyze(graph);
        for (CountingTree<String> countingTree : result.values()) {
            countingTree.dump(0);
        }
        result.toCSV(new File("test.csv"));

        graph.shutdown();

    }

    private ExecutionPoint nextExecutionPoint(ExecutionPoint previous)
    {
        ExecutionPoint xp = previous != null ? previous.next() : ExecutionPoint.A;
        while (xp.compareTo(ExecutionPoint.finish) < 0 && xp.compareTo(ExecutionPoint.error) < 0)
        {
            double r = random.nextDouble();

            if (r < 0.95)
            {
                return xp;
            }
            else if (r > 0.999)
            {
                return ExecutionPoint.error;
            }
            xp = xp.next();
        }
        return xp;
    }

}
