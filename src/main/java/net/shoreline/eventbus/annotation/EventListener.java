package net.shoreline.eventbus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author linus
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventListener {
    /**
     * Returns the event priority of the listener. The default values is
     * set to <b>0</b>.
     *
     * @return The priority of the listener
     */
    int priority() default 0;

    /**
     * @return Whether canceled events can be dispatched
     */
    boolean receiveCanceled() default true;
}
