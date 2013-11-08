package org.technbolts.junit.runners.examples;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.technbolts.junit.runners.Runner;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
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
