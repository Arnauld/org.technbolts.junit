# JUnit extensions

Provides utilities to enrich [Junit](http://junit.org/).

## Parameterized test

When running a parameterized test class, instances are created for the cross-product of
the test methods and the test data elements.
Compared with the `org.junit.runner.Parameterized` (see [Junit Parameterized tests](https://github.com/junit-team/junit/wiki/Parameterized-tests))
`org.technbolts.runner.Runner` allows both test class and test methods to be parameterized.
Furthermore it is possible to use several and different `DataProvider` within the same test class,
thus it is possible to test against different set of values.

### Parameterized method

```java
@RunWith(Runner.class)
public class FibonacciMethodTest {
    @Runner.DataProvider(name = "fib-seq")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {0, 0}, {1, 1}, {2, 1},
                {3, 2}, {4, 3}, {5, 5}, {6, 8}});
    }

    @Test
    @Runner.Parameterized(dataProvider = "fib-seq", namePattern = "{index}: fib({0})={1}")
    public void testFib(int value, int expected) {
        assertThat(fib(value)).isEqualTo(expected);
    }

    public static int fib(int value) {
        if (value == 0 || value == 1)
            return value;
        else
            return fib(value - 1) + fib(value - 2);
    }
}
```


### Parameterized constructor

```java
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

    public staic int fib(int value) {
        if (value == 0 || value == 1)
            return value;
        else
            return fib(value - 1) + fib(value - 2);
    }
}
```

### Parameterized Test combined with parameterized methods

![Alt text](/doc/images/MultiDatabaseScriptsTest-resultTree.png "Result tree screenshot")

```java
@RunWith(Runner.class)
@Runner.Parameterized(dataProvider = "databases")
public class MultiDatabaseScriptsTest {

    @Runner.DataProvider(name = "databases")
    public static Collection<Object[]> allDatabases() {
        return Arrays.asList(o("db2"), o("mysql"), o("oracle"), o("postgres"));
    }

    private final String database;

    public MultiDatabaseScriptsTest(String database) {
        this.database = database;
    }

    @Runner.DataProvider(name = "insertion-scripts")
    public static Collection<Object[]> insertions() {
        return Arrays.asList(o("insert1.sql"), o("insert2.sql"));
    }

    @Test
    @Runner.Parameterized(dataProvider = "insertion-scripts")
    public void insert(String script) {
        executeScript(script);
    }

    @Runner.DataProvider(name = "migration-scripts")
    public static Collection<Object[]> migrations() {
        return Arrays.asList(o("migration_01.sql"), o("migration_02.sql"));
    }

    @Test
    @Runner.Parameterized(dataProvider = "migration-scripts")
    public void migrate(String script) {
        executeScript(script);
    }

    private static Object[] o(Object... objects) {
        return objects;
    }

    private void executeScript(String script) {
        //...
    }

}
```

