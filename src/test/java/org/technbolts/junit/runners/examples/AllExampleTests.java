package org.technbolts.junit.runners.examples;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.technbolts.junit.runners.RunnerTest;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        FibonacciConstructorTest.class,
        FibonacciMethodTest.class,
        MultiDatabaseScriptsTest.class,
        ParameterizedConstructorAndMethodTest.class,
        ParameterizedMethodTest.class
})
public class AllExampleTests {
}
