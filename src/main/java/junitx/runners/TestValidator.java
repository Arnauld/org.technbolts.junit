package junitx.runners;

import org.junit.*;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import static org.junit.internal.runners.rules.RuleFieldValidator.*;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class TestValidator {


    private final TestClass testClass;
    private final List<FrameworkMethod> methods;

    public TestValidator(TestClass testClass) {
        this(testClass, null);
    }

    public TestValidator(TestClass testClass, List<FrameworkMethod> methods) {
        this.testClass = testClass;
        this.methods = methods==null ? computeTestMethods() : methods;
    }


    /**
     * Returns the methods that run tests. Default implementation returns all
     * methods annotated with {@code @Test} on this class and superclasses that
     * are not overridden.
     */
    protected List<FrameworkMethod> computeTestMethods() {
        return getTestClass().getAnnotatedMethods(Test.class);
    }

    public TestClass getTestClass() {
        return testClass;
    }

    protected void collectInitializationErrors(List<Throwable> errors) {
        validatePublicVoidNoArgMethods(BeforeClass.class, true, errors);
        validatePublicVoidNoArgMethods(AfterClass.class, true, errors);
        validateClassRules(errors);

        validateNoNonStaticInnerClass(errors);
        validateConstructor(errors);
        validateInstanceMethods(errors);
        validateFields(errors);
        validateMethods(errors);
    }

    protected void validateNoNonStaticInnerClass(List<Throwable> errors) {
        if (getTestClass().isANonStaticInnerClass()) {
            String gripe = "The inner class " + getTestClass().getName()
                    + " is not static.";
            errors.add(new Exception(gripe));
        }
    }

    /**
     * Adds to {@code errors} if the test class has more than one constructor,
     * or if the constructor takes parameters. Override if a subclass requires
     * different validation rules.
     */
    protected void validateConstructor(List<Throwable> errors) {
        validateOnlyOneConstructor(errors);
        validateZeroArgConstructor(errors);
    }

    /**
     * Adds to {@code errors} if the test class has more than one constructor
     * (do not override)
     */
    protected void validateOnlyOneConstructor(List<Throwable> errors) {
        if (!hasOneConstructor()) {
            String gripe = "Test class should have exactly one public constructor";
            errors.add(new Exception(gripe));
        }
    }

    /**
     * Adds to {@code errors} if the test class's single constructor takes
     * parameters (do not override)
     */
    protected void validateZeroArgConstructor(List<Throwable> errors) {
        if (!getTestClass().isANonStaticInnerClass()
                && hasOneConstructor()
                && (getTestClass().getOnlyConstructor().getParameterTypes().length != 0)) {
            String gripe = "Test class should have exactly one public zero-argument constructor";
            errors.add(new Exception(gripe));
        }
    }

    private boolean hasOneConstructor() {
        return getTestClass().getJavaClass().getConstructors().length == 1;
    }

    /**
     * Adds to {@code errors} for each method annotated with {@code @Test},
     * {@code @Before}, or {@code @After} that is not a public, void instance
     * method with no arguments.
     *
     * @deprecated unused API, will go away in future version
     */
    @Deprecated
    protected void validateInstanceMethods(List<Throwable> errors) {
        validatePublicVoidNoArgMethods(After.class, false, errors);
        validatePublicVoidNoArgMethods(Before.class, false, errors);
        validateTestMethods(errors);

        if (computeTestMethods().size() == 0) {
            errors.add(new Exception("No runnable methods"));
        }
    }

    protected void validateFields(List<Throwable> errors) {
        RULE_VALIDATOR.validate(getTestClass(), errors);
    }

    private void validateMethods(List<Throwable> errors) {
        RULE_METHOD_VALIDATOR.validate(getTestClass(), errors);
    }

    /**
     * Adds to {@code errors} for each method annotated with {@code @Test}that
     * is not a public, void instance method with no arguments.
     */
    protected void validateTestMethods(List<Throwable> errors) {
        for(FrameworkMethod method : computeTestMethods()) {
            method.validatePublicVoid(false, errors);
        }
    }

    /**
     * Returns a new fixture for running a test. Default implementation executes
     * the test class's no-argument constructor (validation should have ensured
     * one exists).
     */
    protected Object createTest() throws Exception {
        return getTestClass().getOnlyConstructor().newInstance();
    }

    /**
     * Adds to {@code errors} if any method in this class is annotated with
     * {@code annotation}, but:
     * <ul>
     * <li>is not public, or
     * <li>takes parameters, or
     * <li>returns something other than void, or
     * <li>is static (given {@code isStatic is false}), or
     * <li>is not static (given {@code isStatic is true}).
     */
    protected void validatePublicVoidNoArgMethods(Class<? extends Annotation> annotation,
                                                  boolean isStatic, List<Throwable> errors) {
        List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(annotation);

        for (FrameworkMethod eachTestMethod : methods) {
            eachTestMethod.validatePublicVoidNoArg(isStatic, errors);
        }
    }

    private void validateClassRules(List<Throwable> errors) {
        CLASS_RULE_VALIDATOR.validate(getTestClass(), errors);
        CLASS_RULE_METHOD_VALIDATOR.validate(getTestClass(), errors);
    }

    public void validate() throws InitializationError {
        List<Throwable> errors = new ArrayList<Throwable>();
        collectInitializationErrors(errors);
        if (!errors.isEmpty()) {
            throw new InitializationError(errors);
        }
    }

}
