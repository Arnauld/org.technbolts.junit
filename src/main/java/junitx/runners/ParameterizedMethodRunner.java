package junitx.runners;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class ParameterizedMethodRunner extends ParentRunner<FrameworkMethodRunner> {

    private final FrameworkMethod method;
    private final String name;
    private final Iterable<Object[]> parameters;
    private final String namePattern;
    private final List<FrameworkMethodRunner> children;

    public ParameterizedMethodRunner(Class<?> testClass,
                                     FrameworkMethod method,
                                     String name,
                                     Iterable<Object[]> parameters,
                                     String namePattern) throws InitializationError {
        super(testClass);
        this.method = method;
        this.name = name;
        this.parameters = parameters;
        this.namePattern = namePattern;
        this.children = createChildren();
    }

    @Override
    protected String getName() {
        return name;
    }

    private static AtomicInteger idGen = new AtomicInteger();

    private List<FrameworkMethodRunner> createChildren() throws InitializationError {
        List<FrameworkMethodRunner> children = new ArrayList<FrameworkMethodRunner>();
        int i = 0;
        for (Object[] parametersOfSingleTest : parameters) {
            Class<?> javaClass = getTestClass().getJavaClass();
            String testName = nameFor(namePattern, i, parametersOfSingleTest);
            Description description = Description.createTestDescription(getTestClass().getName(), testName, idGen.incrementAndGet());
            children.add(new FrameworkMethodRunner(javaClass, null, description, method, parametersOfSingleTest));
            ++i;
        }
        return children;
    }

    private String nameFor(String namePattern, int index, Object[] parameters) {
        String finalPattern = namePattern
                .replaceAll("\\{index\\}", Integer.toString(index))
                .replaceAll("\\{method\\}", method.getName());
        String name = MessageFormat.format(finalPattern, parameters);
        return "[" + name + "]";
    }


    @Override
    public Description getDescription() {
        Description description = Description.createSuiteDescription(getName(),
                //idGen.incrementAndGet(),
                getRunnerAnnotations());
        for (FrameworkMethodRunner child : getFilteredChildren0()) {
            description.addChild(describeChild(child));
        }
        return description;
    }

    private List<FrameworkMethodRunner> getFilteredChildren0() {
        try {
            Method method = ParentRunner.class.getDeclaredMethod("getFilteredChildren");
            method.setAccessible(true);
            return (List<FrameworkMethodRunner>) method.invoke(this);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected List<FrameworkMethodRunner> getChildren() {
        return children;
    }

    @Override
    protected Description describeChild(FrameworkMethodRunner child) {
        return child.getDescription();
    }

    @Override
    protected void runChild(final FrameworkMethodRunner method, RunNotifier notifier) {
        method.run(notifier);
    }
}
