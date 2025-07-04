package nbc.chillguys.nebulazone.domain.common.validator.image;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = ImageFileValidator.class)
public @interface ImageFile {

	String message() default "허용된 이미지 파일이 아닙니다.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
