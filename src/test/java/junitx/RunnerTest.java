package junitx;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collection;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class RunnerTest {
    @RunWith(Runner.class)
    public static class ParameterizedMethodTest {

        @Runner.DataProvider(name = "zog")
        public static Collection<Object[]> allZogs() {
            return Arrays.<Object[]>asList(new Object[]{"pim"}, new Object[]{"pam"}, new Object[]{"poum"});
        }

        @Test
        @Runner.Parameterized(dataProvider = "zog")
        public void testZog(String value) {
            System.out.println(">>>" + value + "<<<");
        }

    }

    @Test
    public void parameterized_method() {
        Result result = JUnitCore.runClasses(ParameterizedMethodTest.class);
        assertThat(result.getRunCount()).isEqualTo(3);
        assertThat(result.getFailureCount()).isEqualTo(0);
    }
}
