package hr.fer.zemris.custom.scripting.exec;

import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class sort of implements {@link Map} but allows user to add mulitple values to a single key. Values are stored
 * in a stack-like collection.
 * 
 * @author Filip Hrenić
 * @version 1.0
 */
class ObjectMultistack {

    private final Map<String, MultistackEntry> map;

    /**
     * Creates a new {@link ObjectMultistack}.
     */
    public ObjectMultistack() {
        this.map = new HashMap<>();
    }

    /**
     * Pushes the {@link ValueWrapper} to the stack determined by parameter <code>name</code>
     * 
     * @param name determines which stack is used
     * @param valueWrapper this is pushed onto stack
     */
    public void push(String name, ValueWrapper valueWrapper) {
        MultistackEntry me = map.get(name);
        if (me == null) {
            me = new MultistackEntry();
        }
        me.push(valueWrapper);
        map.put(name, me);
    }

    /**
     * Pops a {@link ValueWrapper} from the stack determined by parameter <code>name</code>
     * 
     * @param name determines which stack is popped
     * @return {@link ValueWrapper} that was popped
     */
    public ValueWrapper pop(String name) {
        return checkIfExists(name).pop();
    }

    /**
     * Peeks the stack determined by parameter <code>name</code>
     * 
     * @param name determines which stack is peeked
     * @return {@link ValueWrapper} that was peeked
     */
    public ValueWrapper peek(String name) {
        return checkIfExists(name).peek();
    }

    /**
     * Tests if the stack determined by parameter <code>name</code> is empty.
     * 
     * @param name determines which stack is tested
     * @return <code>true</code> if is empty, <code>false</code> otherwise
     */
    public boolean isEmpty(String name) {
        return checkIfExists(name).isEmpty();
    }

    /**
     * Checks if {@link MultistackEntry} exitsts in map.
     * 
     * @param name determines which {@link MultistackEntry} is being checked
     */
    private MultistackEntry checkIfExists(String name) {
        MultistackEntry me = map.get(name);
        if (me == null) {
            throw new IllegalArgumentException("Key " + name + " wasn't mentioned until now.");
        }
        return me;
    }

    /**
     * My implementation of {@link java.util.Stack}.
     * 
     * @author Filip Hrenić
     * @version 1.0
     */
    private static class MultistackEntry {

        /**
         * Initial capacity of stack.
         */
        private static final int INITIAL_CAPACITY = 10;

        /**
         * An array that will be implemented as stack.
         */
        private ValueWrapper[] stack;
        /**
         * Number of elements stored in stack.
         */
        private int numOfElem;
        /**
         * Capacity of stack.
         */
        private int capacity;

        /**
         * Creates a new {@link MultistackEntry} with <code>INITIAL_CAPACITY=10</code>
         */
        public MultistackEntry() {
            this(INITIAL_CAPACITY);
        }

        /**
         * Creates a new {@link MultistackEntry} with wanted capacity.
         * 
         * @param capacity inital capacity
         */
        public MultistackEntry(int capacity) {
            this.capacity = capacity;
            stack = new ValueWrapper[capacity];
            numOfElem = 0;
        }

        /**
         * Pushes the given {@link ValueWrapper} to the stack.
         * 
         * @param valueWrapper added {@link ValueWrapper}
         */
        public void push(ValueWrapper valueWrapper) {
            if (numOfElem == capacity) {
                capacity *= 2;
                stack = Arrays.copyOf(stack, capacity);
            }

            stack[numOfElem++] = valueWrapper;
        }

        /**
         * Removes last {@link ValueWrapper} that was pushed onto stack.
         * 
         * @return popped {@link ValueWrapper}
         */
        public ValueWrapper pop() {
            if (numOfElem == 0) {
                throw new EmptyStackException();
            }

            numOfElem--;
            ValueWrapper popped = stack[numOfElem];
            stack[numOfElem] = null; // gc, you can't collect the popped element
            return popped;
        }

        /**
         * Returns last {@link ValueWrapper} pushed onto stack.
         * 
         * @return last {@link ValueWrapper}
         */
        public ValueWrapper peek() {
            if (numOfElem == 0) {
                throw new EmptyStackException();
            }

            return stack[numOfElem - 1];
        }

        /**
         * Tests if the stack is empty.
         * 
         * @return <code>true</code> if it is empty, <code>false</code> otherwise
         */
        public boolean isEmpty() {
            return numOfElem == 0;
        }
    }
}
