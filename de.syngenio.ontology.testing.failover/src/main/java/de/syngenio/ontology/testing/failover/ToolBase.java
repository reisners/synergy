package de.syngenio.ontology.testing.failover;

import java.io.File;
import java.net.URL;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntDocumentManager.ReadFailureHandler;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

public abstract class ToolBase implements Runnable
{
    private final static Logger log = LoggerFactory.getLogger(ToolBase.class);
    private String ontologyFilename;
    protected OntModel ontologyModel;
    protected int index;

    public ToolBase(String[] args)
    {
        index = 0;
        ontologyFilename = args[index++];
    }

    public void run()
    {
        loadOntology();
    }

    private void loadOntology()
    {
        OntDocumentManager.getInstance().setReadFailureHandler(new ReadFailureHandler() {

            @Override
            public void handleFailedRead(String url, Model model, Exception e)
            {
                System.out.println("url="+url);
                e.printStackTrace(System.out);
                try {
                    File file = new File(new URL(url).toURI());
                    System.out.println(file.exists());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            
        });
        
        OntDocumentManager.getInstance().setFileManager(FileManager.makeGlobal()); // necessary to resolve the import file url 
        
        ontologyModel = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_MICRO_RULE_INF);
        ontologyModel.read(new File(ontologyFilename).toURI().toASCIIString());
        ontologyModel.loadImports();
    }

}
