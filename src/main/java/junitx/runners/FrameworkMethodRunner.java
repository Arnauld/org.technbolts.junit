package junitx.runners;

import org.junit.*;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.*;
import org.junit.rules.RunRules;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class FrameworkMethodRunner extends org.junit.runner.Runner {

    private final TestClass testClass;
    private final Object[] testArgs;
    private final FrameworkMethod method;
    private final Description description;
    private final Object[] methodArgs;

    public FrameworkMethodRunner(Class<?> testClass,
                                 String testName,
                                 FrameworkMethod method) throws InitializationError {
        this(testClass, null, testName, method, null);
    }

    public FrameworkMethodRunner(Class<?> testClass,
                                 Object[] testArgs,
                                 String testName,
                                 FrameworkMethod method, Object[] methodArgs) throws InitializationError {
        this(testClass, testArgs, Description.createTestDescription(testClass.getName(), testName),
                method, methodArgs);
    }

    public FrameworkMethodRunner(Class<?> testClass,
                                 Object[] testArgs,
                                 Description description,
                                 FrameworkMethod method, Object[] methodArgs) throws InitializationError {
        this.testClass = new TestClass(testClass);
        this.testArgs = testArgs;
        this.description = description;
        this.method = method;
        this.methodArgs = methodArgs;
        new TestValidator(this.testClass, Arrays.asList(method)).validate();
    }

    public TestClass getTestClass() {
        return testClass;
    }

    protected Object createTest() throws Exception {
        Constructor<?> constructor = getTestClass().getOnlyConstructor();
        if (testArgs == null || testArgs.length == 0)
            return constructor.newInstance();
        else
            return constructor.newInstance(testArgs);
    }

    @Override
    public Description getDescription() {
        return description;
    }

    protected Description describeMethod(FrameworkMethod method) {
        return getDescription();
    }

    @Override
    public void run(RunNotifier notifier) {
        Description description = getDescription();
        if (method.getAnnotation(Ignore.class) != null) {
            notifier.fireTestIgnored(description);
        } else {
            runLeaf(methodBlock(method), description, notifier);
        }
    }

    /**
     * Runs a {@link org.junit.runners.model.Statement} that represents a leaf (aka atomic) test.
     */
    protected final void runLeaf(Statement statement, Description description,
                                 RunNotifier notifier) {
        EachTestNotifier eachNotifier = new EachTestNotifier(notifier, description);
        eachNotifier.fireTestStarted();
        try {
            statement.evaluate();
        } catch (AssumptionViolatedException e) {
            eachNotifier.addFailedAssumption(e);
        } catch (Throwable e) {
            eachNotifier.addFailure(e);
        } finally {
            eachNotifier.fireTestFinished();
        }
    }

    /**
     * Returns a Statement that, when executed, either returns normally if
     * {@code method} passes, or throws an exception if {@code method} fails.
     * <p/>
     * Here is an outline of the default implementation:
     * <p/>
     * <ul>
     * <li>Invoke {@code method} on the result of {@code createTest()}, and
     * throw any exceptions thrown by either operation.
     * <li>HOWEVER, if {@code method}'s {@code @Test} annotation has the {@code
     * expecting} attribute, return normally only if the previous step threw an
     * exception of the correct type, and throw an exception otherwise.
     * <li>HOWEVER, if {@code method}'s {@code @Test} annotation has the {@code
     * timeout} attribute, throw an exception if the previous step takes more
     * than the specified number of milliseconds.
     * <li>ALWAYS run all non-overridden {@code @Before} methods on this class
     * and superclasses before any of the previous steps; if any throws an
     * Exception, stop execution and pass the exception on.
     * <li>ALWAYS run all non-overridden {@code @After} methods on this class
     * and superclasses after any of the previous steps; all After methods are
     * always executed: exceptions thrown by previous steps are combined, if
     * necessary, with exceptions from After methods into a
     * {@link org.junit.runners.model.MultipleFailureException}.
     * <li>ALWAYS allow {@code @Rule} fields to modify the execution of the
     * above steps. A {@code Rule} may prevent all execution of the above steps,
     * or add additional behavior before and after, or modify thrown exceptions.
     * For more information, see {@link org.junit.rules.TestRule}
     * </ul>
     * <p/>
     * This can be overridden in subclasses, either by overriding this method,
     * or the implementations creating each sub-statement.
     */
    protected Statement methodBlock(FrameworkMethod method) {
        Object test;
        try {
            test = new ReflectiveCallable() {
                @Override
                protected Object runReflectiveCall() throws Throwable {
                    return createTest();
                }
            }.run();
        } catch (Throwable e) {
            return new Fail(e);
        }

        Statement statement = methodInvoker(method, test);
        statement = possiblyExpectingExceptions(method, test, statement);
        statement = withPotentialTimeout(method, test, statement);
        statement = withBefores(method, test, statement);
        statement = withAfters(method, test, statement);
        statement = withRules(method, test, statement);
        return statement;
    }

    //
    // Statement builders
    //

    /**
     * Returns a {@link Statement} that invokes {@code method} on {@code test}
     */
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
        return new ParameterizedInvokeMethod(method, test, methodArgs);
    }

    /**
     * Returns a {@link Statement}: if {@code method}'s {@code @Test} annotation
     * has the {@code expecting} attribute, return normally only if {@code next}
     * throws an exception of the correct type, and throw an exception
     * otherwise.
     *
     * @deprecated Will be private soon: use Rules instead
     */
    @Deprecated
    protected Statement possiblyExpectingExceptions(FrameworkMethod method,
                                                    Object test, Statement next) {
        Test annotation = method.getAnnotation(Test.class);
        return expectsException(annotation) ? new ExpectException(next,
                getExpectedException(annotation)) : next;
    }

    /**
     * Returns a {@link Statement}: if {@code method}'s {@code @Test} annotation
     * has the {@code timeout} attribute, throw an exception if {@code next}
     * takes more than the specified number of milliseconds.
     *
     * @deprecated Will be private soon: use Rules instead
     */
    @Deprecated
    protected Statement withPotentialTimeout(FrameworkMethod method,
                                             Object test, Statement next) {
        long timeout = getTimeout(method.getAnnotation(Test.class));
        return timeout > 0 ? new FailOnTimeout(next, timeout) : next;
    }

    /**
     * Returns a {@link Statement}: run all non-overridden {@code @Before}
     * methods on this class and superclasses before running {@code next}; if
     * any throws an Exception, stop execution and pass the exception on.
     *
     * @deprecated Will be private soon: use Rules instead
     */
    @Deprecated
    protected Statement withBefores(FrameworkMethod method, Object target,
                                    Statement statement) {
        List<FrameworkMethod> befores = getTestClass().getAnnotatedMethods(
                Before.class);
        return befores.isEmpty() ? statement : new RunBefores(statement,
                befores, target);
    }

    /**
     * Returns a {@link Statement}: run all non-overridden {@code @After}
     * methods on this class and superclasses before running {@code next}; all
     * After methods are always executed: exceptions thrown by previous steps
     * are combined, if necessary, with exceptions from After methods into a
     * {@link org.junit.runners.model.MultipleFailureException}.
     *
     * @deprecated Will be private soon: use Rules instead
     */
    @Deprecated
    protected Statement withAfters(FrameworkMethod method, Object target,
                                   Statement statement) {
        List<FrameworkMethod> afters = getTestClass().getAnnotatedMethods(
                After.class);
        return afters.isEmpty() ? statement : new RunAfters(statement, afters,
                target);
    }

    private Statement withRules(FrameworkMethod method, Object target,
                                Statement statement) {
        List<TestRule> testRules = getTestRules(target);
        Statement result = statement;
        result = withMethodRules(method, testRules, target, result);
        result = withTestRules(method, testRules, result);

        return result;
    }

    private Statement withMethodRules(FrameworkMethod method, List<TestRule> testRules,
                                      Object target, Statement result) {
        for (org.junit.rules.MethodRule each : getMethodRules(target)) {
            if (!testRules.contains(each)) {
                result = each.apply(result, method, target);
            }
        }
        return result;
    }

    private List<org.junit.rules.MethodRule> getMethodRules(Object target) {
        return rules(target);
    }

    /**
     * @param target the test case instance
     * @return a list of MethodRules that should be applied when executing this
     *         test
     */
    protected List<org.junit.rules.MethodRule> rules(Object target) {
        return getTestClass().getAnnotatedFieldValues(target, Rule.class,
                org.junit.rules.MethodRule.class);
    }

    /**
     * Returns a {@link Statement}: apply all non-static {-@link Value} fields
     * annotated with {@link Rule}.
     *
     * @param statement The base statement
     * @return a RunRules statement if any class-level {@link Rule}s are
     *         found, or the base statement
     */
    private Statement withTestRules(FrameworkMethod method, List<TestRule> testRules,
                                    Statement statement) {
        return testRules.isEmpty() ? statement :
                new RunRules(statement, testRules, describeMethod(method));
    }

    /**
     * @param target the test case instance
     * @return a list of TestRules that should be applied when executing this
     *         test
     */
    protected List<TestRule> getTestRules(Object target) {
        List<TestRule> result = getTestClass().getAnnotatedMethodValues(target,
                Rule.class, TestRule.class);

        result.addAll(getTestClass().getAnnotatedFieldValues(target,
                Rule.class, TestRule.class));

        return result;
    }

    private Class<? extends Throwable> getExpectedException(Test annotation) {
        if (annotation == null || annotation.expected() == Test.None.class) {
            return null;
        } else {
            return annotation.expected();
        }
    }

    private boolean expectsException(Test annotation) {
        return getExpectedException(annotation) != null;
    }

    private long getTimeout(Test annotation) {
        if (annotation == null) {
            return 0;
        }
        return annotation.timeout();
    }

}
