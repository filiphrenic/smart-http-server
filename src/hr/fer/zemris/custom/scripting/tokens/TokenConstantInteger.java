package hr.fer.zemris.custom.scripting.tokens;

/**
 * This class is used for storing integer tokens inside of echo and forloop nodes.
 * @author Filip Hrenić
 * @version 1.0
 */
public class TokenConstantInteger extends Token {
	
	private final int value;
	
	/**
	 * Setter method for value property.
	 * Given value must be a integer (positive or negative).
	 * @param value desired value of the token
	 */
	public TokenConstantInteger( int value ){
		this.value = value;
	}
	
	/**
	 * Getter method for value property.
	 * @return value of the token
	 */
	public int getValue(){
		return this.value;
	}
	
	/**
	 * Returns the value of the token as a String.
	 * @return the value as a String
	 */
	@Override
	public String asText(){
		return String.valueOf(this.value);
	}
}
