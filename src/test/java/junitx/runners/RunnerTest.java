package junitx.runners;

import junitx.runners.Runner;
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
    public static class MultipleParametrizedMethod { // TODO this test is also executed from the external runner

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

    private static Object[] o(Object...objects) {
        return objects;
    }
}
