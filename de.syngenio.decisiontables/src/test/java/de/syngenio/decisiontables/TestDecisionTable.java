package de.syngenio.decisiontables;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestDecisionTable
{
    private final static Logger log = LoggerFactory.getLogger(TestDecisionTable.class);
    
    Condition<Printer> conditionDoesNotPrint = new ContainsCondition("does not print");
    Condition<Printer> conditionRedLightFlashing = new ContainsCondition("red light flashing");
    Condition<Printer> conditionUnrecognized = new ContainsCondition("unrecognized");

    Output<Printer> outputCheckPowerCable = new MessageOutput<Printer>("Check the power cable");                   
    Output<Printer> outputCheckDataCable = new MessageOutput<Printer>("Check the printer-computer cable");                   
    Output<Printer> outputEnsurePrinterSoftwareInstalled = new MessageOutput<Printer>("Ensure printer software is installed");
    Output<Printer> outputCheckReplaceInk = new MessageOutput<Printer>("Check/replace ink");
    Output<Printer> outputCheckForPaperJam = new MessageOutput<Printer>("Check for paper jam");

    DecisionTable<Printer> consistentDecisionTable;

    DecisionTable<Printer> inconsistentDecisionTable;

    private static class Printer {
        private String state;
        
        Printer(String state) {
            this.state = state;
        }

        public String getState()
        {
            return state;
        }
    }

    private static class ContainsCondition implements Condition<Printer> {
        private String text;
        ContainsCondition(String text) {
            this.text = text;
        }
        @Override
        public boolean test(Printer instance)
        {
            return instance.getState().contains(text);
        }
        @Override
        public int hashCode()
        {
            return HashCodeBuilder.reflectionHashCode(this, false);
        }
        @Override
        public boolean equals(Object obj)
        {
            return EqualsBuilder.reflectionEquals(this, obj, false);
        }
        @Override
        public String toString()
        {
            return "contains \""+text+"\"";
        }
    }
    
    private static class MessageOutput<D> implements Output<D> {
        private String msg;

        MessageOutput(String msg) {
            this.msg = msg;
        }

        public String getMsg()
        {
            return msg;
        }
        @Override
        public int hashCode()
        {
            return HashCodeBuilder.reflectionHashCode(this, false);
        }
        @Override
        public boolean equals(Object obj)
        {
            return EqualsBuilder.reflectionEquals(this, obj, false);
        }
        @Override
        public String toString()
        {
            return "\""+msg+"\"";
        }
    }
    
    private static class UfoAttack {
        enum Color { red, green, blue };
        enum Shape { rocket, flyingSaucer, spiky };
        
        private Color alienColor;
        private Shape shipShape;

        private UfoAttack(Color alienColor, Shape shipShape)
        {
            this.alienColor = alienColor;
            this.shipShape = shipShape;
        }
    }
    
    DecisionTable<UfoAttack> condensedDecisionTable;
    
    @Before
    public void setupConsistentDecisionTable() throws InconsistentRuleException {
        
        consistentDecisionTable = new DecisionTable<Printer>();
        consistentDecisionTable.withCondition(conditionDoesNotPrint).withCondition(conditionRedLightFlashing).withCondition(conditionUnrecognized)
                .withRule(consistentDecisionTable.createRule().withName("A")
                        .withAlternative(conditionDoesNotPrint, true)
                        .withAlternative(conditionRedLightFlashing, true)
                        .withAlternative(conditionUnrecognized, true)
                        .withOutput(outputCheckDataCable)
                        .withOutput(outputEnsurePrinterSoftwareInstalled)
                        .withOutput(outputCheckReplaceInk))
                .withRule(consistentDecisionTable.createRule().withName("B")
                        .withAlternative(conditionDoesNotPrint, true)
                        .withAlternative(conditionRedLightFlashing, true)
                        .withAlternative(conditionUnrecognized, false)
                        .withOutput(outputCheckReplaceInk)
                        .withOutput(outputCheckForPaperJam))
                .withRule(consistentDecisionTable.createRule().withName("C")
                        .withAlternative(conditionDoesNotPrint, true)
                        .withAlternative(conditionRedLightFlashing, false)
                        .withAlternative(conditionUnrecognized, true)
                        .withOutput(outputCheckPowerCable)
                        .withOutput(outputCheckDataCable)
                        .withOutput(outputEnsurePrinterSoftwareInstalled))
                .withRule(consistentDecisionTable.createRule().withName("D")
                        .withAlternative(conditionDoesNotPrint, true)
                        .withAlternative(conditionRedLightFlashing, false)
                        .withAlternative(conditionUnrecognized, false)
                        .withOutput(outputCheckForPaperJam))
                .withRule(consistentDecisionTable.createRule().withName("E")
                        .withAlternative(conditionDoesNotPrint, false)
                        .withAlternative(conditionRedLightFlashing, true)
                        .withAlternative(conditionUnrecognized, true)
                        .withOutput(outputEnsurePrinterSoftwareInstalled)
                        .withOutput(outputCheckReplaceInk))
                .withRule(consistentDecisionTable.createRule().withName("F")
                        .withAlternative(conditionDoesNotPrint, false)
                        .withAlternative(conditionRedLightFlashing, true)
                        .withAlternative(conditionUnrecognized, false)
                        .withOutput(outputCheckReplaceInk))
                .withRule(consistentDecisionTable.createRule().withName("G")
                        .withAlternative(conditionDoesNotPrint, false)
                        .withAlternative(conditionRedLightFlashing, false)
                        .withAlternative(conditionUnrecognized, true)
                        .withOutput(outputEnsurePrinterSoftwareInstalled))
                .withRule(consistentDecisionTable.createRule().withName("H")
                        .withAlternative(conditionDoesNotPrint, false)
                        .withAlternative(conditionRedLightFlashing, false)
                        .withAlternative(conditionUnrecognized, false))
                        ;
        consistentDecisionTable.validate();
    }
    
    @Before
    public void setupInconsistentDecisionTable() throws InconsistentRuleException {
        
        inconsistentDecisionTable = new DecisionTable<Printer>();
        inconsistentDecisionTable.withCondition(conditionDoesNotPrint).withCondition(conditionRedLightFlashing).withCondition(conditionUnrecognized)
                .withRule(inconsistentDecisionTable.createRule()
                        .withAlternative(conditionDoesNotPrint, true)
                        .withAlternative(conditionRedLightFlashing, true)
                        .withAlternative(conditionUnrecognized, true)
                        .withOutput(outputCheckDataCable)
                        .withOutput(outputEnsurePrinterSoftwareInstalled)
                        .withOutput(outputCheckReplaceInk))
                .withRule(inconsistentDecisionTable.createRule()
                        .withAlternative(conditionDoesNotPrint, true)
                        .withAlternative(conditionRedLightFlashing, true)
                        .withAlternative(conditionUnrecognized, false)
                        .withOutput(outputCheckReplaceInk)
                        .withOutput(outputCheckForPaperJam))
                .withRule(inconsistentDecisionTable.createRule()
                        .withAlternative(conditionDoesNotPrint, true)
                        .withAlternative(conditionRedLightFlashing, false)
                        .withAlternative(conditionUnrecognized, true)
                        .withOutput(outputCheckPowerCable)
                        .withOutput(outputCheckDataCable)
                        .withOutput(outputEnsurePrinterSoftwareInstalled))
                .withRule(inconsistentDecisionTable.createRule()
                        .withAlternative(conditionDoesNotPrint, true)
                        .withAlternative(conditionRedLightFlashing, false)
                        .withAlternative(conditionUnrecognized, false)
                        .withOutput(outputCheckForPaperJam))
                .withRule(inconsistentDecisionTable.createRule()
                        .withAlternative(conditionDoesNotPrint, false)
                        .withAlternative(conditionRedLightFlashing, true)
                        .withAlternative(conditionUnrecognized, true)
                        .withOutput(outputEnsurePrinterSoftwareInstalled)
                        .withOutput(outputCheckReplaceInk))
                .withRule(inconsistentDecisionTable.createRule()
                        .withAlternative(conditionDoesNotPrint, false)
                        .withAlternative(conditionRedLightFlashing, true)
                        .withAlternative(conditionUnrecognized, false)
                        .withOutput(outputCheckReplaceInk))
                .withRule(inconsistentDecisionTable.createRule()
                        .withAlternative(conditionDoesNotPrint, false)
                        .withAlternative(conditionRedLightFlashing, false)
                        .withAlternative(conditionUnrecognized, true)
                        .withOutput(outputEnsurePrinterSoftwareInstalled))
                // case false-false-false removed to create inconsistency
                        ;
    }

    private static class ColorCondition implements Condition<UfoAttack> {
        private de.syngenio.decisiontables.TestDecisionTable.UfoAttack.Color color;
        ColorCondition(UfoAttack.Color color) {
            this.color = color;
        }
        @Override
        public boolean test(UfoAttack instance)
        {
            return instance.alienColor.equals(color);
        }
        @Override
        public String toString()
        {
            return "=="+color.name();
        }
    }
    
    private Condition<UfoAttack> conditionAlienColorRed = new ColorCondition(UfoAttack.Color.red);
    private Condition<UfoAttack> conditionAlienColorGreen = new ColorCondition(UfoAttack.Color.green);
    private Condition<UfoAttack> conditionAlienColorBlue = new ColorCondition(UfoAttack.Color.blue);

    private static class ShapeCondition implements Condition<UfoAttack> {
        private de.syngenio.decisiontables.TestDecisionTable.UfoAttack.Shape shape;
        ShapeCondition(UfoAttack.Shape shape) {
            this.shape = shape;
        }
        @Override
        public boolean test(UfoAttack instance)
        {
            return instance.shipShape.equals(shape);
        }
        @Override
        public String toString()
        {
            return "=="+shape.toString();
        }
    }

    private Condition<UfoAttack> conditionRocketShaped = new ShapeCondition(UfoAttack.Shape.rocket);
    private Condition<UfoAttack> conditionSaucerShaped = new ShapeCondition(UfoAttack.Shape.flyingSaucer);
    private Condition<UfoAttack> conditionSpikyShaped = new ShapeCondition(UfoAttack.Shape.spiky);
    
    @Before
    public void setupCondensedDecisionTable() throws InconsistentRuleException {
        condensedDecisionTable = new DecisionTable<UfoAttack>();
        condensedDecisionTable
        .withCondition(conditionAlienColorRed)
        .withCondition(conditionAlienColorGreen)
        .withCondition(conditionAlienColorBlue)
        .withCondition(conditionRocketShaped)
        .withCondition(conditionSaucerShaped)
        .withCondition(conditionSpikyShaped);
        condensedDecisionTable
        .withRule(condensedDecisionTable.createRule().withName("A1")
                .withAlternative(conditionAlienColorRed, false)
                .withAlternative(conditionAlienColorGreen, true)
                .withAlternative(conditionAlienColorBlue, false)
                .withAlternative(conditionRocketShaped, true)
                .withAlternative(conditionSaucerShaped, false)
                .withAlternative(conditionSpikyShaped, false)
                .withOutput(new MessageOutput<UfoAttack>("ignore")))
        .withRule(condensedDecisionTable.createRule().withName("A2")
                .withAlternative(conditionAlienColorRed, false)
                .withAlternative(conditionAlienColorGreen, true)
                .withAlternative(conditionAlienColorBlue, false)
                .withAlternative(conditionRocketShaped, false)
                .withAlternative(conditionSaucerShaped, true)
                .withAlternative(conditionSpikyShaped, false)
                .withOutput(new MessageOutput<UfoAttack>("ignore")))
        .withRule(condensedDecisionTable.createRule().withName("A3")
                .withAlternative(conditionAlienColorRed, false)
                .withAlternative(conditionAlienColorGreen, true)
                .withAlternative(conditionAlienColorBlue, false)
                .withAlternative(conditionRocketShaped, false)
                .withAlternative(conditionSaucerShaped, false)
                .withAlternative(conditionSpikyShaped, true)
                .withOutput(new MessageOutput<UfoAttack>("ignore")))
        .withRule(condensedDecisionTable.createRule().withName("B1")
                .withAlternative(conditionAlienColorRed, true)
                .withAlternative(conditionAlienColorGreen, false)
                .withAlternative(conditionAlienColorBlue, false)
                .withAlternative(conditionRocketShaped, true)
                .withAlternative(conditionSaucerShaped, false)
                .withAlternative(conditionSpikyShaped, false)
                .withOutput(new MessageOutput<UfoAttack>("flee")))
        .withRule(condensedDecisionTable.createRule().withName("B2")
                .withAlternative(conditionAlienColorRed, true)
                .withAlternative(conditionAlienColorGreen, false)
                .withAlternative(conditionAlienColorBlue, false)
                .withAlternative(conditionRocketShaped, false)
                .withAlternative(conditionSaucerShaped, false)
                .withAlternative(conditionSpikyShaped, true)
                .withOutput(new MessageOutput<UfoAttack>("flee")))
        .withRule(condensedDecisionTable.createRule().withName("C1")
                .withAlternative(conditionAlienColorRed, false)
                .withAlternative(conditionAlienColorGreen, false)
                .withAlternative(conditionAlienColorBlue, true)
                .withAlternative(conditionRocketShaped, true)
                .withAlternative(conditionSaucerShaped, false)
                .withAlternative(conditionSpikyShaped, false)
                .withOutput(new MessageOutput<UfoAttack>("flee")))
        .withRule(condensedDecisionTable.createRule().withName("C2")
                .withAlternative(conditionAlienColorRed, false)
                .withAlternative(conditionAlienColorGreen, false)
                .withAlternative(conditionAlienColorBlue, true)
                .withAlternative(conditionRocketShaped, false)
                .withAlternative(conditionSaucerShaped, false)
                .withAlternative(conditionSpikyShaped, true)
                .withOutput(new MessageOutput<UfoAttack>("flee")))
        .withRule(condensedDecisionTable.createRule().withName("D")
                .withAlternative(conditionAlienColorRed, true)
                .withAlternative(conditionAlienColorGreen, false)
                .withAlternative(conditionAlienColorBlue, false)
                .withAlternative(conditionRocketShaped, false)
                .withAlternative(conditionSaucerShaped, true)
                .withAlternative(conditionSpikyShaped, false)
                .withOutput(new MessageOutput<UfoAttack>("fight back")))
        .withRule(condensedDecisionTable.createRule().withName("E")
                .withAlternative(conditionAlienColorRed, false)
                .withAlternative(conditionAlienColorGreen, false)
                .withAlternative(conditionAlienColorBlue, true)
                .withAlternative(conditionRocketShaped, false)
                .withAlternative(conditionSaucerShaped, true)
                .withAlternative(conditionSpikyShaped, false)
                .withOutput(new MessageOutput<UfoAttack>("fight back")))
        // bad inputs
        .withRule(condensedDecisionTable.createRule().withName("F")
                .withAlternative(conditionAlienColorRed, false)
                .withAlternative(conditionAlienColorGreen, true)
                .withAlternative(conditionAlienColorBlue, true)
                .withAlternative(conditionRocketShaped, null)
                .withAlternative(conditionSaucerShaped, null)
                .withAlternative(conditionSpikyShaped, null)
                .withOutput(new MessageOutput<UfoAttack>("bad input")))
        .withRule(condensedDecisionTable.createRule().withName("G")
                .withAlternative(conditionAlienColorRed, true)
                .withAlternative(conditionAlienColorGreen, false)
                .withAlternative(conditionAlienColorBlue, true)
                .withAlternative(conditionRocketShaped, null)
                .withAlternative(conditionSaucerShaped, null)
                .withAlternative(conditionSpikyShaped, null)
                .withOutput(new MessageOutput<UfoAttack>("bad input")))
        .withRule(condensedDecisionTable.createRule().withName("H")
                .withAlternative(conditionAlienColorRed, true)
                .withAlternative(conditionAlienColorGreen, true)
                .withAlternative(conditionAlienColorBlue, false)
                .withAlternative(conditionRocketShaped, null)
                .withAlternative(conditionSaucerShaped, null)
                .withAlternative(conditionSpikyShaped, null)
                .withOutput(new MessageOutput<UfoAttack>("bad input")))
        .withRule(condensedDecisionTable.createRule().withName("I1")
                .withAlternative(conditionAlienColorRed, true)
                .withAlternative(conditionAlienColorGreen, false)
                .withAlternative(conditionAlienColorBlue, false)
                .withAlternative(conditionRocketShaped, false)
                .withAlternative(conditionSaucerShaped, true)
                .withAlternative(conditionSpikyShaped, true)
                .withOutput(new MessageOutput<UfoAttack>("bad input")))
        .withRule(condensedDecisionTable.createRule().withName("I2")
                .withAlternative(conditionAlienColorRed, false)
                .withAlternative(conditionAlienColorGreen, true)
                .withAlternative(conditionAlienColorBlue, false)
                .withAlternative(conditionRocketShaped, false)
                .withAlternative(conditionSaucerShaped, true)
                .withAlternative(conditionSpikyShaped, true)
                .withOutput(new MessageOutput<UfoAttack>("bad input")))
        .withRule(condensedDecisionTable.createRule().withName("I3")
                .withAlternative(conditionAlienColorRed, false)
                .withAlternative(conditionAlienColorGreen, false)
                .withAlternative(conditionAlienColorBlue, true)
                .withAlternative(conditionRocketShaped, false)
                .withAlternative(conditionSaucerShaped, true)
                .withAlternative(conditionSpikyShaped, true)
                .withOutput(new MessageOutput<UfoAttack>("bad input")))
        .withRule(condensedDecisionTable.createRule().withName("J1")
                .withAlternative(conditionAlienColorRed, true)
                .withAlternative(conditionAlienColorGreen, false)
                .withAlternative(conditionAlienColorBlue, false)
                .withAlternative(conditionRocketShaped, true)
                .withAlternative(conditionSaucerShaped, false)
                .withAlternative(conditionSpikyShaped, true)
                .withOutput(new MessageOutput<UfoAttack>("bad input")))
        .withRule(condensedDecisionTable.createRule().withName("J2")
                .withAlternative(conditionAlienColorRed, false)
                .withAlternative(conditionAlienColorGreen, true)
                .withAlternative(conditionAlienColorBlue, false)
                .withAlternative(conditionRocketShaped, true)
                .withAlternative(conditionSaucerShaped, false)
                .withAlternative(conditionSpikyShaped, true)
                .withOutput(new MessageOutput<UfoAttack>("bad input")))
        .withRule(condensedDecisionTable.createRule().withName("J3")
                .withAlternative(conditionAlienColorRed, false)
                .withAlternative(conditionAlienColorGreen, false)
                .withAlternative(conditionAlienColorBlue, true)
                .withAlternative(conditionRocketShaped, true)
                .withAlternative(conditionSaucerShaped, false)
                .withAlternative(conditionSpikyShaped, true)
                .withOutput(new MessageOutput<UfoAttack>("bad input")))
        .withRule(condensedDecisionTable.createRule().withName("K1")
                .withAlternative(conditionAlienColorRed, true)
                .withAlternative(conditionAlienColorGreen, false)
                .withAlternative(conditionAlienColorBlue, false)
                .withAlternative(conditionRocketShaped, true)
                .withAlternative(conditionSaucerShaped, true)
                .withAlternative(conditionSpikyShaped, false)
                .withOutput(new MessageOutput<UfoAttack>("bad input")))
        .withRule(condensedDecisionTable.createRule().withName("K2")
                .withAlternative(conditionAlienColorRed, false)
                .withAlternative(conditionAlienColorGreen, true)
                .withAlternative(conditionAlienColorBlue, false)
                .withAlternative(conditionRocketShaped, true)
                .withAlternative(conditionSaucerShaped, true)
                .withAlternative(conditionSpikyShaped, false)
                .withOutput(new MessageOutput<UfoAttack>("bad input")))
        .withRule(condensedDecisionTable.createRule().withName("K3")
                .withAlternative(conditionAlienColorRed, false)
                .withAlternative(conditionAlienColorGreen, false)
                .withAlternative(conditionAlienColorBlue, true)
                .withAlternative(conditionRocketShaped, true)
                .withAlternative(conditionSaucerShaped, true)
                .withAlternative(conditionSpikyShaped, false)
                .withOutput(new MessageOutput<UfoAttack>("bad input")))
//        .withRule(condensedDecisionTable.createRule().withName("L")
//                .withAlternative(conditionAlienColorRed, false)
//                .withAlternative(conditionAlienColorGreen, false)
//                .withAlternative(conditionAlienColorBlue, false)
//                .withAlternative(conditionRocketShaped, false)
//                .withAlternative(conditionSaucerShaped, false)
//                .withAlternative(conditionSpikyShaped, false)
//                .withOutput(new MessageOutput<UfoAttack>("bad input")))
        .withRule(condensedDecisionTable.createRule().withName("M1")
                .withAlternative(conditionAlienColorRed, true)
                .withAlternative(conditionAlienColorGreen, false)
                .withAlternative(conditionAlienColorBlue, false)
                .withAlternative(conditionRocketShaped, false)
                .withAlternative(conditionSaucerShaped, false)
                .withAlternative(conditionSpikyShaped, false)
                .withOutput(new MessageOutput<UfoAttack>("bad input")))
        .withRule(condensedDecisionTable.createRule().withName("M2")
                .withAlternative(conditionAlienColorRed, false)
                .withAlternative(conditionAlienColorGreen, true)
                .withAlternative(conditionAlienColorBlue, false)
                .withAlternative(conditionRocketShaped, false)
                .withAlternative(conditionSaucerShaped, false)
                .withAlternative(conditionSpikyShaped, false)
                .withOutput(new MessageOutput<UfoAttack>("bad input")))
        .withRule(condensedDecisionTable.createRule().withName("M3")
                .withAlternative(conditionAlienColorRed, false)
                .withAlternative(conditionAlienColorGreen, false)
                .withAlternative(conditionAlienColorBlue, true)
                .withAlternative(conditionRocketShaped, false)
                .withAlternative(conditionSaucerShaped, false)
                .withAlternative(conditionSpikyShaped, false)
                .withOutput(new MessageOutput<UfoAttack>("bad input")))
        .withRule(condensedDecisionTable.createRule().withName("N")
                .withAlternative(conditionAlienColorRed, false)
                .withAlternative(conditionAlienColorGreen, false)
                .withAlternative(conditionAlienColorBlue, false)
                .withAlternative(conditionRocketShaped, null)
                .withAlternative(conditionSaucerShaped, null)
                .withAlternative(conditionSpikyShaped, null)
                .withOutput(new MessageOutput<UfoAttack>("bad input")))
//        .withRule(condensedDecisionTable.createRule().withName("N2")
//                .withAlternative(conditionAlienColorRed, false)
//                .withAlternative(conditionAlienColorGreen, false)
//                .withAlternative(conditionAlienColorBlue, false)
//                .withAlternative(conditionRocketShaped, false)
//                .withAlternative(conditionSaucerShaped, true)
//                .withAlternative(conditionSpikyShaped, false)
//                .withOutput(new MessageOutput<UfoAttack>("bad input")))
//        .withRule(condensedDecisionTable.createRule().withName("N3")
//                .withAlternative(conditionAlienColorRed, false)
//                .withAlternative(conditionAlienColorGreen, false)
//                .withAlternative(conditionAlienColorBlue, false)
//                .withAlternative(conditionRocketShaped, false)
//                .withAlternative(conditionSaucerShaped, false)
//                .withAlternative(conditionSpikyShaped, true)
//                .withOutput(new MessageOutput<UfoAttack>("bad input")))
        .withRule(condensedDecisionTable.createRule().withName("O")
                .withAlternative(conditionAlienColorRed, true)
                .withAlternative(conditionAlienColorGreen, true)
                .withAlternative(conditionAlienColorBlue, true)
                .withAlternative(conditionRocketShaped, null)
                .withAlternative(conditionSaucerShaped, null)
                .withAlternative(conditionSpikyShaped, null)
                .withOutput(new MessageOutput<UfoAttack>("bad input")))
        .withRule(condensedDecisionTable.createRule().withName("P1")
                .withAlternative(conditionAlienColorRed, true)
                .withAlternative(conditionAlienColorGreen, false)
                .withAlternative(conditionAlienColorBlue, false)
                .withAlternative(conditionRocketShaped, true)
                .withAlternative(conditionSaucerShaped, true)
                .withAlternative(conditionSpikyShaped, true)
                .withOutput(new MessageOutput<UfoAttack>("bad input")))
        .withRule(condensedDecisionTable.createRule().withName("P2")
                .withAlternative(conditionAlienColorRed, false)
                .withAlternative(conditionAlienColorGreen, true)
                .withAlternative(conditionAlienColorBlue, false)
                .withAlternative(conditionRocketShaped, true)
                .withAlternative(conditionSaucerShaped, true)
                .withAlternative(conditionSpikyShaped, true)
                .withOutput(new MessageOutput<UfoAttack>("bad input")))
        .withRule(condensedDecisionTable.createRule().withName("P3")
                .withAlternative(conditionAlienColorRed, false)
                .withAlternative(conditionAlienColorGreen, false)
                .withAlternative(conditionAlienColorBlue, true)
                .withAlternative(conditionRocketShaped, true)
                .withAlternative(conditionSaucerShaped, true)
                .withAlternative(conditionSpikyShaped, true)
                .withOutput(new MessageOutput<UfoAttack>("bad input")))
                ;
        assertEquals(29, condensedDecisionTable.getRules().size());
    }
    
    @Test(expected=InconsistentRuleException.class)
    public void verifyExceptionOnInconsistentDecisionTable() throws InconsistentRuleException
    {
        inconsistentDecisionTable.validate();
    }
    
    @Test
    public void verifyConsistentDecisionTableWorks() throws InconsistentRuleException
    {
        assertEquals(asSet(outputCheckDataCable, outputEnsurePrinterSoftwareInstalled, outputCheckReplaceInk), consistentDecisionTable.process(new Printer("does not print;red light flashing;unrecognized")));
        assertEquals(asSet(outputCheckReplaceInk, outputCheckForPaperJam), consistentDecisionTable.process(new Printer("does not print;red light flashing")));
        assertEquals(asSet(outputCheckPowerCable, outputCheckDataCable, outputEnsurePrinterSoftwareInstalled), consistentDecisionTable.process(new Printer("does not print;unrecognized")));
        assertEquals(asSet(outputCheckForPaperJam), consistentDecisionTable.process(new Printer("does not print")));
        assertEquals(asSet(outputEnsurePrinterSoftwareInstalled, outputCheckReplaceInk), consistentDecisionTable.process(new Printer("red light flashing;unrecognized")));
        assertEquals(asSet(outputCheckReplaceInk), consistentDecisionTable.process(new Printer("red light flashing")));
        assertEquals(asSet(outputEnsurePrinterSoftwareInstalled), consistentDecisionTable.process(new Printer("unrecognized")));
    }
    
    @Test
    public void verifyCondensedDecisionTableWorks() throws InconsistentRuleException
    {
        condensedDecisionTable.validate();
        log.info("\n"+condensedDecisionTable.toString());
        assertEquals(asSet(new MessageOutput<UfoAttack>("ignore")), condensedDecisionTable.process(new UfoAttack(UfoAttack.Color.green, UfoAttack.Shape.spiky)));
        assertEquals(asSet(new MessageOutput<UfoAttack>("fight back")), condensedDecisionTable.process(new UfoAttack(UfoAttack.Color.red, UfoAttack.Shape.flyingSaucer)));
        assertEquals(asSet(new MessageOutput<UfoAttack>("flee")), condensedDecisionTable.process(new UfoAttack(UfoAttack.Color.blue, UfoAttack.Shape.spiky)));
    }

    @SafeVarargs
    private static <T> Set<T> asSet(T... elements) {
        return new HashSet<T>(Arrays.asList(elements));
    }
}
