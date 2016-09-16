package hr.fer.zemris.custom.scripting.tokens;

/**
 * This class is used for storing operator tokens inside of echo and forloop nodes.
 * 
 * @author Filip Hrenić
 * @version 1.0
 */
public class TokenOperator extends Token {

    private final String symbol;

    /**
     * Setter method for symbol property.
     * Given symbol must be one of these: +,-,*,/.
     * 
     * @param symbol desired value of the token
     */
    public TokenOperator(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Getter method for symbol property.
     * 
     * @return value of the token
     */
    public String getSymbol() {
        return this.symbol;
    }

    /**
     * Returns the value of the token.
     * 
     * @return the value
     */
    @Override
    public String asText() {
        return this.symbol;
    }
}
