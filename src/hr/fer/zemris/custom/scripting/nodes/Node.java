package hr.fer.zemris.custom.scripting.nodes;

import hr.fer.zemris.custom.collections.ArrayBackedIndexedCollection;

/**
 * This class is the base class for all graph nodes.
 * 
 * @author Filip Hrenić
 * @version 1.0
 */
public abstract class Node {

    private ArrayBackedIndexedCollection children;

    /**
     * Method adds given child to an internally managed collection of children.
     * 
     * @param child a node object that is added
     */
    public void addChildNode(Node child) {
        if (children == null) {
            children = new ArrayBackedIndexedCollection();
        }

        children.add(child);
    }

    /**
     * Returns how many children does a given node have.
     * 
     * @return number of (direct) children
     */
    public int numberOfChildren() {
        if (this.children == null) return 0;
        return this.children.size();
    }

    /**
     * Gets a child at a given index.
     * 
     * @param index index at which we want to get the object
     * @return child node at given index
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater that number of children
     */
    public Node getChild(int index) {
        if (this.children == null) return null;
        if (index < 0 || index >= this.numberOfChildren()) throw new IndexOutOfBoundsException("Invalid index.");
        return (Node) this.children.get(index);
    }

    /**
     * Given visitor visits this node.
     * 
     * @param visitor
     */
    public abstract void accept(final INodeVisitor visitor);

    /**
     * Returns an empty string.
     * 
     * @return an empty string.
     */
    String asText() {
        return "";
    }
}
