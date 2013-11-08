package org.technbolts.junit.runners;

import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class DescriptionTextUniquefier {
    private Set<String> strings = new HashSet<String>();

    public String getUniqueDescription(String junitSafeString) {
        while (strings.contains(junitSafeString)) {
            junitSafeString = junitSafeString + '\u200B'; // zero-width-space
        }
        strings.add(junitSafeString);
        return junitSafeString;
    }
}
