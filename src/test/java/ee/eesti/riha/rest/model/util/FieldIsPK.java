package ee.eesti.riha.rest.model.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Below is to mark/annotate class (of data object) fields for following reason: when accessing via reflection then know
 * which field is primary key in model. This is only used as a guide for reflection.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FieldIsPK {

}
