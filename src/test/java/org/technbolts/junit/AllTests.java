package org.technbolts.junit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.technbolts.junit.runners.RunnerTest;
import org.technbolts.junit.runners.examples.AllExampleTests;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        RunnerTest.class,
        AllExampleTests.class
})
public class AllTests {
}
