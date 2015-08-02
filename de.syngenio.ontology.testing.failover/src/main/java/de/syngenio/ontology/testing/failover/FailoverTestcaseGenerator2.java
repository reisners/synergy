package de.syngenio.ontology.testing.failover;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFCreationHelper;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumn;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumns;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntDocumentManager.ReadFailureHandler;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
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

import de.syngenio.ontology.testing.failover.graph.DiGraph;
import de.syngenio.ontology.testing.failover.graph.Graph;
import de.syngenio.ontology.testing.failover.graph.Graph.Edge;
import de.syngenio.ontology.testing.failover.graph.Graph.Node;

public class FailoverTestcaseGenerator2 implements Runnable
{
    private static final String NS_FAILOVER = "http://www.syngenio.de/ontology/testing/failover#";
    private static final String NS_TESTRESULTS = "http://www.syngenio.de/ontology/testing/failover/testresults#";
    static final String NS_GMAC = "http://www.opelbank.de/ontology/systemmodel#";
    private static final String NS_FISK = "http://www.kordoba.de/ontology/components#";
    private final static Logger log = LoggerFactory.getLogger(FailoverTestcaseGenerator2.class);
    private String ontologyFilename;
    OntModel ontologyModel;
    OntModel testcaseModel;
    private XSSFWorkbook workbook;
    private XSSFCellStyle wrapStyle;
    private XSSFCellStyle servicesCol0Style;
    private XSSFCellStyle servicesStyle;
    private XSSFCellStyle resourcesStyle;
    private XSSFCellStyle resourcesCol0Style;
    private XSSFCellStyle disruptStyle;
    private XSSFCellStyle restoreStyle;
    private OntClass stepClass;
    private Property hasIndex;
    private Property hasStep;
    private Property testcaseOrService;
    private Property propertyPreviousStep;
    private OntClass configuration;
    private Property hasConfig;
    private Property isInactive;
    private OntClass testcaseClass;
    private XSSFCreationHelper creationHelper;

    private int maxDistanceBetween(Individual resourceA, Individual resourceB) {
        List<RDFNode> maxDistances = query("select (max(?distance) as ?maxDistance) where { ?resourceA a owl:Thing FILTER ( ?resourceA = %s ) . ?resourceB a owl:Thing FILTER ( ?resourceB = %s ) . { select ?resourceA ?resourceB (count(?mid) as ?distance) { ?resourceA (failover:connectedTo|failover:dependsOn)* ?mid . ?mid (failover:connectedTo|failover:dependsOn)* ?resourceB } group by ?resourceA ?resourceB } }", "maxDistance", resourceA, resourceB);
        final RDFNode maxDistanceNode = maxDistances.get(0);
        if (maxDistanceNode == null) {
            throw new IllegalArgumentException("maxDistance between "+resourceA+" and "+resourceB+" cannot be calculated");
        }
        return maxDistanceNode.asLiteral().getInt();
    }
    
    private static final Map<String, String> prefixMap = new HashMap<String, String>() {{
       put("failover:", NS_FAILOVER);
       put("gmac:", NS_GMAC);
       put("fisk:", NS_FISK);
       put("testresults:", NS_TESTRESULTS);
    }};

    public FailoverTestcaseGenerator2(String[] args)
    {
        int index = 0;
        ontologyFilename = args[index++];
    }

    public static void main(String[] args)
    {
        try
        {
            new FailoverTestcaseGenerator2(args).run();
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

        buildTestcaseModel();

        generateTestCases();
        
        inferUnavailabilities();
        
        saveTestcaseModel();
        
        generateWorkbook();
        
        generateArchitectureGraph();
    }

    Set<String> colors = new HashSet<String>(Arrays.asList("darkolivegreen1", "darkgoldenrod1"));
    Map<String, String> colorMap = new HashMap<String, String>();
    private XSSFCellStyle headerStyle;
    private XSSFCellStyle dateStyle;
    
    private String mapToColor(String value) {
        String color = colorMap.get(value);
        if (color == null) {
            color = colors.iterator().next();
            colors.remove(color);
            colorMap.put(value, color);
        }
        return color;
    }
    
    private void generateArchitectureGraph() throws IOException
    {
        
        DiGraph digraph = new DiGraph("architecture");
        digraph.set("ratio", new Integer(1));
        digraph.set("splines", Boolean.FALSE);
        digraph.set("label", "System Architecture");
        // add resource nodes along with hosts and buildings
        for (Map<String, RDFNode> res : query("select distinct ?res ?class ?host ?datacenter (bound(?startable) as ?isStartable) (bound(?external) as ?isExternal) where { ?res a ?class . ?class rdfs:subClassOf failover:Resource . filter not exists { ?res a failover:Host } . filter not exists { ?res a failover:Building } . optional { ?res a ?startable filter (?startable = failover:Startable) } . optional { ?res a ?external filter (?external = failover:External ) } . optional { ?res failover:dependsOn ?host . ?host a failover:Host . optional { ?host failover:dependsOn ?datacenter . ?dataceneter a failover:Building } } }")) {
            Individual datacenter = null;
            if (res.get("datacenter") != null) {
                datacenter = res.get("datacenter").as(Individual.class);
            }
            Graph datacenterGraph = digraph;
            Graph hostGraph = digraph;
//            if (res.get("datacenter") != null) {
//                Individual datacenter = res.get("datacenter").as(Individual.class);
//                datacenterGraph = digraph.getSubgraph("cluster_"+datacenter.getLocalName());
//                if (datacenterGraph == null) {
//                    datacenterGraph = digraph.addSubgraph("cluster_"+datacenter.getLocalName());
//                    datacenterGraph.set("fillcolor", "darkgoldenrod1");
//                    datacenterGraph.set("style", "filled");
//                    datacenterGraph.set("label", "Building:\\n"+getLabel(datacenter));
//                }
//            }
            if (res.get("host") != null) {
                Individual host = res.get("host").as(Individual.class);
                hostGraph = datacenterGraph.getSubgraph("cluster_"+host.getLocalName());
                if (hostGraph == null) {
                    hostGraph = datacenterGraph.addSubgraph("cluster_"+host.getLocalName());
                    if (datacenter != null) {
                        hostGraph.set("fillcolor", mapToColor(datacenter.getLocalName()));
                    } else {
                        hostGraph.set("fillcolor", "black");
                    }
                    hostGraph.set("style", "filled");
                    hostGraph.set("label", "Host:\\n"+getLabel(host));
                }
            }
            Individual resource = res.get("res").as(Individual.class);
            OntClass clazz = res.get("class").as(OntClass.class);
            boolean isStartable = res.get("isStartable").asLiteral().getBoolean();
            boolean isExternal = res.get("isExternal").asLiteral().getBoolean();
            Node gnRes = hostGraph.getNode(resource.getLocalName());
            if (gnRes == null) {
                gnRes = hostGraph.addNode(resource.getLocalName());
                gnRes.set("label", clazz.getLocalName()+":\\n"+getLabel(resource));
                if (datacenter != null) {
                    gnRes.set("group", datacenter.getLocalName());
                }
                if (isStartable && !isExternal) {
                    gnRes.set("style", "filled");
                }
            }
        }
        // add nodes for services
        for (RDFNode svc : query("select distinct ?svc where { ?svc a failover:Service }", "svc")) {
            Individual service = svc.as(Individual.class);
            Node gnSvc = digraph.getNode(service.getLocalName());
            if (gnSvc == null) {
                gnSvc = digraph.addNode(service.getLocalName());
                gnSvc.set("label", "Service:\\n"+getLabel(service));
                gnSvc.set("shape", "diamond");
            }
        }
        // add edges
        for (Map<String, RDFNode> p : query("select distinct ?subject ?property ?object where { ?subject ?property ?object . filter not exists { ?subject a failover:Host } . filter not exists { ?object a failover:Host } . filter not exists { ?object a failover:Building } . { { ?property rdfs:subPropertyOf* failover:dependsOn } union { ?property rdfs:subPropertyOf* failover:connectedTo . filter not exists { ?subprop rdfs:subPropertyOf ?property filter ( ?subprop != ?property ) } } } }")) {
            Individual from = p.get("subject").as(Individual.class);
            Individual to = p.get("object").as(Individual.class);
            Property property = p.get("property").as(Property.class);
            if (property.getLocalName().startsWith("connectedTo")) {
                final String auxId = from.getLocalName()+"_"+property.getLocalName();
                Node aux = digraph.getNode(auxId);
                if (aux == null) {
                    Graph graph = digraph.graphOf(from.getLocalName());
                    aux = graph.addNode(auxId);
                    aux.set("label", "");
                    aux.set("shape", "circle");
                    aux.set("width", Double.valueOf(0.1));
                    Edge auxEdge = graph.addEdge(from.getLocalName(), auxId);
                    auxEdge.set("dir", "none");
                }
                digraph.addEdge(auxId, to.getLocalName());
            } else {
                Edge edge = digraph.addEdge(from.getLocalName(), to.getLocalName());
                edge.set("label", property.getLocalName());
            }
        }
        
        final String filenameDiagram = "system_architecture.dot";
        try (FileWriter writer = new FileWriter(filenameDiagram)) {
            digraph.output(writer);
            log.info("saved diagram to "+filenameDiagram);
        }
    }

    public void buildTestcaseModel()
    {
        // create a copy of the original model that will be augmented in subsequent steps
        testcaseModel = ModelFactory.createOntologyModel(ontologyModel.getSpecification() /*, ontologyModel*/);
        Ontology ontology = testcaseModel.createOntology("http://www.syngenio.de/ontology/testing/failover/testcases");
        ontology.addImport(testcaseModel.createResource("http://www.opelbank.de/ontology/systemmodel"));
        testcaseModel.loadImports();
        for (Entry<String, String> entry : prefixMap.entrySet()) {
            testcaseModel.setNsPrefix(entry.getKey().replaceFirst(":$", ""), entry.getValue());
        }
        
        stepClass = testcaseModel.createClass(NS_FAILOVER+"TestcaseStep");
        hasIndex = testcaseModel.createProperty(NS_FAILOVER+"hasIndex");
        hasStep = testcaseModel.createProperty(NS_FAILOVER+"hasStep");
        
        testcaseOrService = testcaseModel.createProperty(NS_FAILOVER+"testcaseOrService");
        propertyPreviousStep = testcaseModel.createProperty(NS_FAILOVER+"hasPreviousStep");
        
        configuration = testcaseModel.createClass(NS_FAILOVER+"Configuration");
        hasConfig = testcaseModel.createProperty(NS_FAILOVER+"hasConfiguration");
        
        isInactive = testcaseModel.createProperty(NS_FAILOVER+"isInactive");
        
        testcaseClass = testcaseModel.createClass(NS_FAILOVER+"Testcase");


    }

    private void generateWorkbook() throws IOException
    {
        String filename = "testcases2.xlsx";
        workbook = new XSSFWorkbook();
        creationHelper = workbook.getCreationHelper();

        headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);

        disruptStyle = workbook.createCellStyle();
        disruptStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        disruptStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        disruptStyle.setBorderBottom(BorderStyle.THIN);
        
        restoreStyle = workbook.createCellStyle();
        restoreStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        restoreStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        restoreStyle.setBorderBottom(BorderStyle.THIN);
        
        dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(creationHelper.createDataFormat().getFormat("d.m.yyyy hh:mm"));
        
//        okStyle = workbook.createCellStyle();
//        okStyle.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
//        okStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
//        okStyle.setBorderBottom(BorderStyle.THIN);
//        
//        notOkStyle = workbook.createCellStyle();
//        notOkStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
//        notOkStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
//        notOkStyle.setBorderBottom(BorderStyle.THIN);
//        
//        notTestableStyle = workbook.createCellStyle();
//        notTestableStyle.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
//        notTestableStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
//        notTestableStyle.setBorderBottom(BorderStyle.THIN);

        XSSFSheet overviewSheet = workbook.createSheet("Overview");
        
        XSSFRow headerRow = overviewSheet.createRow(0);
        headerRow.createCell(0).setCellValue("Testcase");
        overviewSheet.setColumnWidth(0, 75*256);
        headerRow.createCell(1).setCellValue("Description");
        overviewSheet.setColumnWidth(1, 100*256);
        headerRow.createCell(2).setCellValue("Service to monitor");
        overviewSheet.setColumnWidth(2, 30*256);
        headerRow.createCell(3).setCellValue("# of Steps");
        headerRow.createCell(4).setCellValue("Requires Support By");
        overviewSheet.setColumnWidth(4, 80*256);
        headerRow.createCell(5).setCellValue("Starts at");
        overviewSheet.setColumnWidth(5, 20*256);
        headerRow.createCell(6).setCellValue("Ends at");
        overviewSheet.setColumnWidth(6, 20*256);

        for (RDFNode nodeTestcase : query("select ?testcase where { ?testcase a failover:Testcase } order by ?testcase", "testcase")) {
            Individual testcase = nodeTestcase.as(Individual.class);
            Individual service = query("select ?service where { %s failover:testcaseOrService ?service }", "service", testcase).get(0).as(Individual.class);
            
            XSSFRow testcaseRow = overviewSheet.createRow(overviewSheet.getLastRowNum()+1);
            final XSSFCell testcaseCell = testcaseRow.createCell(0);
            testcaseCell.setCellValue(getLabel(testcase));
            testcaseRow.createCell(1).setCellValue(testcase.getComment(null));
            
            XSSFSheet testcaseSheet = workbook.createSheet(testcase.getLocalName());
            XSSFRow testcaseHeader0 = testcaseSheet.createRow(0);
            int currentCol = 0;
            final int stepCol = currentCol++;
            final XSSFCell stepHeaderCell = testcaseHeader0.createCell(stepCol);
            stepHeaderCell.setCellValue("Step");
            testcaseSheet.setColumnWidth(stepCol, 20*256);
            final int actionCol = currentCol++;
            final XSSFCell actionHeaderCell = testcaseHeader0.createCell(actionCol);
            actionHeaderCell.setCellValue("Action");
            testcaseSheet.setColumnWidth(actionCol, 90*256);
            final int durationCol = currentCol++;
            final XSSFCell durationHeaderCell = testcaseHeader0.createCell(durationCol);
            durationHeaderCell.setCellValue("Duration [min]");
            testcaseSheet.setColumnWidth(durationCol, 20*256);
            final int startsAtCol = currentCol++;
            final XSSFCell startsAtHeaderCell = testcaseHeader0.createCell(startsAtCol);
            startsAtHeaderCell.setCellValue("Starts at");
            testcaseSheet.setColumnWidth(startsAtCol, 20*256);
            final int endsAtCol = currentCol++;
            final XSSFCell endsAtHeaderCell = testcaseHeader0.createCell(endsAtCol);
            endsAtHeaderCell.setCellValue("Ends at");
            testcaseSheet.setColumnWidth(endsAtCol, 20*256);
            final int detailsCol = currentCol++;
            final XSSFCell detailsHeaderCell = testcaseHeader0.createCell(detailsCol);
            detailsHeaderCell.setCellValue("Details");
            testcaseSheet.setColumnWidth(detailsCol, 90*256);
            final int effectCol = currentCol++;
            final XSSFCell effectHeaderCell = testcaseHeader0.createCell(effectCol);
            effectHeaderCell.setCellValue("Effect");
            testcaseSheet.setColumnWidth(effectCol, 40*256);
            final int expectedHeaderCol = currentCol++;
            XSSFCell serviceCell = testcaseHeader0.createCell(expectedHeaderCol);
            serviceCell.setCellValue("State of service "+getLabel(service));
            testcaseSheet.setColumnWidth(expectedHeaderCol, 20*256);
            final int observedHeaderCol = currentCol++;
            XSSFCell emptyCell0 = testcaseHeader0.createCell(observedHeaderCol);
            testcaseSheet.setColumnWidth(observedHeaderCol, 20*256);
            mergeCells(testcaseSheet, serviceCell, emptyCell0);
            XSSFRow testcaseHeader1 = testcaseSheet.createRow(1);
            XSSFCell emptyCell1 = testcaseHeader1.createCell(stepCol);
            XSSFCell emptyCell2 = testcaseHeader1.createCell(actionCol);
            XSSFCell emptyCell6 = testcaseHeader1.createCell(durationCol);
            XSSFCell emptyCell5 = testcaseHeader1.createCell(startsAtCol);
            XSSFCell emptyCell7 = testcaseHeader1.createCell(endsAtCol);
            XSSFCell emptyCell3 = testcaseHeader1.createCell(detailsCol);
            XSSFCell emptyCell4 = testcaseHeader1.createCell(effectCol);
            mergeCells(testcaseSheet, stepHeaderCell, emptyCell1);
            mergeCells(testcaseSheet, actionHeaderCell, emptyCell2);
            mergeCells(testcaseSheet, durationHeaderCell, emptyCell6);
            mergeCells(testcaseSheet, startsAtHeaderCell, emptyCell5);
            mergeCells(testcaseSheet, endsAtHeaderCell, emptyCell7);
            mergeCells(testcaseSheet, detailsHeaderCell, emptyCell3);
            mergeCells(testcaseSheet, effectHeaderCell, emptyCell4);
            testcaseHeader1.createCell(expectedHeaderCol).setCellValue("expected");
            testcaseHeader1.createCell(observedHeaderCol).setCellValue("observed");

            testcaseHeader0.setRowStyle(headerStyle);
            testcaseHeader1.setRowStyle(headerStyle);
            
            testcaseRow.createCell(2).setCellValue(getLabel(service));

            Individual previousStep = null;
            XSSFRow previousRow = null;
            
            Individual step = query("select ?step where { %s failover:hasStep ?step . FILTER NOT EXISTS { ?step failover:hasPreviousStep ?previous } }", "step", testcase).get(0).as(Individual.class);
            while (true) {

//                testcaseRow.createCell(testcaseRow.getLastCellNum()).setCellValue(step.getComment(null));
                XSSFRow stepRow = testcaseSheet.createRow(testcaseSheet.getLastRowNum()+1);
                stepRow.createCell(stepCol).setCellValue("Step #"+step.getPropertyValue(hasIndex).asLiteral().getInt());
                stepRow.createCell(actionCol).setCellValue(getLabel(step));
                final XSSFCell durationCell = stepRow.createCell(durationCol);
                durationCell.setCellValue(10);
                final XSSFCell startsAtCell = stepRow.createCell(startsAtCol);
                startsAtCell.setCellStyle(dateStyle);
                if (previousRow == null) {
                    startsAtCell.setCellValue(new Date());
                } else {
                    startsAtCell.setCellFormula(previousRow.getCell(endsAtCol).getReference());
                }
                final XSSFCell endsAtCell = stepRow.createCell(endsAtCol);
                endsAtCell.setCellStyle(dateStyle);
                endsAtCell.setCellFormula(startsAtCell.getReference()+"+"+durationCell.getReference()+"/1440");
                stepRow.createCell(detailsCol).setCellValue(step.getComment(null));
                stepRow.createCell(effectCol).setCellValue(describeEffect(step, previousStep));
                stepRow.createCell(expectedHeaderCol).setCellValue(isAvailableInStep(service, step) ? "available" : "unavailable");

                // move to next step
                previousStep = step;
                
                previousRow = stepRow;
                
                final List<RDFNode> list = query("select ?step where { ?step failover:hasPreviousStep %s }", "step", step);
                if (list.isEmpty()) {
                    break;
                }
                step = list.get(0).as(Individual.class);
            }
            
            int numberOfSteps = previousStep == null ? 0 : previousStep.getPropertyValue(hasIndex).asLiteral().getInt();
            testcaseRow.createCell(3).setCellValue(numberOfSteps);
            
            String administrators = describeAdministratorsFor(testcase);
            testcaseRow.createCell(4).setCellValue(administrators);
            
            XSSFCell testcaseStartCell = testcaseRow.createCell(5);
            testcaseStartCell.setCellStyle(dateStyle);
            testcaseStartCell.setCellFormula(testcaseSheet.getSheetName()+"!"+testcaseSheet.getRow(2).getCell(startsAtCol).getReference());
            XSSFCell testcaseEndCell = testcaseRow.createCell(6);
            testcaseEndCell.setCellStyle(dateStyle);
            testcaseEndCell.setCellFormula(testcaseSheet.getSheetName()+"!"+previousRow.getCell(endsAtCol).getReference());

            XSSFHyperlink link = creationHelper.createHyperlink(XSSFHyperlink.LINK_DOCUMENT);
            link.setAddress("'"+testcaseSheet.getSheetName()+"'!A1");
            testcaseCell.setHyperlink(link);
            
        }
        
        createTable(overviewSheet);
        
        try (OutputStream stream = new FileOutputStream(filename)) {
            workbook.write(stream);
            log.info("saved test report to "+filename);
        }
    }

    private String describeAdministratorsFor(Individual testcase)
    {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (RDFNode administratorNode : query("select distinct ?administrator { ?resource a ?class . ?class failover:administeredBy ?administrator . ?testcase failover:hasStep/failover:hasConfiguration/failover:isInactive ?resource filter ( ?testcase = %s ) } order by ?administrator", "administrator", testcase )) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(getLabel(administratorNode.as(Individual.class)));
        }
        return sb.toString();
    }

    private String describeEffect(Individual step, Individual previousStep)
    {
        Set<Individual> unavailables = findUnavailables(step);
        Set<Individual> unavailablesPrevious = findUnavailables(previousStep);
        StringBuffer sb = new StringBuffer();
        Set<Individual> disruptions = new HashSet<Individual>(unavailables);
        disruptions.removeAll(unavailablesPrevious);
        if (!disruptions.isEmpty()) {
            sb.append("disrupted:");
            for (Individual disrupted : disruptions) {
                sb.append("\r\n"+getLabel(disrupted));
            }
        }
        Set<Individual> restores = new HashSet<Individual>(unavailablesPrevious);
        restores.removeAll(unavailables);
        if (!restores.isEmpty()) {
            sb.append("restored:");
            for (Individual restored : restores) {
                sb.append("\r\n"+getLabel(restored));
            }
        }
        return sb.toString();
    }

    /**
     * Finds resources that are unavailable in the given test step.
     * Will gracefully return an empty set if step is null 
     * @param step step or null
     * @return set of unavailable resources
     */
    private Set<Individual> findUnavailables(Individual step)
    {
        Set<Individual> unavailables = new HashSet<Individual>();
        if (step != null) {
            for (RDFNode unavailableNode : query("select distinct ?resource where { %s failover:hasConfiguration ?cfg . ?cfg failover:isUnavailable ?resource }", "resource", step)) {
                unavailables.add(unavailableNode.as(Individual.class));
            }
        }
        return unavailables;
    }

    private void createTable(XSSFSheet sheet)
    {
        /* Create Table into Existing Worksheet */
        XSSFTable my_table = sheet.createTable();    
        /* get CTTable object*/
        CTTable cttable = my_table.getCTTable();
        /* Define Styles */    
        CTTableStyleInfo table_style = cttable.addNewTableStyleInfo();
        table_style.setName("TableStyleMedium9");           
        /* Define Style Options */
        table_style.setShowColumnStripes(false); //showColumnStripes=0
        table_style.setShowRowStripes(true); //showRowStripes=1    
        /* Define the data range including headers */
        final short lastCellNum = sheet.getRow(0).getLastCellNum();
        AreaReference my_data_range = new AreaReference(new CellReference(0, 0), new CellReference(sheet.getLastRowNum(), lastCellNum-1));
        /* Set Range to the Table */
        cttable.setRef(my_data_range.formatAsString());
        cttable.setDisplayName(sheet.getSheetName());      /* this is the display name of the table */
        cttable.setName(sheet.getSheetName());    /* This maps to "displayName" attribute in &lt;table&gt;, OOXML */            
        cttable.setId(1L); //id attribute against table as long value
        /* Add header columns */               
        CTTableColumns columns = cttable.addNewTableColumns();
        columns.setCount(lastCellNum); //define number of columns
        /* Define Header Information for the Table */
        for (int i = 0; i < lastCellNum; i++)
        {
            CTTableColumn column = columns.addNewTableColumn();
            column.setName("Column" + i);
            column.setId(i + 1);
        }
    }

    public String getLabel(Individual individual)
    {
        final String label = individual.getLabel(null);
        return label != null ? label : individual.getLocalName();
    }

    private void saveRawModel() throws FileNotFoundException, IOException
    {
        final String testcaseModelFilename = ontologyFilename.replaceFirst("\\.[^.]+$", "")+"_testcases1.rdf";
        try (OutputStream out = new FileOutputStream(testcaseModelFilename)) {
            testcaseModel.write(out, "RDF/XML-ABBREV");
            log.info("saved testcase model to "+testcaseModelFilename);
        }
    }

    private void saveTestcaseModel() throws FileNotFoundException, IOException
    {
        final String testcaseModelFilename = ontologyFilename.replaceFirst("\\.[^.]+$", "")+"_testcases2.rdf";
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

    private List<Map<String, RDFNode>> query(String queryTemplate, Resource... resources)
    {
        List<Map<String, RDFNode>> results = new ArrayList<Map<String, RDFNode>>();
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
                Map<String, RDFNode> fields = new HashMap<String, RDFNode>();
                for (Iterator<String> i = querySolution.varNames(); i.hasNext();) {
                    String varName = i.next();
                    fields.put(varName, querySolution.get(varName));
                }
                results.add(fields);
            }
            log.debug(query+" -> "+results);
            return results;
        } catch (Throwable e) {
            throw new IllegalArgumentException(query, e);
        }
    }

    private boolean ask(String queryTemplate, Resource... resources)
    {
        List<Map<String, RDFNode>> results = new ArrayList<Map<String, RDFNode>>();
        String[] uris = new String[resources.length];
        for (int i = 0; i < resources.length; ++i) {
            uris[i] = withPrefixes(resources[i].getURI());
        }
        String query = String.format("ask { "+queryTemplate+" }", uris);
        try {
            QueryExecution qe = QueryExecutionFactory.create(generateQuery(query), testcaseModel);
            return qe.execAsk();
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
        // iterate over configurations
        int generation = 0;
        while (true) {
            
            ++generation;
            
            int count = 0;
            
            // infer unavailability by dependsOn
            String queryDependsOn = "construct {?cfg failover:isUnavailable ?unavailable} where { ?unavailable failover:dependsOn* ?dependency . ?cfg (failover:isInactive|failover:isUnavailable) ?dependency FILTER NOT EXISTS { ?cfg failover:isUnavailable ?unavailable } }";
            Model dependsOnModel = QueryExecutionFactory.create(generateQuery(queryDependsOn), testcaseModel).execConstruct();
            testcaseModel.add(dependsOnModel.listStatements());
            count += count(dependsOnModel.listStatements());
            
            // infer unavailability by connectedTo
            String queryConnectedTo = "construct {?cfg failover:isUnavailable ?resource} "+
                    "WHERE { ?resource   ?connection ?instance . ?connection rdfs:subPropertyOf failover:connectedTo filter not exists { ?subcon rdfs:subPropertyOf ?connection filter ( ?subcon != ?connection ) } ?cfg failover:isUnavailable ?instance "+
                    "FILTER NOT EXISTS { ?resource ?connection ?instance2 filter ( ?instance2 != ?instance ) FILTER NOT EXISTS { ?cfg (failover:isInactive|failover:isUnavailable) ?instance2 } } FILTER NOT EXISTS { ?cfg failover:isUnavailable ?resource } }";
            Model connectedToModel = QueryExecutionFactory.create(generateQuery(queryConnectedTo), testcaseModel).execConstruct();
            testcaseModel.add(connectedToModel.listStatements());
            count += count(connectedToModel.listStatements());
            for (StmtIterator i = connectedToModel.listStatements(); i.hasNext();) {
                log.info("generation "+generation+": "+i.next().toString());
            }
            log.info("added "+count);
            if (count == 0) {
                break;
            }
        }
        
        // check: there should be no available resources depending on unavailable resources
        String query = "select ?cfg ?available where { ?cfg (failover:isInactive|failover:isUnavailable) ?dependency . ?available failover:dependsOn* ?dependency FILTER NOT EXISTS { ?cfg failover:isUnavailable ?available } }";
        QueryExecution qe = QueryExecutionFactory.create(generateQuery(query), testcaseModel);
        ResultSet results = qe.execSelect();
        results.forEachRemaining(new Consumer<QuerySolution>() {public void accept(QuerySolution t) {}});
//        // Output query results
//        ResultSetFormatter.out(results);
//        System.out.println(""+results.getRowNumber()+" rows");
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

    void loadOntology()
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
        
        ontologyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
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
                value = getLabel(individual);
            }
            if (value == null) {
                value = ((Resource)r).getLocalName(); 
            }
        }
        return value != null ? value : "[]";
    }

    private void generateTestCases()
    {
        List<Map<String, Resource>> groups = findFailoverGroups();

        for (Map<String, Resource> group : groups) {
            List<Individual> failoverGroupMembers = findFailoverGroupMembers(group);
            if (failoverGroupMembers.size() < 2) {
                continue;
            }
            
            final Individual failoverGroup = group.get("group").as(Individual.class);
            final OntClass failoverClass = group.get("class").as(OntClass.class);
            
            generateTestcase(failoverGroup, failoverClass, failoverGroupMembers);
        }
    }

    private void generateTestcase(Individual failoverGroup, OntClass failoverClass, List<Individual> failoverGroupMembers)
    {
        final String testcaseUri = failoverGroup.getNameSpace()+"Testcase_"+failoverGroup.getLocalName()+"_"+failoverClass.getLocalName()+"_failover";
        final String testcaseLabel = "Testcase "+getLabel(failoverGroup)+" "+getLabel(failoverClass.as(Individual.class))+" failover";
        log.info("test case "+testcaseUri);
        final Individual testcase = createTestcase(testcaseUri, testcaseLabel);
        testcase.addComment("Test that "+getLabel(failoverGroup)+" can failover between "+getLabel(failoverClass.as(Individual.class))+" instances", null);
        
        final Individual service;
        
        if (failoverGroup.hasOntClass(NS_FAILOVER+"Service")) {
            service = failoverGroup;
        } else {
            service = findTestingServiceForGroup(failoverGroup);
        }
        log.info("test service "+service);
        testcase.addProperty(testcaseOrService, service);
        
        Individual previousStep = null;
        
        final Set<Individual> resourcesPreventingTest = findAlternativeResources(service, failoverGroup);
        if (!resourcesPreventingTest.isEmpty()) {
            previousStep = createPrepareStep(failoverGroup, testcase, service, resourcesPreventingTest);
        }

        // collect all critical dependencies (there might be common ones!)
        LinkedHashSet<Individual> criticalDependencies = new LinkedHashSet<Individual>();
        for (Individual failoverGroupMember : failoverGroupMembers) {
            criticalDependencies.addAll(findCriticalDependencies(failoverGroupMember, true));
        }
        
        // iterate over all critical dependencies to generate steps
        for (Individual criticalDependency : criticalDependencies) {
            previousStep = createDisruptRestoreSteps(service, failoverGroup, failoverGroupMembers, testcase, resourcesPreventingTest, previousStep, criticalDependency);
        }
        
        // test the first critical dependency once more
        for (Individual criticalDependency : criticalDependencies) {
            Individual lastStepCreated = createDisruptRestoreSteps(service, failoverGroup, failoverGroupMembers, testcase, resourcesPreventingTest, previousStep, criticalDependency);
            if (!previousStep.equals(lastStepCreated)) {
                // a new step was created, we're done
                break;
            }
        }
    }

    public Individual createPrepareStep(Individual failoverGroup, final Individual testcase, final Individual service,
            final Set<Individual> resourcesPreventingTest)
    {
        Individual previousStep;
        StringBuffer sbCommentPrepare = new StringBuffer();
        for (Individual resourcePreventingTest : resourcesPreventingTest) {
            if (sbCommentPrepare.length() > 0) {
                sbCommentPrepare.append(",\n");
            }
            sbCommentPrepare.append(describeDisrupt(resourcePreventingTest));
        }
        // deactivate all resources preventing the service test
        String prepareAction = "Deactivate redundant resources so that service "+getLabel(service)+" will fail if "+getLabel(failoverGroup)+" fails";
        String prepareDescription = sbCommentPrepare.toString();
        Individual prepStep = createStep(testcase, testcase.getURI()+"_prepare", 1, prepareAction, prepareDescription);
        Individual prepConfig = createConfig(prepStep);
        deactivate(prepConfig, resourcesPreventingTest);
        
        previousStep = prepStep;
        return previousStep;
    }
    
    private String describeDisrupt(Individual resource)
    {
        final List<RDFNode> disrupts = query("select ?disrupt where { %s a ?class . ?class rdfs:subClassOf failover:Resource . ?class failover:disrupt ?disrupt }", "disrupt", resource);
        final String description;
        if (disrupts.isEmpty()) {
            log.warn("class of "+resource.getURI()+" missing @disrupt");
            description = "shut it down";
        } else {
            description = disrupts.get(0).asLiteral().getString();
        }
        return "To deactivate "+resource.getLocalName()+", "+description;
    }

    private String describeRestore(Individual resource)
    {
        final List<RDFNode> restores = query("select ?restore where { %s a ?class . ?class rdfs:subClassOf failover:Resource . ?class failover:restore ?restore }", "restore", resource);
        final String description;
        if (restores.isEmpty()) {
            log.warn("class of "+resource.getURI()+" missing @restore");
            description = "start it up";
        } else {
            description = restores.get(0).asLiteral().getString();
        }
        return "To reactivate "+resource.getLocalName()+", "+description;
    }

    /**
     * Tries to create a disrupt and a restore step for the given criticalDependency.
     * @param service service to be monitored (should not fail)
     * @param failoverGroup
     * @param failoverGroupMembers
     * @param testcase
     * @param resourcesPreventingTest
     * @param previousStep the last step created or null if none
     * @param criticalDependency
     * 
     * @return the restore step created, if successful. Otherwise returns previousStep 
     */
    private Individual createDisruptRestoreSteps(Individual service, Individual failoverGroup, List<Individual> failoverGroupMembers, Individual testcase,
            Set<Individual> resourcesPreventingTest, Individual previousStep, Individual criticalDependency)
    {
        Set<Individual> affectedFailoverGroupMembers = findAffectedFailoverGroupMembers(failoverGroupMembers, criticalDependency);
        String affectedFailoverGroupMemberLabels = concatLabels(affectedFailoverGroupMembers);
        String affectedFailoverGroupMemberLocalNames = concatLocalNames(affectedFailoverGroupMembers);
        if (isStartable(criticalDependency) && !isExternal(criticalDependency)) {
            log.info("failover group "+failoverGroup.getLocalName()+" member(s) "+affectedFailoverGroupMemberLabels+" critically depends on "+criticalDependency.getLocalName());
            String commentDisrupt;
            String disruptUri; 
            String commentRestore;
            final String restoreUri;
            String disruptAction;
            String restoreAction;
            if (failoverGroupMembers.contains(criticalDependency)) {
                commentDisrupt =  describeDisrupt(criticalDependency);
                disruptUri = testcase.getURI()+"_deactivate_"+criticalDependency.getLocalName();
                disruptAction = "Disrupt "+getLabel(criticalDependency)+" directly";
                commentRestore = describeRestore(criticalDependency);
                restoreUri = testcase.getURI()+"_reactivate_"+criticalDependency.getLocalName();
                restoreAction = "Restore "+getLabel(criticalDependency);
            } else {
                commentDisrupt = describeDisrupt(criticalDependency)+". This will disrupt "+affectedFailoverGroupMemberLabels;
                disruptUri = testcase.getURI()+"_disrupt_"+affectedFailoverGroupMemberLocalNames+"_by_deactivating_"+criticalDependency.getLocalName();
                disruptAction = "Disrupt "+affectedFailoverGroupMemberLabels+" by deactivating "+getLabel(criticalDependency);
                commentRestore = describeRestore(criticalDependency)+". This will restore "+affectedFailoverGroupMemberLabels;
                restoreUri = testcase.getURI()+"_restore_"+affectedFailoverGroupMemberLocalNames+"_by_reactivating_"+criticalDependency.getLocalName();
                restoreAction = "Restore "+affectedFailoverGroupMemberLabels+" by reactivating "+getLabel(criticalDependency);
            }
            final Individual stepDisrupt = createStep(testcase, disruptUri, getNextIndex(previousStep), disruptAction, commentDisrupt);
            if (previousStep != null) {
                stepDisrupt.addProperty(propertyPreviousStep, previousStep);
            }
            final Individual configDisrupt = createConfig(stepDisrupt);
            deactivate(configDisrupt, resourcesPreventingTest);
            deactivate(configDisrupt, criticalDependency);
            
            // add isUnavailable statements induced by the disrupt step
            inferUnavailabilities();
            
            // only procede if this disruption does not disrupt the service
            if (!ask("%s failover:hasConfiguration ?cfg . ?cfg failover:isUnavailable %s", stepDisrupt, service)) {
                previousStep = stepDisrupt;
                
                final Individual stepRestore = createStep(testcase, restoreUri, getNextIndex(previousStep), restoreAction, commentRestore);
                stepRestore.addProperty(propertyPreviousStep, previousStep);
                final Individual configRestore = createConfig(stepRestore);
                deactivate(configRestore, resourcesPreventingTest);
                previousStep = stepRestore;
            } else {
                log.info("step "+stepDisrupt.getLocalName()+" excluded because it would disrupt service "+service.getLocalName());
            }
        }
        return previousStep;
    }

    private String concatLabels(Set<Individual> resources)
    {
        SortedSet<String> labels = new TreeSet<String>();
        for (Individual resource : resources) {
            labels.add(getLabel(resource));
        }
        switch (labels.size()) {
        case 0:
            return "";
        case 1:
            return labels.first();
        case 2:
            return labels.first()+" and "+labels.last();
        default:
            break;
        }
        StringBuilder sb = new StringBuilder();
        for (String label : labels) {
            if (!label.equals(labels.first())) {
                if (!label.equals(labels.last())) {
                    sb.append(", ");
                } else {
                    sb.append(", and ");
                }
            }
            sb.append(label);
        }
        return sb.toString();
    }

    private String concatLocalNames(Set<Individual> resources)
    {
        SortedSet<String> localNames = new TreeSet<String>();
        for (Individual resource : resources) {
            localNames.add(resource.getLocalName());
        }
        StringBuilder sb = new StringBuilder();
        for (String localName : localNames) {
            if (!localName.equals(localNames.first())) {
                sb.append("_");
            }
            sb.append(localName);
        }
        return sb.toString();
    }

    /**
     * Returns the subset of failoverGroupMembers that critically depend on criticalDependency
     * @param failoverGroupMembers
     * @param criticalDependency
     * @return
     */
    private Set<Individual> findAffectedFailoverGroupMembers(List<Individual> failoverGroupMembers, Individual criticalDependency)
    {
        Set<Individual> affectedFailoverGroupMembers = new HashSet<Individual>();
        for (Individual failoverGroupMember : failoverGroupMembers) {
            if (findCriticalDependencies(failoverGroupMember, true).contains(criticalDependency)) {
                affectedFailoverGroupMembers.add(failoverGroupMember);
            }
        }
        return affectedFailoverGroupMembers;
    }

    private int getNextIndex(Individual previousStep)
    {
        if (previousStep == null) {
            return 1;
        }
        return previousStep.getPropertyValue(hasIndex).asLiteral().getInt()+1;
    }

    private boolean isStartable(Individual resource)
    {
        return resource.hasOntClass(NS_FAILOVER+"Startable");
    }

    private boolean isExternal(Individual resource)
    {
        return resource.hasOntClass(NS_FAILOVER+"External");
    }

    private Individual createConfig(Individual step)
    {
        Individual config = testcaseModel.createIndividual(configuration);
        testcaseModel.add(step, hasConfig, config);
        return config;
    }

    private Individual createStep(Individual testcase, String stepUri, int index, String action, String description)
    {
        // do not re-use steps to avoid cycles
        if (testcaseModel.getIndividual(stepUri) != null) {
            stepUri += "_again";
        }
        Individual step = testcaseModel.createIndividual(stepUri, stepClass);
        step.setLabel(action, null);
        step.setComment(description, null);
        step.addLiteral(hasIndex, index);
        testcaseModel.add(testcase, hasStep, step);
        log.info("step #"+index+" "+stepUri);
        return step;
    }

    /**
     * Returns all resources whose failure causes dependee to fail as well
     * @param dependee a startable resource
     * @return
     */
    private LinkedHashSet<Individual> findCriticalDependencies(Individual dependee, boolean recursive)
    {
        LinkedHashSet<Individual> criticalDependencies = new LinkedHashSet<Individual>();
        criticalDependencies.add(dependee);
        for (Individual immediateCriticalDependency : findImmediateCriticalDependencies(dependee)) {
            if (recursive) {
                final LinkedHashSet<Individual> criticalSubDependencies = findCriticalDependencies(immediateCriticalDependency, recursive);
                criticalDependencies.addAll(criticalSubDependencies);
            } else {
                criticalDependencies.add(immediateCriticalDependency);
            }
        }
        return criticalDependencies;
    }

    /**
     * Returns all immediate dependencies of dependee that are critical
     * @param dependee a startable resource
     * @return
     */
    private Set<Individual> findImmediateCriticalDependencies(Individual dependee)
    {
        log.info("findImmediateCriticalDependencies("+dependee+")");
        Set<Individual> criticalDependencies = new HashSet<Individual>();
        final List<RDFNode> critDepNodes = query("select ?dependee ?criticalDependency where { { ?dependee a failover:Resource FILTER (?dependee = %s) . ?dependee failover:dependsOn ?criticalDependency } union { ?dependee a failover:Resource FILTER (?dependee = %s) . ?dependee ?connectedTo ?criticalDependency . ?connectedTo rdfs:subPropertyOf+ failover:connectedTo FILTER NOT EXISTS { ?subcon rdfs:subPropertyOf ?connectedTo FILTER ( ?subcon != ?connectedTo ) } . FILTER NOT EXISTS { ?dependee ?connectedTo ?otherDependency FILTER (?otherDependency != ?criticalDependency) } } }", "criticalDependency", dependee, dependee);
        for (RDFNode critDepNode : critDepNodes) {
            criticalDependencies.add(critDepNode.as(Individual.class));
        }
        return criticalDependencies;
    }

    /**
     * Find all members of the given failover group
     * @param group the group
     * @return
     */
    private List<Individual> findFailoverGroupMembers(Map<String, Resource> group)
    {
        Individual failoverGroup = group.get("group").as(Individual.class);
        OntClass failoverClass = group.get("class").as(OntClass.class);
        List<Individual> members = new ArrayList<Individual>();
        for (RDFNode memberNode : query("select ?member where { %s failover:connectedTo ?member . ?member a %s FILTER NOT EXISTS { ?member a failover:External } }", "member", failoverGroup, failoverClass)) {
            members.add(memberNode.as(Individual.class));
        }
        return members;
    }

    /**
     * Sets each resource to inactive in the given configuration
     * @param config the configuration
     * @param resourcesToDeactivate resources to deactivate in config
     */
    private void deactivate(Individual config, Set<Individual> resourcesToDeactivate)
    {
        for (Individual resourceToDeactivate : resourcesToDeactivate) {
            deactivate(config, resourceToDeactivate);
        }
    }

    /**
     * Sets resource to inactive in the given configuration
     * @param config the configuration
     * @param resourceToDeactivate resource to deactivate in config
     */
    private void deactivate(Individual config, Individual resourceToDeactivate)
    {
        if (isExternal(resourceToDeactivate)) {
            throw new IllegalArgumentException(resourceToDeactivate.getURI()+" is external");
        }
        testcaseModel.add(config, isInactive, resourceToDeactivate);
        log.info("configured inactive "+resourceToDeactivate.getLocalName());
    }

    /**
     * Returns all startable resources that we need 
     * to shutdown so that the service will 
     * immediately fail if the failover group fails
     * @param service the service
     * @param group the failover group
     * @return
     */
    private Set<Individual> findAlternativeResources(Individual service, Individual failoverGroup)
    {
        Set<Individual> chainResources = findConnectingChains(service, failoverGroup);
        
//        Set<Individual> dependencies = new HashSet<Individual>();
//        // moving up the chain from failoverGroup to service, find the first resource that has redundancies
//        List<List<RDFNode>> redundancies = query("select distinct ?redundantStartableMember1 ?redundantMember1 ?otherStartableMember1 ?otherMember1 where { ?redundantStartableMember1 a failover:Startable . ?redundantMember1 failover:dependsOn* ?redundantStartableMember1 . FILTER NOT EXISTS { ?redundantMember1 failover:dependsOn* ?redundantStartableIntermediate . ?redundantStartableIntermediate a failover:Startable ; failover:dependsOn+ ?redundantStartableMember1 FILTER ( ?redundantStartableIntermediate != ?redundantStartableMember1 ) } . ?otherStartableMember1 a failover:Startable . ?otherMember1 failover:dependsOn* ?otherStartableMember1 . FILTER NOT EXISTS { ?otherMember1 failover:dependsOn* ?otherStartableIntermediate . ?otherStartableIntermediate a failover:Startable ; failover:dependsOn+ ?otherStartableMember1 FILTER ( ?otherStartableIntermediate != ?otherStartableMember1 ) } . %s (failover:connectedTo|failover:dependsOn)+ ?parent1 . ?parent1 ?connectedTo1 ?redundantMember1 . ?connectedTo1 rdfs:subPropertyOf+ failover:connectedTo FILTER NOT EXISTS { ?subcon1 rdfs:subPropertyOf ?connectedTo1 FILTER ( ?subcon1 != ?connectedTo1 ) } . ?parent1 ?connectedTo1 ?otherMember1 FILTER (?otherMember1 != ?redundantMember1) . ?redundantMember1 (failover:dependsOn|failover:connectedTo)* %s . FILTER NOT EXISTS { ?redundantMember1 (failover:dependsOn|failover:connectedTo)* ?parent2 . ?connectedTo2 rdfs:subPropertyOf+ failover:connectedTo FILTER NOT EXISTS { ?subcon2 rdfs:subPropertyOf ?connectedTo2 FILTER ( ?subcon2 != ?connectedTo2 ) } . ?parent2 ?connectedTo2 ?member2 . ?member2 (failover:dependsOn|failover:connectedTo)* %s . ?parent2 ?connectedTo2 ?otherMember2 FILTER (?otherMember2 != ?member2)} }", service, failoverGroup, failoverGroup);
//        for (List<RDFNode> redundancy : redundancies) {
//            Individual chainMember = redundancy.get(0).as(Individual.class);
//            Individual otherMember = redundancy.get(1).as(Individual.class);
//            if (!otherMember.equals(chainMember)) {
//                dependencies.add(otherMember);
//            }
//        }
//        // remove resources that critically depend on failoverGroup
//        for (Iterator<Individual> i = dependencies.iterator(); i.hasNext();) {
//            final Individual dependency = i.next();
//            final LinkedHashSet<Individual> criticalDependencies = findCriticalDependencies(dependency, true);
//            if (criticalDependencies.contains(failoverGroup)) {
//                i.remove();
//            }
//        }
//        return dependencies;
        Set<Individual> redundancies = new HashSet<Individual>();
//        List<List<Resource>> failoverPairs = queryResources("select distinct ?parent ?member ?otherMember where { ?parent ?connectedTo ?member . ?parent ?connectedTo ?otherMember FILTER (?member != ?otherMember) . ?connectedTo rdfs:subPropertyOf+ failover:connectedTo FILTER NOT EXISTS { ?subcon rdfs:subPropertyOf ?connectedTo FILTER ( ?subcon != ?connectedTo ) } }");
//        for (List<Resource> failoverPair : failoverPairs) {
//            Individual parent = failoverPair.get(0).as(Individual.class);
//            Individual member = failoverPair.get(1).as(Individual.class);
//            Individual otherMember = failoverPair.get(2).as(Individual.class);
//            if (chainResources.contains(parent) && chainResources.contains(member) && !chainResources.contains(otherMember)) {
//                redundancies.add(otherMember);
//            }
//        }
        
        List<RDFNode> redundantNodes = query("select distinct ?otherMember where {  %s (failover:connectedTo|failover:dependsOn)* ?parent . ?parent ?connectedTo ?member . ?parent ?connectedTo ?otherMember FILTER (?member != ?otherMember) . ?connectedTo rdfs:subPropertyOf+ failover:connectedTo FILTER NOT EXISTS { ?subcon rdfs:subPropertyOf ?connectedTo FILTER ( ?subcon != ?connectedTo ) } . ?member (failover:connectedTo|failover:dependsOn)* %s .    FILTER NOT EXISTS { ?otherMember (failover:connectedTo|failover:dependsOn)* %s } }", "otherMember", service, failoverGroup, failoverGroup);
        for (RDFNode redundantNode : redundantNodes) {
            redundancies.add(redundantNode.as(Individual.class));
        }
        
        Set<Individual> criticalResources = new HashSet<Individual>();
        for (Individual redundancy : redundancies) {
            LinkedHashSet<Individual> criticalDependencies = findCriticalDependencies(redundancy, true);
            for (Individual criticalResource : criticalDependencies) {
                if (isStartable(criticalResource) && !isExternal(criticalResource)) {
                    criticalResources.add(criticalResource);
                    break;
                }
            }
        }
        return criticalResources;
    }

    private Set<Individual> findConnectingChains(Individual service, Individual failoverGroup)
    {
        Set<Individual> chain = new HashSet<Individual>();
        List<RDFNode> nodes = query("select distinct ?link where { %s (failover:connectedTo|failover:dependsOn)* ?link . ?link (failover:connectedTo|failover:dependsOn)* %s }", "link", service, failoverGroup);
        for (RDFNode node : nodes) {
            chain.add(node.as(Individual.class));
        }
        return chain;
    }

    /**
     * Find all failover groups in the model
     * @return
     */
    private List<Map<String, Resource>> findFailoverGroups()
    {
        return queryResources("SELECT ?group ?class WHERE {?group ?connectedTo ?resource . ?connectedTo rdfs:subPropertyOf+ failover:connectedTo FILTER NOT EXISTS { ?subcon rdfs:subPropertyOf ?connectedTo FILTER ( ?subcon != ?connectedTo ) } . ?connectedTo rdfs:range ?class . ?class rdfs:subClassOf failover:Resource . FILTER NOT EXISTS { ?subclass rdfs:subClassOf ?class ; rdfs:subClassOf failover:Resource FILTER ( ?subclass != ?class && ?subclass != owl:Nothing ) } } GROUP BY ?class ?group HAVING (count(?resource)>1) ORDER BY ?group ?class");
    }

    private List<Map<String, Resource>> queryResources(String string)
    {
        List<Map<String, RDFNode>> rows = query(string);
        List<Map<String, Resource>> result = new ArrayList<Map<String, Resource>>();
        for (Map<String, RDFNode> row : rows) {
            Map<String, Resource> resourcesRow = new HashMap<String, Resource>();
            for (Entry<String, RDFNode> entry : row.entrySet()) {
                resourcesRow.put(entry.getKey(), entry.getValue().asResource());
            }
            result.add(resourcesRow);
        }
        return result;
    }

    /**
     * Find one service that can test the availability of the failover group
     * @param failoverGroup the failover group to be tested
     * @return the testing service
     */
    private Individual findTestingServiceForGroup(Individual failoverGroup)
    {
        List<RDFNode> services = query("select ?service ?failoverGroup ?distance where { ?service a failover:Service . ?failoverGroup a failover:Resource FILTER ( ?failoverGroup = %s ) . { select ?service ?failoverGroup (count(?mid) as ?distance) { ?service (failover:connectedTo|failover:dependsOn)* ?mid . ?mid (failover:connectedTo|failover:dependsOn)+ ?failoverGroup } group by ?service ?failoverGroup } } order by ?distance", "service", failoverGroup);
        if (services.isEmpty()) {
            return null;
        }
        return services.get(0).as(Individual.class);
    }
    
    private Individual _findTestingServiceForGroup(Individual failoverGroup)
    {
        OntClass serviceClass = testcaseModel.getOntClass(NS_FAILOVER+"Service");
        // ascend dependencies until we find a service
        List<Individual> dependees = new ArrayList<Individual>();
        // to start the iteration, add the failoverGroup itself to the list
        dependees.add(failoverGroup);
        while (dependees.addAll(findDependees(dependees)));
        // find a service
        for (Individual resource : dependees) {
            if (resource.hasOntClass(serviceClass)) {
                return resource;
            }
        }
        throw new IllegalArgumentException("no service depends on "+failoverGroup);
    }

    private Collection< ? extends Individual> findDependees(List<Individual> dependees)
    {
        return null;
    }

    private Individual createTestcase(String uri, String label)
    {
        final Individual testcase = testcaseModel.createIndividual(uri, testcaseClass);
        testcase.setLabel(label, null);
        return testcase;
    }

    @SuppressWarnings("unused")
    private void debugQuery(String queryTemplate, Resource... resources)
    {
        List<RDFNode> results = new ArrayList<RDFNode>();
        String[] uris = new String[resources.length];
        for (int i = 0; i < resources.length; ++i) {
            uris[i] = withPrefixes(resources[i].getURI());
        }
        String query = String.format(queryTemplate, (Object[])uris);
        try {
            QueryExecution qe = QueryExecutionFactory.create(generateQuery(query), testcaseModel);
            ResultSet resultSet = qe.execSelect();
            ResultSetFormatter.out(System.out, resultSet);
            System.out.println(""+resultSet.getRowNumber()+" rows");
        } catch (Throwable e) {
            throw new IllegalArgumentException(query, e);
        }
    }

    private void mergeCells(XSSFSheet testcaseSheet, XSSFCell cell1, XSSFCell cell2)
    {
        CellRangeAddress region = new CellRangeAddress(cell1.getRowIndex(), cell2.getRowIndex(), cell1.getColumnIndex(), cell2.getColumnIndex());
        testcaseSheet.addMergedRegion(region );
    }
}
