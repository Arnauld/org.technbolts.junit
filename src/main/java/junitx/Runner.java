package junitx;

import org.junit.Test;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class Runner extends Suite {

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public @interface DataProvider {
        String name();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public @interface Parameterized {
        String namePattern() default "{index}";

        String dataProvider();
    }

    private static final List<org.junit.runner.Runner> NO_RUNNERS = Collections.emptyList();

    private final List<org.junit.runner.Runner> runners = new ArrayList<org.junit.runner.Runner>();

    /**
     * Creates a BlockJUnit4ClassRunner to run {@code klass}
     *
     * @throws org.junit.runners.model.InitializationError
     *          if the test class is malformed.
     */
    public Runner(Class<?> klass) throws Throwable {
        super(klass, NO_RUNNERS);
        createRunners();
    }

    @Override
    protected List<org.junit.runner.Runner> getChildren() {
        return runners;
    }

    private void createRunners() throws Throwable {
        List<FrameworkMethod> testMethods = getTestClass().getAnnotatedMethods(Test.class);
        for (FrameworkMethod method : testMethods) {
            Parameterized parameterized = method.getAnnotation(Parameterized.class);
            if (parameterized == null) {
                createSimpleTest(getTestClass(), method);
            } else {
                createParameterizedTest(getTestClass(), method, parameterized);
            }
        }
    }

    private void createParameterizedTest(TestClass testClass, FrameworkMethod method, Parameterized parameterized) throws Throwable {
        String namePattern = parameterized.namePattern();
        int i = 0;
        for (Object[] parametersOfSingleTest : allParameters(parameterized.dataProvider())) {
            String name = nameFor(namePattern, i, parametersOfSingleTest);
            TestClassParameterizedMethodRunner runner = new TestClassParameterizedMethodRunner(
                    testClass.getJavaClass(), method, parametersOfSingleTest, name);
            runners.add(runner);
            ++i;
        }
    }

    private String nameFor(String namePattern, int index, Object[] parameters) {
        String finalPattern = namePattern.replaceAll("\\{index\\}",
                Integer.toString(index));
        String name = MessageFormat.format(finalPattern, parameters);
        return "[" + name + "]";
    }


    private Iterable<Object[]> allParameters(String dataProviderName) throws Throwable {
        FrameworkMethod dataProviderMethod = getDataProviderMethod(dataProviderName);
        Object parameters = dataProviderMethod.invokeExplosively(null);
        if (parameters instanceof Iterable) {
            return (Iterable<Object[]>) parameters;
        } else {
            throw parametersMethodReturnedWrongType(dataProviderMethod);
        }
    }

    private Exception parametersMethodReturnedWrongType(FrameworkMethod dataProviderMethod) throws Exception {
        String className = getTestClass().getName();
        String methodName = dataProviderMethod.getName();
        String message = MessageFormat.format(
                "{0}.{1}() must return an Iterable of arrays.",
                className, methodName);
        return new Exception(message);
    }


    private FrameworkMethod getDataProviderMethod(String name) throws Exception {
        List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(
                DataProvider.class);
        for (FrameworkMethod each : methods) {
            if (each.isStatic() && each.isPublic()) {
                return each;
            }
        }

        throw new Exception("No public static parameters method on class "
                + getTestClass().getName());
    }

    private void createSimpleTest(TestClass testClass, FrameworkMethod method) throws InitializationError {
        runners.add(new TestClassMethodRunner(testClass.getJavaClass(), method));
    }

    private class TestClassMethodRunner extends BlockJUnit4ClassRunner {
        private final FrameworkMethod method;

        TestClassMethodRunner(Class<?> type, FrameworkMethod method) throws InitializationError {
            super(type);
            this.method = method;
        }

        @Override
        protected List<FrameworkMethod> computeTestMethods() {
            return Arrays.asList(method);
        }

        /**
         * Adds to {@code errors} for each method annotated with {@code @Test}that
         * is not a public, void instance method with no arguments.
         */
        protected void validateTestMethods(List<Throwable> errors) {
            // method is already provided and scanned
            //   validatePublicVoidNoArgMethods(Test.class, false, errors);
        }

    }

    private class TestClassParameterizedMethodRunner extends BlockJUnit4ClassRunner {
        private final FrameworkMethod method;
        private final Object[] params;
        private final String name;

        TestClassParameterizedMethodRunner(Class<?> type, FrameworkMethod method, Object[] params, String name) throws InitializationError {
            super(type);
            this.method = method;
            this.params = params;
            this.name = name;
        }



        @Override
        protected List<FrameworkMethod> computeTestMethods() {
            return Arrays.asList(method);
        }

        protected Statement methodInvoker(FrameworkMethod method, Object test) {
            return new InvokeParameterizedMethod(method, test, params);
        }

        /**
         * Adds to {@code errors} for each method annotated with {@code @Test}that
         * is not a public, void instance method with no arguments.
         */
        protected void validateTestMethods(List<Throwable> errors) {
            // method is already provided and scanned
            //   validatePublicVoidNoArgMethods(Test.class, false, errors);
        }

    }

    public static class InvokeParameterizedMethod extends Statement {
        private final FrameworkMethod testMethod;
        private final Object target;
        private final Object[] params;

        public InvokeParameterizedMethod(FrameworkMethod testMethod, Object target, Object[] params) {
            this.testMethod = testMethod;
            this.target = target;
            this.params = params;
        }

        @Override
        public void evaluate() throws Throwable {
            testMethod.invokeExplosively(target, params);
        }
    }
}
