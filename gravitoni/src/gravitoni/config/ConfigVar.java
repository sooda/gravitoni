package gravitoni.config;

import java.lang.annotation.*;

/** Supercool annotation helper for reading variables straight into objects. */
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigVar {
	String value();
	boolean mandatory() default false;
}
