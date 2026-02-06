package gr.aueb.cf.cookingfactory.core.exceptions;

import lombok.Getter;
import org.springframework.validation.BindingResult;

@Getter
public class ValidationException extends Exception {
    private final BindingResult bindingResult;

    public ValidationException(BindingResult bindingResult) {
        super("Validation failed");
        this.bindingResult = bindingResult;
    }
}