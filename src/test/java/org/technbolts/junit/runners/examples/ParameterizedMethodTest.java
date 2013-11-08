package org.technbolts.junit.runners.examples;

import org.technbolts.junit.runners.Runner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
@RunWith(Runner.class)
public class ParameterizedMethodTest {

    @Runner.DataProvider(name = "zog")
    public static Collection<Object[]> allZogs() {
        return Arrays.<Object[]>asList(new Object[]{"pim"}, new Object[]{"pam"}, new Object[]{"poum"});
    }

    @Test
    @Runner.Parameterized(dataProvider = "zog")
    public void testZog(String value) {
        System.out.println("ParameterizedMethodTest.testZog(" + value + ")");
    }

    @Test
    public void someOtherTest() {
        System.out.println("ParameterizedMethodTest.someOtherTest()");
    }

}
