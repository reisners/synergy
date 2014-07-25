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
import java.io.IOException;
import java.util.Random;

import org.junit.Test;
import org.neo4j.visualization.graphviz.Script;

import de.syngenio.xps.XPS.Checkpoint;

public class TestExecutionPathStats
{
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
    public void testSplitJoin() throws IOException {
        RecordLogger recordLogger = new RecordLogger();
        XPS.configure(recordLogger);

        // "input" marks the opening of an input container with six items 
        Checkpoint input = XPS.point("input"); // keep a reference to this point
        
        // item #1
        XPS.split(input); // explicitly connect to point "start" (must work even across threads)
        XPS.point("A");
        XPS.point("B");
        XPS.point("C");
        XPS.done(input);
        // item is now fully processed and has been put into some output container
        
        // item #2
        XPS.split(input); // explicitly connect to point "start"
        XPS.point("A");
        XPS.point("B"); // implicitly connects to the previous point
        XPS.point("C");
        XPS.done(input);
        // item is now fully processed and has been put into some output container
        
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
        // (even if the join on d never takes place (e.g. because an exception occurred), 
        // the structure must stay intact
        XPS.point("C");
        // branch ends abruptly
//        XPS.done(input);
        // item is now fully processed and has been put into some output container
        
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
        
        // the output container that has been filled with several items is now being closed and output
        XPS.join(input); // all branches originating from startRef are now terminated 
        XPS.point("output");

        String analysisPath = "analysis_graph";
        try (GraphAnalyzer ga = new GraphAnalyzer(recordLogger, analysisPath)) {
            ga.analyze();
        }
        Script script = Script.initialize(Script.class, analysisPath);
        script.emit(new File("graph.dot"));
    }
    
    @Test
    public void test() throws IOException
    {
        RecordLogger recordLogger = new RecordLogger();
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

        try (GraphAnalyzer analyzer = new GraphAnalyzer(recordLogger, "analysis_graph")) {
            analyzer.analyze();
        }
    }

    private ExecutionPoint nextExecutionPoint(ExecutionPoint previous)
    {
        ExecutionPoint xp = previous != null ? previous.next() : ExecutionPoint.A;
        while (xp.compareTo(ExecutionPoint.finish) < 0 && xp.compareTo(ExecutionPoint.error) < 0)
        {
            double r = random.nextDouble();

            if (r < 0.95) {
                return xp;
            } else if (r > 0.999) {
                return ExecutionPoint.error;
            }
            xp = xp.next();
        }
        return xp;
    }

}
