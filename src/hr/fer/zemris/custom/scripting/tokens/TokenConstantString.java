package hr.fer.zemris.custom.scripting.tokens;

/**
 * This class is used for storing string tokens inside of echo and forloop nodes.
 * 
 * @author Filip Hrenić
 * @version 1.0
 */
public class TokenConstantString extends Token {

    private final String value;

    /**
     * Setter method for value property.
     * Given value must start and end with " sign.
     * 
     * @param value desired value of the token
     */
    public TokenConstantString(String value) {
        this.value = value;
    }

    /**
     * Getter method for value property.
     * 
     * @return value of the token
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Returns the value of the token.
     * 
     * @return the value
     */
    @Override
    public String asText() {
        return "\"" + this.value + "\"";
    }
}
