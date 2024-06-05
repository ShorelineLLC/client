package net.shoreline.eventbus.annotation;

import net.shoreline.eventbus.event.Event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to identify a cancelable {@link Event}. Events that are
 * cancelable are given access to {@link Event#setCanceled(boolean)}.
 *
 * @author linus
 * @see Event
 * @since 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cancelable {

}
