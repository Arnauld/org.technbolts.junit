package org.technbolts.junit.runners.examples;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.technbolts.junit.runners.Runner;

import java.util.Arrays;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
@RunWith(Runner.class)
@Runner.Parameterized(dataProvider = "fib-seq", namePattern = "{index}: fib({0})={1}")
public class FibonacciConstructorTest {


    @Runner.DataProvider(name = "fib-seq")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {0, 0}, {1, 1}, {2, 1},
                {3, 2}, {4, 3}, {5, 5}, {6, 8}});
    }

    private final int value;
    private final int expected;

    public FibonacciConstructorTest(int value, int expected) {
        this.value = value;
        this.expected = expected;
    }

    @Test
    public void testFib() {
        assertThat(fib(value)).isEqualTo(expected);
    }

    public static int fib(int value) {
        if (value == 0 || value == 1)
            return value;
        else
            return fib(value - 1) + fib(value - 2);
    }
}
