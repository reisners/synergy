package de.syngenio.vaadin.synergy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.thirdparty.guava.common.collect.Sets;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;

import de.syngenio.vaadin.synergy.builder.SynergyBuilder;

public class TestSynergyBuilder
{
    Logger log = LoggerFactory.getLogger(TestSynergyBuilder.class);
    
    HierarchicalContainer hc;

    HierarchicalContainer navigationHierarchy;

    @Before
    public void setup() {
        hc = SynergyBuilder.createHierarchicalContainer();
        hc.addItem("|Root");
        hc.addItem("|Root|Child1");
        hc.setParent("|Root|Child1", "|Root");
        hc.addItem("|Root|Child2");
        hc.setParent("|Root|Child2", "|Root");
        
        navigationHierarchy = new SynergyBuilder() {{
            addItem(
                    button("|Tools").asGroup().withChildren( 
                            button("|Tools|Specification").asGroup().withChildren(
                                    button("|Tools|Specification|MappingCases")),
                                    button("|Tools|Collaboration").asGroup().withChildren(
                                            button("|Tools|Collaboration|Chat"))));
            addItem(button("|Administration").asGroup().withChildren(
                    button("|Administration|Benutzer").asGroup()));
        }}.build();
        
    }
    
    @Test
    public void testBasicFiltering()
    {
        hc.addContainerFilter(new Filter() {

            @Override
            public boolean passesFilter(Object itemId, Item item) throws UnsupportedOperationException
            {
                return !"|Root|Child1".equals(itemId);
            }

            @Override
            public boolean appliesToProperty(Object propertyId)
            {
                return true;
            }
            
        });
        
        Set<String> actualIds = new HashSet<String>((List<String>)hc.getItemIds());
        assertEquals(Sets.newHashSet("|Root", "|Root|Child2"), actualIds);
    }

    @Test
    public void testBuilderCaptionDefaulting() {
        assertEquals("MappingCases", navigationHierarchy.getItem("|Tools|Specification|MappingCases").getItemProperty(SynergyBuilder.PROPERTY_ITEM_CAPTION).getValue());
    }
    
    @Test
    public void testParentNoTargetNavigationState() {
        assertNull(navigationHierarchy.getItem("|Tools|Specification").getItemProperty(SynergyBuilder.PROPERTY_TARGET_NAVIGATION_STATE).getValue());
    }
    
    @Test
    public void testAdvancedFiltering()
    {
        final Set<String> allowedIds = Sets.newHashSet("|Tools|Specification|MappingCases");
        
        navigationHierarchy.addContainerFilter(new Filter() {

            @Override
            public boolean passesFilter(Object itemId, Item item) throws UnsupportedOperationException
            {
                final boolean contains = allowedIds.contains(itemId);
                final boolean hasChildren = !SynergyBuilder.getChildIdsOf(navigationHierarchy, (String) itemId).isEmpty();
                final boolean visibleIfEmpty = !((Boolean)item.getItemProperty(SynergyBuilder.PROPERTY_ITEM_HIDDEN_IF_EMPTY).getValue());
                log.debug("itemId="+itemId+" --> contains="+contains+", hasChildren="+hasChildren+", visibleIfEmpty="+visibleIfEmpty);
                return contains
                        || hasChildren
                        || visibleIfEmpty;
            }

            @Override
            public boolean appliesToProperty(Object propertyId)
            {
                return true;
            }
            
        });
        Set<String> actualIds = new HashSet<String>((List<String>)navigationHierarchy.getItemIds());
        assertEquals(Sets.newHashSet("|Tools", "|Tools|Specification", "|Tools|Specification|MappingCases", "|Tools|Collaboration", "|Tools|Collaboration|Chat"), actualIds);
    }

    @Test
    public void testParent() {
        assertEquals("|Administration", SynergyBuilder.getParentId(navigationHierarchy, "|Administration|Benutzer"));
    }

    @Test
    public void testAncestor() {
        assertFalse(SynergyBuilder.isAncestorOf(navigationHierarchy, "|Tools", "|Administration|Benutzer"));
    }
}
