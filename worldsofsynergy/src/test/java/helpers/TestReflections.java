package helpers;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

public class TestReflections
{

    @Test
    public void test()
    {
        final Reflections reflections = new Reflections(worlds.PackageTag.class.getPackage().getName(), new SubTypesScanner(false));
        final Set<String> types = reflections.getAllTypes();
        assertFalse(types.isEmpty());
        for (String className : types) {
            System.out.println(className);
        }
    }

}
