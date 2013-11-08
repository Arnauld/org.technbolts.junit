package org.technbolts.junit;

import org.technbolts.junit.runners.RunnerTest;
import org.technbolts.junit.runners.examples.ParameterizedConstructorAndMethodTest;
import org.technbolts.junit.runners.examples.ParameterizedMethodTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        RunnerTest.class,
        ParameterizedMethodTest.class,
        ParameterizedConstructorAndMethodTest.class
})
public class AllTests {
}
