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

import org.apache.poi.ss.formula.functions.Hyperlink;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFCreationHelper;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntDocumentManager.ReadFailureHandler;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

public class FailoverTestReportOld implements Runnable
{
    private static final String NS_FAILOVER = "http://www.syngenio.de/ontology/testing/failover#";
    private static final String NS_TESTRESULTS = "http://www.opelbank.de/ontology/testing/failover/testresults#";
    private static final String NS_GMAC = "http://www.opelbank.de/ontology/systemmodel#";
    private static final String NS_FISK = "http://www.kordoba.de/ontology/components#";
    private final static Logger log = LoggerFactory.getLogger(FailoverTestReportOld.class);
    private String testcaseOntologyFilename;
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
    private XSSFCreationHelper creationHelper;
    private XSSFCellStyle okStyle;
    private XSSFCellStyle notOkStyle;
    private XSSFCellStyle notTestableStyle;

    enum Status { incomplete, ok, failed, notTestable };
    
    public FailoverTestReportOld(String[] args)
    {
        int index = 0;
        testcaseOntologyFilename = args[index++];
    }

    public static void main(String[] args)
    {
        try
        {
            new FailoverTestReportOld(args).run();
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

        generateWorkbook("failover_test_report.xlsx");
        
    }

    private void generateWorkbook(String filename) throws FileNotFoundException, IOException
    {
        workbook = new XSSFWorkbook();
        
        creationHelper = workbook.getCreationHelper();
        
        //to enable newlines you need set a cell styles with wrap=true
        wrapStyle = workbook.createCellStyle();
        wrapStyle.setWrapText(true);
        
        servicesStyle = workbook.createCellStyle();
        servicesStyle.setFillForegroundColor(IndexedColors.GOLD.getIndex());
        servicesStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        servicesStyle.setBorderBottom(BorderStyle.THIN);
        servicesCol0Style = (XSSFCellStyle) servicesStyle.clone();
        servicesCol0Style.setRotation((short) 90);
        servicesCol0Style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        resourcesStyle = workbook.createCellStyle();
        resourcesStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        resourcesStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        resourcesStyle.setBorderBottom(BorderStyle.THIN);
        resourcesCol0Style = (XSSFCellStyle) resourcesStyle.clone();
        resourcesCol0Style.setRotation((short) 90);
        resourcesCol0Style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        disruptHeaderStyle = workbook.createCellStyle();
        disruptHeaderStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        disruptHeaderStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        disruptHeaderStyle.setBorderBottom(BorderStyle.THIN);
        
        restoreHeaderStyle = workbook.createCellStyle();
        restoreHeaderStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        restoreHeaderStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        restoreHeaderStyle.setBorderBottom(BorderStyle.THIN);

        okStyle = workbook.createCellStyle();
        okStyle.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
        okStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        okStyle.setBorderBottom(BorderStyle.THIN);
        
        notOkStyle = workbook.createCellStyle();
        notOkStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        notOkStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        notOkStyle.setBorderBottom(BorderStyle.THIN);
        
        notTestableStyle = workbook.createCellStyle();
        notTestableStyle.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
        notTestableStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        notTestableStyle.setBorderBottom(BorderStyle.THIN);

        // create one sheet for each test case
        for (RDFNode nodeTestcase : query("select ?testcase where { ?testcase a failover:Testcase } order by ?testcase", "testcase")) {
            Individual testcase = nodeTestcase.as(Individual.class);
            generateTestcaseSheet(testcase);
        }
        
        // create an overview sheet
        generateOverviewSheet();
        
        try (OutputStream stream = new FileOutputStream(filename)) {
            workbook.write(stream);
            log.info("saved test report to "+filename);
        }
    }

    private void generateOverviewSheet()
    {
        XSSFSheet overviewSheet = workbook.createSheet("Overview");
        workbook.setSheetOrder(overviewSheet.getSheetName(), 0); // move to front
        XSSFRow headerRow = overviewSheet.createRow(0);
        headerRow.createCell(0).setCellValue("Testcase");
        overviewSheet.setColumnWidth(0, 25*256);
        headerRow.createCell(1).setCellValue("Remark");
        overviewSheet.setColumnWidth(1, 60*256);
        headerRow.createCell(2).setCellValue("Description");
        overviewSheet.setColumnWidth(2, 120*256);
        headerRow.createCell(3).setCellValue("open checks");
        overviewSheet.setColumnWidth(3, 15*256);
        headerRow.createCell(4).setCellValue("failed checks");
        overviewSheet.setColumnWidth(4, 15*256);
        headerRow.createCell(5).setCellValue("ok checks");
        overviewSheet.setColumnWidth(5, 15*256);
        headerRow.createCell(5).setCellValue("untestable checks");
        overviewSheet.setColumnWidth(6, 15*256);
        for (RDFNode nodeTestcase: query("select ?testcase where { ?testcase a failover:Testcase } order by ?testcase", "testcase")) {
            Individual testcase = nodeTestcase.as(Individual.class);
            XSSFRow testcaseRow = overviewSheet.createRow(overviewSheet.getLastRowNum()+1);
            XSSFCell testcaseCell = testcaseRow.createCell(0);
            testcaseCell.setCellValue(testcase.getLocalName());
            Observation obtc = new Observation(testcase, null);
            if (obtc.status != null) {
                XSSFCell remarkCell = testcaseRow.createCell(1);
                remarkCell.setCellValue(obtc.log);
            }
            testcaseRow.createCell(2).setCellValue(testcase.getComment(null));
            XSSFHyperlink link = creationHelper.createHyperlink(XSSFHyperlink.LINK_DOCUMENT);
            link.setAddress("'"+testcase.getLocalName()+"'!A1");
            testcaseCell.setHyperlink(link);
            
            TestcaseStatus status = getTestcaseStatus(testcase);
            testcaseRow.createCell(3).setCellValue(status.countSteps-status.countOk-status.countNotOk);
            testcaseRow.createCell(4).setCellValue(status.countNotOk);
            testcaseRow.createCell(5).setCellValue(status.countOk);
            testcaseRow.createCell(6).setCellValue(status.countNotTestable);
        }
    }

    private TestcaseStatus getTestcaseStatus(Individual testcase)
    {
        List<RDFNode> serviceNodes = query("select distinct ?service where { ?service a failover:Service . ?service (failover:dependsOn|failover:connectedTo)+ ?unavailable . ?cfg failover:isUnavailable ?unavailable . %s failover:hasStep ?step . ?step failover:hasConfiguration ?cfg }", "service", testcase);;
        
        List<RDFNode> steps = getTestcaseSteps(testcase);
        List<RDFNode> resourceNodes = query("select distinct ?resource where { ?resource a failover:Resource . ?resource (failover:dependsOn|failover:connectedTo)* ?unavailable . ?cfg failover:isUnavailable ?unavailable . ?step failover:hasConfiguration ?cfg . %s failover:hasStep ?step }", "resource", testcase);
        
        List<RDFNode> nodes = new ArrayList<RDFNode>();
        nodes.addAll(serviceNodes);
        nodes.addAll(resourceNodes);
        
        TestcaseStatus testcaseStatus = new TestcaseStatus();
        testcaseStatus.countSteps = steps.size();
        testcaseStatus.countOk = 0;
        testcaseStatus.countNotOk = 0;
        testcaseStatus.countNotTestable = 0;
        for (RDFNode step : steps) {
            Status status = null;
            for (RDFNode resource : nodes) {
                Observation tcObservation = new Observation(testcase, resource.as(Individual.class));
                Observation observation = new Observation(step.as(Individual.class), resource.as(Individual.class));
                status = promoteStatus(status, tcObservation.status);
                status = promoteStatus(status, observation.status);
            }
            if (status != null) {
                switch (status) {
                case ok:
                    ++testcaseStatus.countOk;
                    break;
                case failed:
                    ++testcaseStatus.countNotOk;
                    break;
                case notTestable:
                    ++testcaseStatus.countNotTestable;
                    break;
                }
            }
            testcaseStatus.status = promoteStatus(testcaseStatus.status, status);
        }
        return testcaseStatus;
    }
    
    static class TestcaseStatus {
        public int countNotTestable;
        public Status status = null;
        int countSteps = 0;
        int countOk = 0;
        int countNotOk = 0;
        
        @Override
        public String toString()
        {
            StringBuffer sb = new StringBuffer();
            if (status != null) {
                sb.append(status.name()+" ");
            }
            sb.append("(ok "+countOk+", not testable "+countNotTestable+" of "+countSteps+")");
            return sb.toString();
        }
    }

    private void generateTestcaseSheet(Individual testcase)
    {
        final String testcaseName = toString(testcase);
        XSSFSheet testcaseSheet = workbook.createSheet(testcaseName);
        testcaseSheet.setColumnWidth(1, 25*256);
        testcaseSheet.setColumnWidth(2, 10*256);
        List<RDFNode> steps = getTestcaseSteps(testcase);
        addHeading(testcaseSheet, testcase);
        addHeader(testcaseSheet, steps);
        addServicesRows(testcaseSheet, testcase, steps);
        addMonitoredResourcesRows(testcaseSheet, testcase, steps);
        log.info("sheet for testcase "+testcaseName+" created");
    }

    private void addHeading(XSSFSheet testcaseSheet, Individual testcase)
    {
        XSSFRow statusRow = testcaseSheet.createRow(testcaseSheet.getLastRowNum()+1);
        TestcaseStatus status = getTestcaseStatus(testcase);
        statusRow.createCell(0).setCellValue(status.toString());

        testcaseSheet.createRow(testcaseSheet.getLastRowNum()+1).createCell(0).setCellValue(testcase.getComment(null));        
    }

    private int addServicesRows(XSSFSheet testcaseSheet, Individual testcase, List<RDFNode> steps)
    {
//        debugQuery("select distinct ?testcase ?cfg ?unavailable ?service where { ?cfg failover:isUnavailable ?unavailable . %s failover:hasStep ?step . ?step failover:hasConfiguration ?cfg . ?service a failover:Service . ?service (failover:dependsOn|failover:connectedTo)+ ?unavailable }", testcase);
        List<RDFNode> services = query("select distinct ?service where { ?service a failover:Service . ?service (failover:dependsOn|failover:connectedTo)+ ?unavailable . ?cfg failover:isUnavailable ?unavailable . %s failover:hasStep ?step . ?step failover:hasConfiguration ?cfg }", "service", testcase);
        XSSFCell firstCol0 = null;
        XSSFCell lastCol0 = null;
        for (RDFNode nodeService : services) {
            Individual service = nodeService.as(Individual.class);
            Observation testcaseObservation = new Observation(testcase, service);
            XSSFRow serviceRowExpected = testcaseSheet.createRow(testcaseSheet.getLastRowNum()+1);
            lastCol0 = serviceRowExpected.createCell(0);
            if (firstCol0 == null) {
                firstCol0 = lastCol0;
            }
            XSSFCell serviceCell = serviceRowExpected.createCell(1);
            serviceCell.setCellValue(toString(service));
            serviceCell.setCellStyle(servicesStyle);
            XSSFCell expectedCell = serviceRowExpected.createCell(2);
            expectedCell.setCellValue("expected");
            expectedCell.setCellStyle(servicesStyle);
            XSSFRow serviceRowObserved = testcaseSheet.createRow(testcaseSheet.getLastRowNum()+1);
            lastCol0 = serviceRowObserved.createCell(0);
            XSSFCell emptyCell = serviceRowObserved.createCell(1);
            XSSFCell observedCell = serviceRowObserved.createCell(2);
            observedCell.setCellValue("observed");
            observedCell.setCellStyle(servicesStyle);
            serviceRowExpected.createCell(3).setCellValue("available");
            
            Status serviceStatus = null;
            
            for (int i = 0; i < steps.size(); ++i) {
                Individual step = steps.get(i).as(Individual.class);
                boolean isDisrupt = step.getLocalName().endsWith("_disrupt");
                boolean available = isAvailableInStep(service, step);
                XSSFCell cellExpected = serviceRowExpected.createCell(i+4);
                cellExpected.setCellValue((!isDisrupt || available) ? "available" : "unavailable");
                
                Observation ob = new Observation(step, service);
                XSSFCell cellObserved = serviceRowObserved.createCell(i+4);
                if (testcaseObservation.status != null) {
                    cellObserved.setCellValue(testcaseObservation.log);
                    log.info(testcase+" "+service+" "+testcaseObservation.log);
                }
                if (ob.status != null) {
                    cellObserved.setCellValue(ob.log);
                    log.info(testcase+" "+step+" "+service+" "+ob.log);
                }
                serviceStatus = promoteStatus(serviceStatus, ob.status);
            }
            setRowStyle(serviceRowObserved, steps, serviceStatus);
            
            mergeCells(testcaseSheet, serviceCell, emptyCell);
        }
        if (firstCol0 != null && lastCol0 != null) {
            mergeCells(testcaseSheet, firstCol0, lastCol0);
            firstCol0.setCellValue("Services");
            firstCol0.setCellStyle(servicesCol0Style);
        }
        return testcaseSheet.getLastRowNum()+1;
    }
    
    private int addMonitoredResourcesRows(XSSFSheet testcaseSheet, Individual testcase, List<RDFNode> steps)
    {
        List<RDFNode> resources = query("select distinct ?resource where { ?resource a failover:Resource . filter not exists { ?resource rdfs:subClassOf failover:Service } . ?resource (failover:dependsOn|failover:connectedTo)* ?unavailable . ?cfg failover:isUnavailable ?unavailable . ?step failover:hasConfiguration ?cfg . %s failover:hasStep ?step }", "resource", testcase);
        XSSFCell firstCol0 = null;
        XSSFCell lastCol0 = null;
        for (RDFNode nodeResource : resources) {
            Individual resource = nodeResource.as(Individual.class);
            
            XSSFRow resourceRowExpected = testcaseSheet.createRow(testcaseSheet.getLastRowNum()+1);
            lastCol0 = resourceRowExpected.createCell(0);
            if (firstCol0 == null) {
                firstCol0 = lastCol0;
            }
            XSSFCell resourceCell = resourceRowExpected.createCell(1);
            resourceCell.setCellValue(toString(resource));
            resourceCell.setCellStyle(resourcesStyle);
            XSSFCell expectedCell = resourceRowExpected.createCell(2);
            expectedCell.setCellValue("expected");
            expectedCell.setCellStyle(resourcesStyle);
            resourceRowExpected.createCell(3).setCellValue("available");
            
            XSSFRow resourceRowObserved = testcaseSheet.createRow(testcaseSheet.getLastRowNum()+1);
            lastCol0 = resourceRowObserved.createCell(0);
            XSSFCell emptyCell = resourceRowObserved.createCell(1);
            emptyCell.setCellStyle(resourcesStyle);
            XSSFCell observedCell = resourceRowObserved.createCell(2);
            observedCell.setCellValue("observed");
            observedCell.setCellStyle(resourcesStyle);
            
            Status resourceStatus = null;
            
            for (int i = 0; i < steps.size(); ++i) {
                Individual step = steps.get(i).as(Individual.class);
                XSSFCell cell = resourceRowExpected.createCell(i+4);
                final String expected = isAvailableInStep(resource, step) ? "available" : "unavailable";
                cell.setCellValue(expected);
                
                Observation ob = new Observation(step, resource);
                XSSFCell cellObserved = resourceRowObserved.createCell(i+4);
                if (ob.status != null) {
                    cellObserved.setCellValue(ob.log);
                    log.info(testcase+" "+step+" "+resource+" "+ob.log);
                } else {
//                    cellObserved.setCellValue(expected);
                }
                resourceStatus = promoteStatus(resourceStatus, ob.status);
                
            }
            
            setRowStyle(resourceRowObserved, steps, resourceStatus);
            
            mergeCells(testcaseSheet, resourceCell, emptyCell);
        }
        if (firstCol0 != null && lastCol0 != null) {
            mergeCells(testcaseSheet, firstCol0, lastCol0);
            firstCol0.setCellValue("Resources");
            firstCol0.setCellStyle(resourcesCol0Style);
        }
        return testcaseSheet.getLastRowNum()+1;
    }

    public void setRowStyle(XSSFRow rowObserved, List<RDFNode> steps, Status status)
    {
        XSSFCellStyle resourceRowStyle = null;
        if (status != null) {
            switch (status) {
            case failed:
                resourceRowStyle = notOkStyle;
                break;
            case ok:
                resourceRowStyle = okStyle;
                break;
            case notTestable:
                resourceRowStyle = notTestableStyle;
                break;
            default:
                break;
            }
        }
        if (resourceRowStyle != null) {
            for (int i = 0; i < steps.size(); ++i) {
                XSSFCell cell = rowObserved.getCell(4+i);
                if (cell != null) {
                    cell.setCellStyle(resourceRowStyle);
                }
            }
        }
    }
    
    private Status promoteStatus(Status previousStatus, Status update)
    {
        if (previousStatus == null) {
            return update;
        }
        switch (previousStatus) {
        case incomplete:
            return Status.incomplete;
        case failed:
            return Status.failed;
        case notTestable:
            return Status.notTestable;
        case ok:
            return update;
        default:
            throw new Error("should not occur "+previousStatus);
        }
    }

    class Observation {
        Status status = null;
        String log;
        
        Observation(Individual testcaseOrStep, Individual resourceOrService) {
            List<Map<String, RDFNode>> observations;
            if (resourceOrService != null) {
                observations = query("select ?observation ?class where { ?observation rdf:type ?class . ?class rdfs:subClassOf+ testresults:Observation filter ( ?class != testresults:Observation ) . ?observation testresults:testcaseOrStep %s . ?observation testresults:resourceOrService %s . OPTIONAL { ?observation testresults:timestamp ?ts } } order by ?ts", testcaseOrStep, resourceOrService);
            } else {
                observations = query("select ?observation ?class where { ?observation rdf:type ?class . ?class rdfs:subClassOf+ testresults:Observation filter ( ?class != testresults:Observation ) . ?observation testresults:testcaseOrStep %s . OPTIONAL { ?observation testresults:timestamp ?ts } } order by ?ts", testcaseOrStep);
            }
            if (!observations.isEmpty()) {
                StringBuffer sb = new StringBuffer();
                for (Map<String, RDFNode> map : observations) {
                    Individual observation = map.get("observation").as(Individual.class);
                    switch (map.get("class").as(Individual.class).getLocalName()) {
                    case "ObservationOk":
                        status = Status.ok;
                        break;
                    case "ObservationNotOk":
                        status = Status.failed;
                        break;
                    case "ObservationNotTestable":
                        status = Status.notTestable;
                        break;
                    default:
                        continue;
                    }
                    RDFNode value = observation.getPropertyValue(testcaseModel.createProperty(NS_TESTRESULTS+"timestamp"));
                    if (value != null) {
                        String ts = String.format("%1$tY-%1$tm-%1$td", ((XSDDateTime)value.asLiteral().getValue()).asCalendar());
                        sb.append(ts+":");
                    }
                    String comment = observation.getComment(null);
                    if (comment != null) {
                        sb.append(observation.getComment(null)+" ->");
                    }
                    sb.append(status);
                    sb.append('\n');
                }
                log = sb.toString();
            }
        }
    }

    public boolean isAvailableInStep(Individual resourceOrService, Individual step)
    {
        if (step.getLocalName().endsWith("_restore")) {
            return true;
        }
        List<RDFNode> unavailables = query("select ?cfg { %s failover:hasConfiguration ?cfg . ?cfg failover:isUnavailable %s }", "cfg", step.asResource(), resourceOrService.asResource());
        final boolean available = unavailables.isEmpty();
        return available;
    }

    private void mergeCells(XSSFSheet testcaseSheet, XSSFCell cell1, XSSFCell cell2)
    {
        CellRangeAddress region = new CellRangeAddress(cell1.getRowIndex(), cell2.getRowIndex(), cell1.getColumnIndex(), cell2.getColumnIndex());
        testcaseSheet.addMergedRegion(region );
    }

    private int addHeader(XSSFSheet testcaseSheet, List<RDFNode> steps)
    {
        XSSFRow header = testcaseSheet.createRow(testcaseSheet.getLastRowNum()+1);
        XSSFRow header1 = testcaseSheet.createRow(testcaseSheet.getLastRowNum()+1);

        header.createCell(2).setCellValue("Step ->");;
        header1.createCell(2).setCellValue("Action(s) ->");;
        XSSFCell cellNormalize = header.createCell(3);
        cellNormalize.setCellValue("normal operations");
        XSSFCell cellNormalizeDescription = header1.createCell(3);
        cellNormalizeDescription.setCellValue("establish normal operations");
        cellNormalizeDescription.setCellStyle(wrapStyle);
        testcaseSheet.setColumnWidth(3, 30*256);

        for (int i = 0; i < steps.size(); ++i) {
            Individual step = steps.get(i).as(Individual.class);
            XSSFCellStyle headerStyle = step.getLocalName().endsWith("_disrupt") ? disruptHeaderStyle : restoreHeaderStyle;
            
            XSSFCell cell = header.createCell(i+4);
            cell.setCellStyle(headerStyle);
            cell.setCellValue(toString(step));
            XSSFCell cellDescription = header1.createCell(i+4);
            cellDescription.setCellStyle(headerStyle);
            cellDescription.setCellValue(step.getComment(null));
            testcaseSheet.setColumnWidth(4+i, 30*256);
        }
        return testcaseSheet.getLastRowNum()+1;
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
                Map<String, RDFNode> map = new HashMap<String, RDFNode>();
                for (Iterator<String> i = querySolution.varNames(); i.hasNext();) {
                    String varName = i.next();
                    map.put(varName, querySolution.get(varName));
                }
                results.add(map);
            }
            log.debug(query+" -> "+results);
            return results;
        } catch (Throwable e) {
            throw new IllegalArgumentException(query, e);
        }
    }

    private List<RDFNode> getTestcaseSteps(Resource testcase)
    {
        return query("select ?step where { %s failover:hasStep ?step } order by ?step", "step", testcase);
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
        
        testcaseModel = ModelFactory.createOntologyModel();
        testcaseModel.read(new File(testcaseOntologyFilename).toURI().toASCIIString());
        testcaseModel.loadImports();
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
