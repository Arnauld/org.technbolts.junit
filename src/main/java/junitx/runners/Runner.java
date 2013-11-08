package junitx.runners;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

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
    @Target({ElementType.METHOD})
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
        createRunners();
    }

    @Override
    protected List<org.junit.runner.Runner> getChildren() {
        return runners;
    }

    @Override
    protected Description describeChild(org.junit.runner.Runner child) {
        return child.getDescription();
    }

    private void createRunners() throws Throwable {
        List<FrameworkMethod> testMethods = getTestClass().getAnnotatedMethods(Test.class);
        for (FrameworkMethod method : testMethods) {
            Parameterized parameterized = method.getAnnotation(Parameterized.class);
            if (parameterized == null) {
                createSimpleTest(method);
            } else {
                createParameterizedTest(method, parameterized);
            }
        }
    }

    private void createParameterizedTest(FrameworkMethod method, Parameterized parameterized) throws Throwable {
        String namePattern = parameterized.namePattern();
        if (namePattern.isEmpty()) {
            int length = method.getMethod().getParameterTypes().length;
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < length; i++) {
                if (b.length() > 0)
                    b.append(", ");
                b.append("{").append(i).append("}");
            }
            namePattern = "" + b; //"{method}:" + b;
        }
        runners.add(new ParameterizedMethodRunner(getTestClass().getJavaClass(),
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

    private void createSimpleTest(FrameworkMethod method) throws InitializationError {
        runners.add(new FrameworkMethodRunner(getTestClass().getJavaClass(), method.getName(), method));
    }

}
