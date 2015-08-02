package de.syngenio.ontology.testing.failover;

import java.io.File;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;

public class SparqlTool extends ToolBase
{
    private String sparqlScriptFilename;

    public SparqlTool(String[] args)
    {
        super(args);

        if (index < args.length)
        {
            sparqlScriptFilename = args[index++];
        }
    }

    public void run()
    {
        super.run();

        Query query = QueryFactory.read(new File(sparqlScriptFilename).toURI().toASCIIString());
        // Execute the query and obtain results
        QueryExecution qe = QueryExecutionFactory.create(query, ontologyModel);
        com.hp.hpl.jena.query.ResultSet results = qe.execSelect();

        // Output query results
        ResultSetFormatter.out(System.out, results, query);
        System.out.println(""+results.getRowNumber()+" rows");

        qe.close();
    }

    public static void main(String[] args)
    {
        try
        {
            new SparqlTool(args).run();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }
}
