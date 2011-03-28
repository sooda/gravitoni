package gravitoni.config;

import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigVar {
	String value();
	boolean mandatory() default false;
}
