package org.jvnet.jaxb.xml.bind;

import java.lang.annotation.*;

/**
 * add @org.jvnet.jaxb.xml.bind.Generated annotations to all generated nodes where possible;
 * useful for JaCoCo (which has built in support), or other style checkers and code coverage tools:
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Generated {
}
