package junitx;

import junitx.runners.RunnerTest;
import junitx.runners.examples.ParameterizedMethodTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        RunnerTest.class,
        ParameterizedMethodTest.class
})
public class AllTests {
}
