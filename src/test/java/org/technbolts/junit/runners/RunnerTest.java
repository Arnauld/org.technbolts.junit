package org.technbolts.junit.runners;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class RunnerTest {

    private static List<String> testsExecution = new ArrayList<String>();

    @Before
    public void setUp() {
        testsExecution.clear();
    }

    @RunWith(Runner.class)
    public static class OneParametrizedMethod { // TODO this test is also executed from the external runner

        @Runner.DataProvider(name = "zog")
        public static Collection<Object[]> allZogs() {
            return Arrays.asList(o("pim"), o("pam"), o("poum"));
        }

        @Test
        @Runner.Parameterized(dataProvider = "zog")
        public void testZog(String value) {
            testsExecution.add(value);
        }

    }

    @Test
    public void one_parameterized_method() {
        Result result = JUnitCore.runClasses(OneParametrizedMethod.class);
        assertThat(result.getRunCount()).isEqualTo(3);
        assertThat(result.getFailureCount()).isEqualTo(0);
        assertThat(testsExecution).contains("pim", "pam", "poum");
    }

    @RunWith(Runner.class)
    public static class MultipleParametrizedMethod {

        @Runner.DataProvider(name = "zog")
        public static Collection<Object[]> allZogs() {
            return Arrays.asList(o("zig"), o("zag"), o("zoum"));
        }

        @Runner.DataProvider(name = "movies")
        public static Collection<Object[]> allMovies() {
            return Arrays.asList(o("One", 1), o("Two", 2));
        }

        @Test
        @Runner.Parameterized(dataProvider = "zog")
        public void testZog1(String value) {
            testsExecution.add("zog1:" + value);
        }

        @Test
        @Runner.Parameterized(dataProvider = "zog")
        public void testZog2(String value) {
            testsExecution.add("zog2:" + value);
        }

        @Test
        @Runner.Parameterized(dataProvider = "movies")
        public void movies(String value, int index) {
            testsExecution.add("movies:" + value + ":" + index);
        }

        @Test
        public void nonParameterized() {
            testsExecution.add("nonParameterized");
        }
    }

    @Test
    public void multiple_parametrized_method() {
        Result result = JUnitCore.runClasses(MultipleParametrizedMethod.class);
        assertThat(result.getRunCount()).isEqualTo(9);
        assertThat(result.getFailureCount()).isEqualTo(0);
        assertThat(testsExecution).contains(//
                "zog1:zig", "zog1:zag", "zog1:zoum", //
                "zog2:zig", "zog2:zag", "zog2:zoum", //
                "movies:One:1", "movies:Two:2", //
                "nonParameterized");
    }

    @RunWith(Runner.class)
    @Runner.Parameterized(dataProvider = "pif")
    public static class ParametrizedConstructor {

        @Runner.DataProvider(name = "pif")
        public static Collection<Object[]> allZogs() {
            return Arrays.asList(o("pif"), o("paf"), o("pouf"));
        }

        private final String param;

        public ParametrizedConstructor(String param) {
            this.param = param;
        }

        @Test
        public void nonParameterized1() {
            testsExecution.add(param + ":nonParameterized1");
        }

        @Test
        public void nonParameterized2() {
            testsExecution.add(param + ":nonParameterized2");
        }
    }

    @Test
    public void parametrized_constructor() {
        Result result = JUnitCore.runClasses(ParametrizedConstructor.class);

        assertThat(result.getRunCount()).isEqualTo(6);
        assertThat(result.getFailureCount()).isEqualTo(0);
        assertThat(testsExecution).contains(//
                "pif:nonParameterized1", "paf:nonParameterized1", "pouf:nonParameterized1", //
                "pif:nonParameterized2", "paf:nonParameterized2", "pouf:nonParameterized2");
    }

    @RunWith(Runner.class)
    @Runner.Parameterized(dataProvider = "databases")
    public static class ParametrizedConstructorWithParametrizedMethods {

        @Runner.DataProvider(name = "databases")
        public static Collection<Object[]> allZogs() {
            return Arrays.asList(o("oracle"), o("mysql"), o("pgsql"), o("db2"));
        }

        private final String database;

        public ParametrizedConstructorWithParametrizedMethods(String database) {
            this.database = database;
        }

        @Runner.DataProvider(name = "scripts")
        public static Collection<Object[]> allScripts() {
            return Arrays.asList(o("insert"), o("select"), o("update"), o("delete"));
        }

        @Test
        @Runner.Parameterized(dataProvider = "scripts")
        public void script(String script) {
            testsExecution.add(database + ":" + script);
        }

        @Test
        public void nonParameterized1() {
            testsExecution.add(database + ":nonParameterized1");
        }

        @Test
        public void nonParameterized2() {
            testsExecution.add(database + ":nonParameterized2");
        }
    }

    @Test
    public void parametrized_constructor_and_methods() {
        Result result = JUnitCore.runClasses(ParametrizedConstructorWithParametrizedMethods.class);

        assertThat(result.getRunCount()).isEqualTo(24);
        assertThat(result.getFailureCount()).isEqualTo(0);
        assertThat(testsExecution).contains(//
                "oracle:insert", "oracle:select", "oracle:update", //
                "oracle:delete", "oracle:nonParameterized1", "oracle:nonParameterized2", //
                "mysql:insert", "mysql:select", "mysql:update", //
                "mysql:delete", "mysql:nonParameterized1", "mysql:nonParameterized2", //
                "pgsql:insert", "pgsql:select", "pgsql:update", //
                "pgsql:delete", "pgsql:nonParameterized1", "pgsql:nonParameterized2", //
                "db2:insert", "db2:select", "db2:update", //
                "db2:delete", "db2:nonParameterized1", "db2:nonParameterized2"
        );
    }

    private static Object[] o(Object... objects) {
        return objects;
    }
}
