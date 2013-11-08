package junitx.runners;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class ParameterizedInvokeMethod extends Statement {
    private final FrameworkMethod testMethod;
    private final Object target;
    private final Object[] params;

    public ParameterizedInvokeMethod(FrameworkMethod testMethod, Object target, Object[] params) {
        this.testMethod = testMethod;
        this.target = target;
        this.params = params;
    }

    @Override
    public void evaluate() throws Throwable {
        if (params == null || params.length == 0)
            testMethod.invokeExplosively(target);
        else
            testMethod.invokeExplosively(target, params);
    }
}
