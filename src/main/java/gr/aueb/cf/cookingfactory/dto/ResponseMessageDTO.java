package gr.aueb.cf.cookingfactory.dto;

public record ResponseMessageDTO(String code, String description) {

    public ResponseMessageDTO(String code) {
        this(code, "");
    }
}
