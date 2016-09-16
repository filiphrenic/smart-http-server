package hr.fer.zemris.util;

/**
 * Some utility methods used throughout this project.
 * 
 * @author Filip Hrenić
 * @version 1.0
 */
public final class NumUtil {

    /**
     * Final class, can't be created. Only provides static methods.
     */
    private NumUtil() {
    }

    /**
     * Checks if the given value can be parsed as a double
     * 
     * @param value checked value
     * @return <code>true</code> if can be parsed as a double, <code>false</code> otherwise
     */
    public static boolean isDouble(final String value) {
        try {
            Double.parseDouble(value);
        } catch (final NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * Checks if the given value can be parsed as a double
     * 
     * @param value checked value
     * @return <code>true</code> if can be parsed as a double, <code>false</code> otherwise
     */
    public static boolean isInteger(final String value) {
        try {
            Integer.parseInt(value);
        } catch (final NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * Returns the value parsed as int. If it can't be parsed to integer, 0 is returned.
     * 
     * @param value value you want to parse
     * @return integer representation of value, or 0 if value can't be represented as a integer
     */
    public static int getInt(final Object value) {
        int result = 0;
        try {
            result = Integer.parseInt(value.toString());
        } catch (NumberFormatException ignored) {
        }
        return result;
    }
}
