package hr.fer.zemris.custom.scripting.exec;

import hr.fer.zemris.custom.scripting.exec.enums.Operation;
import hr.fer.zemris.custom.scripting.exec.enums.Type;
import hr.fer.zemris.util.NumUtil;

/**
 * A class that is used for storing a object that can be parsed to a number (integer or double).
 * 
 * @author Filip Hrenić
 * @version 1.0
 */
public class ValueWrapper {

    /**
     * Value stored in this {@link ValueWrapper}.
     */
    private Object value;

    /**
     * Values type.
     */
    private final Type type;

    /**
     * Creates {@link ValueWrapper} with initial <code>value</code>.
     * 
     * @param value initial value
     */
    public ValueWrapper(Object value) {
        this.value = value;
        this.type = determineType(value);
    }

    /**
     * Gets the value.
     * 
     * @return value
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * Sets the value.
     * 
     * @param value wanted value
     */
    public void setValue(final Object value) {
        this.value = value;
    }

    /**
     * Determines the type of the given value.
     * 
     * @param value tested value
     * @return type of the value
     */
    private static Type determineType(final Object value) {
        if (value == null) {
            return Type.INTEGER;

        }

        if (NumUtil.isInteger(value.toString())) {
            return Type.INTEGER;
        } else if (NumUtil.isDouble(value.toString())) {
            return Type.DOUBLE;
        }

        throw new IllegalArgumentException(
                "Value must be a integer, double or a string that can be parsed to integer/double.");
    }

    /**
     * Compares two objects. Null values are treated as 0. If they are of different types, they are both converted into
     * {@link Double}, otherwise they are compared as they are.
     * 
     * @param withValue value compared to this value
     * @return negative number (if this value is less then withValue), 0 if they are equal, positive number otherwise
     */
    public int numCompare(final Object withValue) {

        final Type typeOfWithValue = determineType(withValue);

        // at this point, withValue is either null or a parsable integer/double

        if (this.type != Type.INTEGER || typeOfWithValue != Type.INTEGER) {
            final Double v1 = getDoubleValue(this.value);
            final Double v2 = getDoubleValue(withValue);
            return v1.compareTo(v2);
        } else {
            final Integer v1 = getIntegerValue(this.value);
            final Integer v2 = getIntegerValue(withValue);
            return v1.compareTo(v2);
        }

    }

    /**
     * Used when incrementing the stored value with given value
     * 
     * @param incValue value which is used to perform operation
     */
    public void increment(final Object incValue) {
        this.value = this.performOperation(incValue, Operation.INCREMENT);
    }

    /**
     * Used when decrementing the stored value with given value
     * 
     * @param incValue value which is used to perform operation
     */
    public void decrement(final Object incValue) {
        this.value = this.performOperation(incValue, Operation.DECREMENT);
    }

    /**
     * Used when multiplying the stored value with given value
     * 
     * @param incValue value which is used to perform operation
     */
    public void multiply(final Object incValue) {
        this.value = this.performOperation(incValue, Operation.MULTIPLY);
    }

    /**
     * Used when dividing the stored value with given value
     * 
     * @param incValue value which is used to perform operation
     */
    public void divide(final Object incValue) {
        this.value = this.performOperation(incValue, Operation.DIVIDE);
    }

    /**
     * Delegates the operation to the operation performer on integers or on doubles.
     * 
     * @param incValue value that will be operated on
     * @param oper operation performed
     * @return result of the performed operation
     */
    private Object performOperation(final Object incValue, final Operation oper) {
        final Type typeOfIncValue = determineType(incValue);

        if (this.type != Type.INTEGER || typeOfIncValue != Type.INTEGER) {
            final Double v1 = getDoubleValue(this.value);
            final Double v2 = getDoubleValue(incValue);
            return performOperationDouble(v1, v2, oper);
        } else {
            final Integer v1 = getIntegerValue(this.value);
            final Integer v2 = getIntegerValue(incValue);
            return performOperationInteger(v1, v2, oper);
        }
    }

    /**
     * Performs a given operation on two double values.
     * 
     * @param o1 first value
     * @param o2 second value
     * @param oper operation to perform
     * @return result of the operation
     */
    private Double performOperationDouble(final Double o1, final Double o2, final Operation oper) {
        switch (oper) {
            case INCREMENT:
                return o1 + o2;
            case DECREMENT:
                return o1 - o2;
            case MULTIPLY:
                return o1 * o2;
            default:
                return o1 / o2;
        }
    }

    /**
     * Performs a given operation on two integer values.
     * 
     * @param o1 first value
     * @param o2 second value
     * @param oper operation to perform
     * @return result of the operation
     */
    private Integer performOperationInteger(final Integer o1, final Integer o2, final Operation oper) {
        switch (oper) {
            case INCREMENT:
                return o1 + o2;
            case DECREMENT:
                return o1 - o2;
            case MULTIPLY:
                return o1 * o2;
            default:
                return o1 / o2;
        }
    }

    /**
     * Returns a double that is the value of given value.
     * 
     * @param value value being parsed
     * @return parsed double
     */
    private static Double getDoubleValue(final Object value) {
        return value == null ? 0 : Double.parseDouble(value.toString());
    }

    /**
     * Returns a integer that is the value of given value.
     * 
     * @param value value being parsed
     * @return parsed integer
     */
    private static Integer getIntegerValue(final Object value) {
        return value == null ? 0 : Integer.parseInt(value.toString());
    }

    /**
     * Returns true if this object is equal to the given object.
     * 
     * @param obj tested object
     * @return <code>true</code> if the two objects are equal, <code>false</code> otherwise
     */
    @Override
    public boolean equals(final Object obj) {
        return obj != null && this.hashCode() == obj.hashCode();
    }

    /**
     * Returns value's to string method.
     */
    @Override
    public String toString() {
        return value.toString();
    }

    /**
     * Returns hash code of the value property.
     * 
     * @return hash code
     */
    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
