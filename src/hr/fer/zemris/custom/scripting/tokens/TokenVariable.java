package hr.fer.zemris.custom.scripting.tokens;

/**
 * This class is used for storing token variables inside of echo and forloop nodes.
 * 
 * @author Filip Hrenić
 * @version 1.0
 */
public class TokenVariable extends Token {

    private final String name;

    /**
     * Setter method for the name property.
     * Name must start with a letter which can be followed by letters, digits and underscores.
     * 
     * @param name desired name of the token
     */
    public TokenVariable(String name) {
        this.name = name;
    }

    /**
     * Getter method for proprety name.
     * 
     * @return name of object
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the token name as it was given.
     * 
     * @return the token name
     */
    @Override
    public String asText() {
        return this.name;
    }

}
