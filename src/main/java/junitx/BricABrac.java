package junitx;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class BricABrac {

    public static String signature(Class[] params) {
        StringBuilder sb = new StringBuilder();

        for (int j = 0; j < params.length; j++) {
            sb.append(getTypeName(params[j]));
            if (j < (params.length - 1))
                sb.append(",");
        }
        return sb.toString();
    }

    static String getTypeName(Class type) {
        if (type.isArray()) {
            try {
                Class cl = type;
                int dimensions = 0;
                while (cl.isArray()) {
                    dimensions++;
                    cl = cl.getComponentType();
                }
                StringBuffer sb = new StringBuffer();
                sb.append(cl.getName());
                for (int i = 0; i < dimensions; i++) {
                    sb.append("[]");
                }
                return sb.toString();
            } catch (Throwable e) { /*FALLTHRU*/ }
        }
        return type.getName();
    }
}
