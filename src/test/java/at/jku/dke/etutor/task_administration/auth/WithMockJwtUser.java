package at.jku.dke.etutor.task_administration.auth;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockJwtUserContextFactory.class)
public @interface WithMockJwtUser {
    String uid() default "1";

    String sub() default "user";

    String fullAdmin() default "false";

    String[] roles() default {};
}
