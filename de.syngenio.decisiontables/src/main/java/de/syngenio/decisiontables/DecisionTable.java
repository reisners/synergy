package de.syngenio.decisiontables;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.io.IOUtils;

import com.google.common.collect.Sets;

import dnl.utils.text.table.TextTable;

public class DecisionTable<D>
{
    private LinkedHashSet<Condition<D>> conditions = new LinkedHashSet<Condition<D>>();
    private SortedSet<Rule> rules = new TreeSet<Rule>();
    
    public LinkedHashSet<Condition<D>> getConditions()
    {
        return conditions;
    }

    public SortedSet<Rule> getRules()
    {
        return rules;
    }

    public DecisionTable<D> withCondition(Condition<D> condition) {
        conditions.add(condition);
        return this;
    }
    
    public DecisionTable<D> withRule(Rule rule) throws InconsistentRuleException {
        rules.add(rule);
        checkRule(rule);
        checkRules(false);
        return this;
    }
    
    private void checkRule(Rule rule) throws InconsistentRuleException
    {
        // check that all table conditions and no others occur
        if (!rule.getAlternatives().keySet().equals(conditions)) {
            throw new InconsistentRuleException(rule+" condition set differs from decision table\n"+toString());
        }
    }

    public void validate() throws InconsistentRuleException {
        checkRules(true);
    }
    
    @Override
    public String toString()
    {
        List<String> columnNames = new ArrayList<String>();
        columnNames.add("Conditions");
        for (Rule rule : rules) {
            columnNames.add(rule.toString());
        }
        LinkedHashSet<Output<D>> outputs = new LinkedHashSet<Output<D>>();
        for (Rule rule : rules) {
            outputs.addAll(rule.getOutputs());
        }
        List<String[]> rows = new ArrayList<String[]>();
        for (Condition<D> condition : conditions) {
            List<String> row = new ArrayList<String>();
            row.add(condition.toString());
            for (Rule rule : rules) {
                if (rule.getAlternatives().containsKey(condition)) {
                    Boolean alternative = rule.getAlternatives().get(condition);
                    if (alternative == null) {
                        row.add("*");
                    } else if (alternative) {
                        row.add("T");
                    } else {
                        row.add("F");
                    }
                } else {
                    row.add("!");
                }
            }
            rows.add(row.toArray(new String[0]));
        }
        rows.add(new String[] {"Outputs"});
        for (Output<D> output: outputs) {
            List<String> row = new ArrayList<String>();
            row.add(output.toString());
            for (Rule rule : rules) {
                row.add(rule.getOutputs().contains(output) ? "X" : "");
            }
            rows.add(row.toArray(new String[0]));
        }
        TextTable tt = new TextTable(columnNames.toArray(new String[0]), rows.toArray(new String[0][]));
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            tt.printTable(new PrintStream(baos), 0);
            return IOUtils.toString(new ByteArrayInputStream(baos.toByteArray()));
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    private void checkRules(boolean checkCompleteness) throws InconsistentRuleException
    {
        // guava implementation
        for (final Set<Condition<D>> subset : Sets.powerSet(conditions)) {
            Set<Rule> filtered = new HashSet<Rule>(rules);
            CollectionUtils.filter(filtered, new Predicate<Rule>() {
                @Override
                public boolean evaluate(Rule rule)
                {
                    return rule.matches(subset);
                }
            });
            switch (filtered.size()) {
            case 0:
                if (checkCompleteness) {
                    throw new InconsistentRuleException(String.format("no rules matches %s%n%s", subset, toString()));
                }
                break;
            case 1:
                continue;
            default:
                throw new InconsistentRuleException(String.format("multiple rules %s match %s%n%s",filtered.toString(), subset, toString()));
            }
        }
        
//        long lNumCombinations = BigInteger.ONE.shiftLeft(conditions.size()).longValue();
//        for (long lCombination = 0; lCombination < lNumCombinations; ++lCombination) {
//            final long bits = lCombination;
//            Set<Rule> filtered = new HashSet<Rule>(rules);
//            CollectionUtils.filter(filtered, new Predicate<Rule>() {
//                @Override
//                public boolean evaluate(Rule rule)
//                {
//                    return rule.matches(bits);
//                }
//            });
//            switch (filtered.size()) {
//            case 0:
//                if (checkCompleteness) {
//                    throw new InconsistentRuleException(String.format("no rules matches %s%n%s", bitsToConditions(bits), toString()));
//                }
//                break;
//            case 1:
//                continue;
//            default:
//                throw new InconsistentRuleException(String.format("multiple rules %s match %s%n%s",filtered.toString(), bitsToConditions(bits), toString()));
//            }
//        }
    }

    private List<Condition<D>> bitsToConditions(long bits) {
        List<Condition<D>> list = new ArrayList<Condition<D>>();
        BigInteger bi = BigInteger.valueOf(bits);
        int i = 0;
        for (Condition<D> condition : conditions) {
            if (bi.testBit(i++)) {
                list.add(condition);
            }
        }
        return list;
    }
    
    public Collection<Output<D>> process(D instance) {
        Rule matchingRule = findMatchingRuleFor(instance);
        return matchingRule.getOutputs();
    }

    private Rule findMatchingRuleFor(D instance)
    {
        List<Rule> matchingRules = new ArrayList<Rule>();
        for (Rule rule : rules) {
            if (rule.matches(instance)) {
                matchingRules.add(rule);
            }
        }
        switch (matchingRules.size()) {
        case 0:
            throw new InconsistentRuleSetError("no rule matches instance "+instance+"\n"+toString());
        case 1:
            return matchingRules.get(0);
        default:
            throw new InconsistentRuleSetError("multiple rules "+matchingRules+" match instance "+instance+"\n"+toString());
        }
    }
    
    public class Rule implements Comparable<Rule>
    {
        private Map<Condition<D>, Boolean> alternatives = new HashMap<Condition<D>, Boolean>();
        private Set<Output<D>> outputs = new HashSet<Output<D>>();
        private String name;
        
        public Rule withAlternative(Condition<D> condition, Boolean alternative) {
            alternatives.put(condition, alternative);
            return this;
        }
        
        public boolean matches(Set<Condition<D>> actuallyTrueConditions)
        {
            for (Entry<Condition<D>, Boolean> alternativeEntry : alternatives.entrySet()) {
                Boolean alternative = alternativeEntry.getValue();
                boolean actual = actuallyTrueConditions.contains(alternativeEntry.getKey());
                if (alternative != null && alternative != actual) {
                    return false;
                }
            }
            return true;
        }

        public Rule withOutput(Output<D> output) {
            outputs.add(output);
            return this;
        }
        
        public boolean matches(D instance) {
            BigInteger bi = BigInteger.ZERO;
            int i = 0;
            for (Condition<D> condition : conditions) {
                if (condition.test(instance)) {
                    bi = bi.setBit(i);
                }
                ++i;
            }
            return matches(bi.longValue());
        }

        public boolean matches(long bits)
        {
            BigInteger bi = BigInteger.valueOf(bits);
            int i = 0;
            for (Boolean alternative : getAlternativesInDecisionTableOrder()) {
                boolean actual = bi.testBit(i++);
                if (alternative != null && alternative != actual) {
                    return false;
                }
            }
            return true;
        }
        
        /**
         * @return the alternatives
         */
        public Map<Condition<D>, Boolean> getAlternatives()
        {
            return alternatives;
        }
        /**
         * @return the outputs
         */
        public Set<Output<D>> getOutputs()
        {
            return outputs;
        }

        @Override
        public int compareTo(Rule o)
        {
            List<Boolean> alternativesInDTOrder = getAlternativesInDecisionTableOrder();
            List<Boolean> otherAlternativesInDTOrder = o.getAlternativesInDecisionTableOrder();
            for (int i = 0; i < alternativesInDTOrder.size(); ++i) {
                Boolean alternative = alternativesInDTOrder.get(i);
                Boolean otherAlternative = otherAlternativesInDTOrder.get(i);
                if (alternative == null && otherAlternative == null) {
                    continue;
                }
                if (alternative != null && otherAlternative == null) {
                    return -1;
                }
                if (alternative == null && otherAlternative != null) {
                    return 1;
                }
                int comparison = alternative.compareTo(otherAlternative);
                if (comparison != 0) {
                    return -comparison;
                }
            }
            return 0;
        }

        private List<Boolean> getAlternativesInDecisionTableOrder()
        {
            List<Boolean> list = new ArrayList<Boolean>();
            for (Condition<D> condition : getConditions()) {
                list.add(alternatives.get(condition));
            }
            return list;
        }

        /**
         * @see java.lang.Object#toString()
         * @return
         */
        @Override
        public String toString()
        {
            return name != null ? name : "R#"+(getRules().headSet(this).size()+1);
        }

        public Rule withName(String name)
        {
            this.name = name;
            return this;
        }
    }

    public Rule createRule()
    {
        return new Rule();
    }
}
