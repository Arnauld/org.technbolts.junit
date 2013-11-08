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
@Runner.Parameterized(dataProvider = "databases")
public class ParameterizedConstructorAndMethodTest {

    @Runner.DataProvider(name = "databases")
    public static Collection<Object[]> allDatabases() {
        return Arrays.asList(o("db2"), o("mysql"), o("oracle"), o("postgres"));
    }

    private final String database;

    public ParameterizedConstructorAndMethodTest(String database) {
        this.database = database;
    }

    @Runner.DataProvider(name = "scripts")
    public static Collection<Object[]> allZogs() {
        return Arrays.asList(o("script1"), o("script2"));
    }

    @Test
    @Runner.Parameterized(dataProvider = "scripts")
    public void script(String script) {
        System.out.println("ParameterizedConstructorAndMethodTest.script(" + database + ":" + script + ")");
    }

    @Test
    public void someOtherTest() {
        System.out.println("ParameterizedConstructorAndMethodTest.someOtherTest()");
    }

    private static Object[] o(Object... objects) {
        return objects;
    }
}
