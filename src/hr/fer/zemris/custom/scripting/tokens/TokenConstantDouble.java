package hr.fer.zemris.custom.scripting.tokens;

/**
 * This class is used for storing double tokens inside of echo and forloop nodes.
 * @author Filip Hrenić
 * @version 1.0
 */
public class TokenConstantDouble extends Token {

	private final double value;

	/**
	 * Setter method for value property.
	 * Given value must be a double (positive or negative).
	 * @param value desired value of the token
	 */
	public TokenConstantDouble( double value ){
		this.value = value;
	}

	/**
	 * Getter method for value property.
	 * @return value of the token
	 */
	public double getValue(){
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
