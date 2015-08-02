package de.syngenio.ontology.testing.failover;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntDocumentManager.ReadFailureHandler;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class FailoverTestcaseGeneratorOld implements Runnable
{
    private static final String NS_FAILOVER = "http://www.syngenio.de/ontology/testing/failover#";
    private static final String NS_TESTRESULTS = "http://www.syngenio.de/ontology/testing/failover/testresults#";
    private static final String NS_GMAC = "http://www.opelbank.de/ontology/systemmodel#";
    private static final String NS_FISK = "http://www.kordoba.de/ontology/components#";
    private final static Logger log = LoggerFactory.getLogger(FailoverTestcaseGeneratorOld.class);
    private String ontologyFilename;
    private OntModel ontologyModel;
    private OntModel testcaseModel;
    private XSSFWorkbook workbook;
    private XSSFCellStyle wrapStyle;
    private XSSFCellStyle servicesCol0Style;
    private XSSFCellStyle servicesStyle;
    private XSSFCellStyle resourcesStyle;
    private XSSFCellStyle resourcesCol0Style;
    private XSSFCellStyle disruptHeaderStyle;
    private XSSFCellStyle restoreHeaderStyle;
    
    private static final Map<String, String> prefixMap = new HashMap<String, String>() {{
       put("failover:", NS_FAILOVER);
       put("gmac:", NS_GMAC);
       put("fisk:", NS_FISK);
       put("testresults:", NS_TESTRESULTS);
    }};

    public FailoverTestcaseGeneratorOld(String[] args)
    {
        int index = 0;
        ontologyFilename = args[index++];
    }

    public static void main(String[] args)
    {
        try
        {
            new FailoverTestcaseGeneratorOld(args).run();
        }
        catch (Throwable e)
        {
            log.error("args="+Arrays.toString(args), e);
        }
    }

    public void run()
    {
        try {
            doRun();
        } catch (Exception e) {
            throw new Error(e);
        }
    }
    
    private void doRun() throws FileNotFoundException, IOException {
        loadOntology();

        // create a copy of the original model that will be augmented in subsequent steps
        testcaseModel = ModelFactory.createOntologyModel(ontologyModel.getSpecification() /*, ontologyModel*/);
        Ontology ontology = testcaseModel.createOntology("http://www.syngenio.de/ontology/testing/failover/testcases");
        ontology.addImport(testcaseModel.createResource("http://www.syngenio.de/ontology/testing/failover"));
        testcaseModel.loadImports();
        for (Entry<String, String> entry : prefixMap.entrySet()) {
            testcaseModel.setNsPrefix(entry.getKey().replaceFirst(":$", ""), entry.getValue());
        }

        generateTestCases();
        
        inferUnavailabilities();
        
        saveTestcaseModel();
    }

    private void saveTestcaseModel() throws FileNotFoundException, IOException
    {
        final String testcaseModelFilename = ontologyFilename.replaceFirst("\\.[^.]+$", "")+"_testcases.rdf";
        try (OutputStream out = new FileOutputStream(testcaseModelFilename)) {
            testcaseModel.write(out, "RDF/XML-ABBREV");
            log.info("saved testcase model to "+testcaseModelFilename);
        }
    }

    public boolean isAvailableInStep(RDFNode resourceOrService, RDFNode step)
    {
        List<RDFNode> unavailables = query("select ?cfg { %s failover:hasConfiguration ?cfg . ?cfg failover:isUnavailable %s }", "cfg", step.asResource(), resourceOrService.asResource());
        final boolean available = unavailables.isEmpty();
        return available;
    }

    private List<List<RDFNode>> query(String queryTemplate, Resource... resources)
    {
        List<List<RDFNode>> results = new ArrayList<List<RDFNode>>();
        String[] uris = new String[resources.length];
        for (int i = 0; i < resources.length; ++i) {
            uris[i] = withPrefixes(resources[i].getURI());
        }
        String query = String.format(queryTemplate, uris);
        try {
            QueryExecution qe = QueryExecutionFactory.create(generateQuery(query), testcaseModel);
            ResultSet resultSet = qe.execSelect();
            while (resultSet.hasNext()) {
                QuerySolution querySolution = resultSet.next();
                List<RDFNode> fields = new ArrayList<RDFNode>();
                for (Iterator<String> i = querySolution.varNames(); i.hasNext();) {
                    String varName = i.next();
                    fields.add(querySolution.get(varName));
                }
                results.add(fields);
            }
            log.debug(query+" -> "+results);
            return results;
        } catch (Throwable e) {
            throw new IllegalArgumentException(query, e);
        }
    }

    private List<RDFNode> query(String queryTemplate, String field, Resource... resources)
    {
        List<RDFNode> results = new ArrayList<RDFNode>();
        String[] uris = new String[resources.length];
        for (int i = 0; i < resources.length; ++i) {
            uris[i] = withPrefixes(resources[i].getURI());
        }
        String query = String.format(queryTemplate, uris);
        try {
            QueryExecution qe = QueryExecutionFactory.create(generateQuery(query), testcaseModel);
            ResultSet resultSet = qe.execSelect();
            while (resultSet.hasNext()) {
                QuerySolution querySolution = resultSet.next();
                results.add(querySolution.get(field));
            }
            log.debug(query+" -> "+results);
            return results;
        } catch (Throwable e) {
            throw new IllegalArgumentException(query, e);
        }
    }

    private String withPrefixes(String uri)
    {
        for (Entry<String, String> entry : prefixMap.entrySet()) {
            uri = uri.replaceAll(entry.getValue(), entry.getKey());
        }
        return uri;
    }

    private void inferUnavailabilities()
    {
        // iteratively add isUnavailable assertions
        while (true) {
            int count = 0;
            
            // infer unavailability by dependsOn
            String queryDependsOn = "construct {?cfg failover:isUnavailable ?unavailable} where { ?unavailable failover:dependsOn* ?dependency . ?cfg (failover:isInactive|failover:isUnavailable) ?dependency FILTER NOT EXISTS { ?cfg failover:isUnavailable ?unavailable } }";
            Model dependsOnModel = QueryExecutionFactory.create(generateQuery(queryDependsOn), testcaseModel).execConstruct();
            testcaseModel.add(dependsOnModel.listStatements());
            count += count(dependsOnModel.listStatements());

            // infer unavailability by connectedTo
            String queryConnectedTo = "construct {?cfg failover:isUnavailable ?resource} "+
"WHERE { ?resource   ?connection ?instance . ?connection rdfs:subPropertyOf failover:connectedTo . ?cfg failover:isUnavailable ?instance . "+
  "FILTER NOT EXISTS { ?resource ?connection ?instance2 . FILTER NOT EXISTS { ?cfg (failover:isInactive|failover:isUnavailable) ?instance2 } } FILTER NOT EXISTS { ?cfg failover:isUnavailable ?resource } }";
            Model connectedToModel = QueryExecutionFactory.create(generateQuery(queryConnectedTo), testcaseModel).execConstruct();
            testcaseModel.add(connectedToModel.listStatements());
            count += count(connectedToModel.listStatements());
            for (StmtIterator i = connectedToModel.listStatements(); i.hasNext();) {
                log.debug(i.next().toString());
            }
            log.debug("added "+count);
            if (count == 0) {
                break;
            }
        }
        
        // check: there should be no available resources depending on unavailable resources
        String query = "select ?cfg ?available where { ?cfg (failover:isInactive|failover:isUnavailable) ?dependency . ?available failover:dependsOn* ?dependency FILTER NOT EXISTS { ?cfg failover:isUnavailable ?available } }";
        QueryExecution qe = QueryExecutionFactory.create(generateQuery(query), testcaseModel);
        ResultSet results = qe.execSelect();
        // Output query results
        ResultSetFormatter.out(results);
        System.out.println(""+results.getRowNumber()+" rows");
        if (results.getRowNumber() > 0) {
            throw new Error("assertion failed");
        }

    }

    private int count(Iterator i)
    {
        int count = 0;
        while (i.hasNext()) {
            ++count;
            i.next();
        }
        return count;
    }

    private String generateQuery(String rawQuery)
    {
        return "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
"PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"+
"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"+
generatePrefixeLines()+
rawQuery;
    }

    private String generatePrefixeLines()
    {
        StringBuffer sb = new StringBuffer();
        for (Entry<String, String> entry : prefixMap.entrySet()) {
            sb.append(String.format("PREFIX %s <%s>\n", entry.getKey(), entry.getValue()));
        }
        return sb.toString();
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
        
        ontologyModel = ModelFactory.createOntologyModel();
        ontologyModel.read(new File(ontologyFilename).toURI().toASCIIString());
        ontologyModel.loadImports();
    }

    private String toString(RDFNode r) {
        String value = null;
        if (r.isLiteral()) {
            return r.asLiteral().getString();
        } else if (r.isResource()) {
            Individual individual = r.as(Individual.class);
            if (individual != null) {
                value = individual.getLabel(null);
            }
            if (value == null) {
                value = ((Resource)r).getLocalName(); 
            }
        }
        return value != null ? value : "[]";
    }

    private void generateTestCases()
    {
        // construct a test case for each concrete class of resources 
        List<RDFNode> resourceClasses = query("SELECT ?c WHERE {?c rdfs:subClassOf+ failover:Resource filter not exists { ?sc rdfs:subClassOf ?c filter ( ?sc != ?c ) } }", "c");
        
        for (RDFNode nodeResourceClass : resourceClasses) {
            Resource resourceClass = nodeResourceClass.asResource();
            log.info("creating test case for "+resourceClass.getLocalName()+" failover");
            
            Resource testcase = createTestcase(NS_GMAC+"Failover_"+resourceClass.getLocalName());
            
            // find all startable instances of the resource class
            List<String> instanceNames = new ArrayList<String>();
            
            List<RDFNode> startableInstances = query("select distinct ?s where { ?s a %s . ?s a failover:Startable }", "s", resourceClass);
            for (RDFNode nodeStartableInstance : startableInstances) {
                Resource startableInstance = nodeStartableInstance.asResource();
                instanceNames.add(startableInstance.getLocalName());
                
                OntClass configuration = testcaseModel.createClass(NS_FAILOVER+"Configuration");
                Resource config_disruption = testcaseModel.createIndividual(configuration);
                
                Property isInactive = testcaseModel.createProperty(NS_FAILOVER+"isInactive");
                testcaseModel.add(config_disruption, isInactive, startableInstance);
                
                OntClass stepClass = testcaseModel.createClass(NS_FAILOVER+"TestcaseStep");
                Individual step_disrupt = testcaseModel.createIndividual(NS_GMAC+startableInstance.getLocalName()+"_disrupt", stepClass);
                
                List<RDFNode> disrupts = query("select (group_concat(?disrupt;separator=';') as ?disrupts) where { %s failover:disrupt ?disrupt } ", "disrupts", resourceClass);
                if (!disrupts.isEmpty()) {
                    String disruptString = disrupts.get(0).asLiteral().getString();
                    step_disrupt.setComment(disruptString, null);
                }
                
                Property hasConfig = testcaseModel.createProperty(NS_FAILOVER+"hasConfiguration");
                testcaseModel.add(step_disrupt, hasConfig, config_disruption);
                Property hasStep = testcaseModel.createProperty(NS_FAILOVER+"hasStep");
                testcaseModel.add(testcase, hasStep, step_disrupt);
                
                log.info("created step "+step_disrupt.getLocalName());
                
                Individual step_restore = testcaseModel.createIndividual(NS_GMAC+startableInstance.getLocalName()+"_restore", stepClass);
                
                List<RDFNode> restores = query("select (group_concat(?restore;separator=';') as ?restores) where { %s failover:restore ?restore } ", "restores", resourceClass);
                if (!restores.isEmpty()) {
                    String restoreString = restores.get(0).asLiteral().getString();
                    step_restore.setComment(restoreString, null);
                }
                testcaseModel.add(testcase, hasStep, step_restore);
                
                log.info("created step "+step_restore.getLocalName());

            }
            
            testcase.as(Individual.class).addComment("Test that failover between "+resourceClass.getLocalName()+" instances "+instanceNames.toString().replaceAll("[\\[\\]]", "")+" works", null);
        }
    }

    private Resource createTestcase(String uri)
    {
        OntClass testcase = testcaseModel.createClass(NS_FAILOVER+"Testcase");
        return testcaseModel.createIndividual(uri, testcase);
    }

    private void debugQuery(String queryTemplate, Resource... resources)
    {
        List<RDFNode> results = new ArrayList<RDFNode>();
        String[] uris = new String[resources.length];
        for (int i = 0; i < resources.length; ++i) {
            uris[i] = withPrefixes(resources[i].getURI());
        }
        String query = String.format(queryTemplate, uris);
        try {
            QueryExecution qe = QueryExecutionFactory.create(generateQuery(query), testcaseModel);
            ResultSet resultSet = qe.execSelect();
            ResultSetFormatter.out(System.out, resultSet);
            System.out.println(""+resultSet.getRowNumber()+" rows");
        } catch (Throwable e) {
            throw new IllegalArgumentException(query, e);
        }
    }
}
