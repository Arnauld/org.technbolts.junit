package org.technbolts.junit.runners;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.text.MessageFormat;
import java.util.ArrayList;
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
    @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface Parameterized {
        String namePattern() default "";

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
        createRunners(runners);
    }

    @Override
    protected List<org.junit.runner.Runner> getChildren() {
        return runners;
    }

    @Override
    protected Description describeChild(org.junit.runner.Runner child) {
        return child.getDescription();
    }

    private void createRunners(List<org.junit.runner.Runner> runners) throws Throwable {
        TestClass testClass = getTestClass();

        Parameterized testParameterized = testClass.getJavaClass().getAnnotation(Parameterized.class);

        if (testParameterized != null) {

            String namePattern = testParameterized.namePattern();
            if (namePattern.isEmpty()) {
                int length = testClass.getOnlyConstructor().getParameterTypes().length;
                namePattern = generateNamePattern(length); //"{method}:" + b;
            }

            int i = 0;
            for (Object[] testArgs : allParameters(testParameterized.dataProvider())) {
                List<org.junit.runner.Runner> subRunners = new ArrayList<org.junit.runner.Runner>();
                createRunnersWithTestParameters(subRunners, testArgs);
                if (subRunners.isEmpty())
                    continue;
                String name = nameFor(namePattern, i, testArgs);
                runners.add(new SuiteExt(getTestClass().getJavaClass(), name, subRunners));
                ++i;
            }
        } else {
            createRunnersWithTestParameters(runners, null);
        }
    }

    private String nameFor(String namePattern, int index, Object[] parameters) {
        String finalPattern = namePattern
                .replaceAll("\\{index\\}", Integer.toString(index))
                .replaceAll("\\{class\\}", getTestClass().getName());
        return MessageFormat.format(finalPattern, parameters);
    }

    protected static class SuiteExt extends Suite {
        private final String name;
        private final Description description;

        public SuiteExt(Class<?> klass, String name, List<org.junit.runner.Runner> runners) throws InitializationError {
            super(klass, runners);
            this.name = name;
            this.description = Description.createTestDescription(klass, name);
        }

        @Override
        protected String getName() {
            return name;
        }
    }

    private void createRunnersWithTestParameters(List<org.junit.runner.Runner> runners, Object[] testArgs) throws Throwable {
        List<FrameworkMethod> testMethods = getTestClass().getAnnotatedMethods(Test.class);
        for (FrameworkMethod method : testMethods) {
            Parameterized parameterized = method.getAnnotation(Parameterized.class);
            if (parameterized == null) {
                createSimpleTest(runners, testArgs, method);
            } else {
                createParameterizedTest(runners, testArgs, method, parameterized);
            }
        }
    }

    private String generateNamePattern(int length) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (i > 0)
                b.append(", ");
            b.append("{").append(i).append("}");
        }
        return b.toString();
    }

    private void createParameterizedTest(List<org.junit.runner.Runner> runners, Object[] testArgs, FrameworkMethod method, Parameterized parameterized) throws Throwable {
        String namePattern = parameterized.namePattern();
        if (namePattern.isEmpty()) {
            int length = method.getMethod().getParameterTypes().length;
            namePattern = generateNamePattern(length); //"{method}:" + b;
        }
        runners.add(new ParameterizedMethodRunner(getTestClass().getJavaClass(),
                testArgs,
                method,
                method.getName(),
                allParameters(parameterized.dataProvider()),
                namePattern));
    }

    private Iterable<Object[]> allParameters(String dataProviderName) throws Throwable {
        FrameworkMethod dataProviderMethod = getDataProviderMethod(dataProviderName);
        Object parameters = dataProviderMethod.invokeExplosively(null);
        if (parameters instanceof Iterable) {
            //noinspection unchecked
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
            DataProvider dataProvider = each.getAnnotation(DataProvider.class);
            if (each.isStatic() && each.isPublic() && name.equals(dataProvider.name())) {
                return each;
            }
        }

        throw new Exception("No public static data provider method on class "
                + getTestClass().getName() + " for name " + name);
    }

    private DescriptionTextUniquefier uniquefier = new DescriptionTextUniquefier();

    private void createSimpleTest(List<org.junit.runner.Runner> runners, Object[] testArgs, FrameworkMethod method) throws InitializationError {
        runners.add(new FrameworkMethodRunner(
                getTestClass().getJavaClass(), testArgs,
                uniquefier.getUniqueDescription(method.getName()),
                method, null));
    }

}
