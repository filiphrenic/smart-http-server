package hr.fer.zemris.custom.scripting.parser;

import hr.fer.zemris.custom.collections.*;
import hr.fer.zemris.custom.scripting.nodes.*;
import hr.fer.zemris.custom.scripting.tokens.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * This class is used for parsing documents.
 * The whole document is stored into a {@link DocumentNode}.
 *
 * @author Filip Hrenić
 * @version 1.0
 */
public class SmartScriptParser {

    /*
     * constants
     * 
     * used for determining of what is currently parsed
     * 
     * by the status we know what is the status of the parsing, but also what should come next
     */

    private static final int STARTING = 0; // initial state

    private static final int TEXT_NODE = 1; // parsing of text

    private static final int ESCAPING = 2; // something will be escaped in text node

    private static final int OPEN_TAG_D = 3; // when a { comes, next should be $ to open the tag

    private static final int OPEN_TAG = 4; // a tag is opened, next should be the name of the node

    private static final int NAME_OF_NODE = 5; // reading the name of the node (for,=,end)

    private static final int CLOSED_TAG_D = 6; // we read $, next should be a } to close the tag

    private static final int FOR_NODE = 7; // building a forloop node

    private static final int FOR_START = 8; // start expression

    private static final int FOR_END = 9; // end expression

    private static final int FOR_STEP = 10; // step expression

    private static final int ECHO_NODE = 11; // building a echo node

    private static final int ECHO_FUNCTION = 12; // reading a function in echo node, started with @

    private static final int ECHO_VARIABLE = 13; // reading a variable in echo node, started with a letter

    private static final int ECHO_OPER_NUM = 14; // reading a operator or a number because - is a operator but can be a
    // start of a number

    private static final int ECHO_STRING = 15; // reading a string in the echo node

    private static final int ECHO_ESC_STRING = 16; // if something is escaped in the string inside of a echo node

    private static final int END_TAG = 17; // end tag

    // -----------------------------------------------------------------------------------------------------------------------

    private final String documentBody; // the main string from which we parse the document
    private DocumentNode documentNode; // a node in which we store the document structure
    private final StringBuilder sb; // used for creating nodes/tokens/tags
    private final ObjectStack stack; // needed to build a document structure
    private ArrayBackedIndexedCollection echoTokens; // used for storing tokens in the echo node

    /**
     * Constructs a new instance of the class. Starts the parsing.
     *
     * @param documentBody text which needs to be parsed
     */
    public SmartScriptParser(String documentBody) {

        this.documentBody = documentBody;
        this.stack = new ObjectStack();
        this.sb = new StringBuilder();
        this.parse();

    }

    /**
     * Getter method for the document node property.
     * Used when we want to do something with the document.
     *
     * @return a document node in which the whole text is stored
     */
    public DocumentNode getDocumentNode() {
        return this.documentNode;
    }

    /**
     * The main method used for parsing.
     * Creates a hierarchy of nodes/tags and stores it in the documentNode.
     *
     * @throws SmartScriptParserException if a error happens while parsing
     */
    private void parse() throws SmartScriptParserException {

        // creating a input stream from the given string
        InputStream iS = new ByteArrayInputStream(this.documentBody.getBytes(StandardCharsets.UTF_8));
        // creating a reader for reading the document
        Reader reader = new InputStreamReader(iS, StandardCharsets.UTF_8);

        documentNode = new DocumentNode();
        stack.push(documentNode);

        int status = STARTING; // starting of the parsing

        try {

            int item = reader.read();

            while (item != -1) { // when this is true, it means we read the whole document
                char c = (char) item; // character that is read

                // with this switch we determine what was the status of the last read character
                switch (status) {

                    case STARTING:
                        if (c == '{') {
                            status = OPEN_TAG_D;
                        } else if (c == '\\') {
                            sb.append(c);
                            status = ESCAPING;
                        } else {
                            status = TEXT_NODE;
                            sb.append(c);
                        }
                        break;

                    case TEXT_NODE:
                        if (c == '{') {
                            this.addTextNode();
                            status = OPEN_TAG_D;
                        } else if (c == '\\') {
                            status = ESCAPING;
                        } else {
                            sb.append(c);
                        }
                        break;

                    case ESCAPING:
                        if (c == 'r') {
                            sb.append('\r');
                        } else if (c == 'n') {
                            sb.append('\n');
                        } else if (c == 't') {
                            sb.append('\t');
                        }
                        status = TEXT_NODE;
                        break;

                    case OPEN_TAG_D:
                        if (c == '$') {
                            status = OPEN_TAG;
                        } else if (Character.isWhitespace(c)) {
                            // ignore
                        } else {
                            throw new SmartScriptParserException("After { must come a $ sign.");
                        }
                        break;

                    case OPEN_TAG:
                        if (Character.isWhitespace(c)) {
                            // ignore
                        } else if (c == '=') {
                            status = ECHO_NODE;
                        } else if (Character.isLetter(c)) {
                            sb.append(c);
                            status = NAME_OF_NODE;
                        } else {
                            throw new SmartScriptParserException(
                                    "Opened tag must have a name (for,end) or has to be empty (=).");
                        }
                        break;

                    case NAME_OF_NODE:
                        if (Character.isLetter(c)) {
                            sb.append(c);
                        } else if (c == ' ') {
                            String str = sb.toString().toLowerCase();
                            sb.setLength(0); // clearing the content of builder
                            switch (str) {
                                case "for":
                                    status = FOR_NODE;
                                    break;
                                case "end":
                                    this.resolveEndTag();
                                    status = END_TAG;
                                    break;
                                default:
                                    throw new SmartScriptParserException("Unknown command.");
                            }
                        } else if (c == '$') {
                            // popraviti
                            if (sb.toString().toLowerCase().equals("end")) {
                                sb.setLength(0); // clearing the content of builder
                                this.resolveEndTag();
                                status = CLOSED_TAG_D;
                            }
                        }
                        break;

                    case CLOSED_TAG_D:
                        if (Character.isWhitespace(c)) {
                            // ignore
                        } else if (c == '}') {
                            status = TEXT_NODE;
                        } else {
                            throw new SmartScriptParserException("Missing $} combination.");
                        }
                        break;

                    case FOR_NODE:
                        if (Character.isWhitespace(c)) {
                            if (this.whitespaceOrEmptyBuilder()) {
                                // ignore because we want to read a token
                            } else {
                                // we already have something in builder
                                sb.append(c); // we put a ' ' to seperate the tokens in the builder so we can later
                                // split the string with it
                                status = FOR_START;
                            }
                        } else {
                            sb.append(c); // building a variable
                        }
                        break;

                    case FOR_START:
                        if (Character.isWhitespace(c)) {
                            if (this.whitespaceOrEmptyBuilder()) {
                                // ignore because we want to read a token
                            } else {
                                // we already have something in builder
                                sb.append(c); // we put a ' ' to seperate the tokens in the builder so we can later
                                // split the string with it
                                status = FOR_END;
                            }
                        } else {
                            sb.append(c); // building a start expressiona
                        }
                        break;

                    case FOR_END:
                        if (Character.isWhitespace(c)) {
                            if (this.whitespaceOrEmptyBuilder()) {
                                // ignore because we want to read a token
                            } else {
                                // we already have something in builder
                                sb.append(c); // we put a ' ' to seperate the tokens in the builder so we can later
                                // split the string with it
                                status = FOR_STEP;
                            }
                        } else {
                            sb.append(c); // building a end expressiona
                        }
                        break;

                    case FOR_STEP:
                        if (Character.isWhitespace(c)) {
                            if (this.whitespaceOrEmptyBuilder()) {
                                // ignoring because we don't have a step expression yet
                            } else {
                                this.addForLoopNode();
                            }
                        } else if (c == '$') {
                            if (sb.length() != 0) { // checking if we haven't created a node yet
                                this.addForLoopNode();
                            }
                            status = CLOSED_TAG_D;
                        } else {
                            sb.append(c); // building a step expressiona
                        }
                        break;

                    case ECHO_NODE:
                        if (Character.isWhitespace(c)) {
                            // ignore
                        } else if (c == '$') {
                            this.addEchoNode();
                            status = CLOSED_TAG_D;
                        } else {
                            sb.append(c);
                            if (c == '@') {
                                status = ECHO_FUNCTION;
                            } else if (Character.isLetter(c)) {
                                status = ECHO_VARIABLE;
                            } else if (c == '+' || c == '-' || c == '*' || c == '/' || Character.isDigit(c)) {
                                status = ECHO_OPER_NUM;
                            } else if (c == '"') {
                                status = ECHO_STRING;
                            }

                        }
                        break;

                    case ECHO_FUNCTION:
                    case ECHO_VARIABLE:
                    case ECHO_OPER_NUM:
                        if (Character.isWhitespace(c)) {
                            this.addEchoToken();
                            status = ECHO_NODE;
                        } else if (c == '$') {
                            this.addEchoToken();
                            this.addEchoNode();
                            status = CLOSED_TAG_D;
                        } else {
                            sb.append(c);
                        }
                        break;

                    case ECHO_STRING:
                        if (c == '"') {
                            sb.append(c);
                            this.addEchoToken();
                            status = ECHO_NODE;
                        } else if (c == '$') {
                            this.addEchoToken();
                            this.addEchoNode();
                            status = CLOSED_TAG_D;
                        } else if (c == '\\') {
                            status = ECHO_ESC_STRING;
                        } else {
                            sb.append(c);
                        }
                        break;

                    case ECHO_ESC_STRING:
                        if (c == 'r') {
                            sb.append('\r');
                        } else if (c == 'n') {
                            sb.append('\n');
                        } else if (c == 't') {
                            sb.append('\t');
                        }
                        status = ECHO_STRING;
                        break;

                } // the end of the switch statement

                item = reader.read();
            }

            reader.close();

            if (status == TEXT_NODE) {
                this.addTextNode();
            }

            if (this.stack.size() != 1) {
                throw new SmartScriptParserException("Missing a few END tags.");
            } else {
                this.stack.pop(); // poping the document node
            }

        } catch (Exception e) {
            throw new SmartScriptParserException(e.getMessage());
        }

    }

    // ------------------------------------------------------------------------------------------------------------------------

    /*
     * Adding nodes/tags.
     */

    /**
     * Adds a text node as a child as a current node that's on top of the stack.
     * Clears the builder.
     */
    private void addTextNode() {

        // add a node to the last node that was pushed onto stack
        ((Node) stack.peek()).addChildNode(new TextNode(sb.toString()));

        sb.setLength(0); // clearing the builder

    }

    /**
     * Tries to pop the for node from the stack because end tag should always be preceded with a for loop node.
     *
     * @throws SmartScriptParserException if there are more end tags than for loop nodes in the text
     */
    private void resolveEndTag() {
        if (this.stack.size() == 1) {
            throw new SmartScriptParserException("Too much end tags.");
        } else {
            this.stack.pop();
        }
    }

    /**
     * Adds a for loop node as a child to the current top of stack and then pushes the for loop node to the stack
     */
    private void addForLoopNode() {

        // splitting the elements out from the builder (variable, start, end, step? expression)
        String[] elements = sb.toString().split(" ");

        Token[] fToken = new Token[4];

        for (int i = 0; i < elements.length; i++) {
            fToken[i] = makeNewToken(elements[i]);
        }

        ForLoopNode fln = new ForLoopNode((TokenVariable) fToken[0], fToken[1], fToken[2], fToken[3]);

        // adding a for loop node as a child of the current top of stack
        ((Node) this.stack.peek()).addChildNode(fln);

        // pushing this for loop node to the stack so I can add other nodes/tags to that node
        this.stack.push(fln);

        sb.setLength(0); // clearing the builder

    }

    /**
     * Adding a echo node to the current top of stack.
     */
    private void addEchoNode() {

        Token[] tokens = new Token[echoTokens.size()];
        for (int i = 0; i < echoTokens.size(); i++) {
            tokens[i] = (Token) echoTokens.get(i);
        }

        echoTokens.clear(); // deleting the echo tokens from the array

        EchoNode en = new EchoNode(tokens);

        ((Node) this.stack.peek()).addChildNode(en);

    }

    /*
     * Helper methods.
     */

    /**
     * Adding a echo token to the array of echo tokens
     */
    private void addEchoToken() {
        if (this.echoTokens == null) this.echoTokens = new ArrayBackedIndexedCollection();
        this.echoTokens.add(this.makeNewToken(sb.toString()));
        sb.setLength(0); // clearing the builder
    }

    /**
     * Determines if the last character that is in the builder is a whitespace or if the builder is emtpy
     *
     * @return true if builder is empty or the last character in builder is a whitespace
     */
    private boolean whitespaceOrEmptyBuilder() {
        return this.sb.length() == 0 || Character.isWhitespace(this.sb.charAt(this.sb.length() - 1));
    }

    /**
     * Method used for testing if a variable/function name is valid.<br>
     * A name is valid if it starts with a letter which can be followed with letters, numbers or underscores.
     *
     * @param name name which we want to check
     * @return true if it is valid
     */
    private boolean isValidName(String name) {
        if (!Character.isLetter(name.charAt(0))) return false;
        for (int i = 1; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c != '_' && !Character.isLetterOrDigit(c)) return false;
        }
        return true;
    }

    /**
     * Method used for testing if the string can be parsed into a integer.
     *
     * @param value string we want to check
     * @return true if it can be parsed into a integer
     */
    private boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Method used for testing if the string can be parsed into a double.
     *
     * @param value string we want to check
     * @return true if it can be parsed into a double
     */
    private boolean isDouble(String value) {
        try {
            Double.parseDouble(value);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * A method that is used for determining which token is created.<br>
     * If the name is a operator (+,-,*,/) a {@link TokenOperator} is created.<br>
     * If the name starts with a <code>@</code> a {@link TokenFunction} is created if the name is valid.<br>
     * If the name is a valid variable name, a {@link TokenVariable} is created.<br>
     * If the name can be parsed to a integer, a {@link TokenConstantInteger} is created.<br>
     * If the name can be parsed to a double, a {@link TokenConstantDouble} is created.<br>
     * If the name ends and starts with a <code>"</code>, a {@link TokenConstantString} is created.<br>
     *
     * @param s name we want to check
     * @return a new Token (can be any kind of Token)
     * @throws SmartScriptParserException if the function name is not valid or if it is a unknown data
     */
    private Token makeNewToken(String s) {

        if (s.equals("+") || s.equals("-") || s.equals("*") || s.equals("/")) {
            return new TokenOperator(s); // creating TokenOperator
        } else if (s.startsWith("@")) {
            if (this.isValidName(s.substring(1))) {
                return new TokenFunction(s.substring(1)); // creating TokenFunction
            } else {
                throw new SmartScriptParserException("Function name is not valid."); // name contains illegal characters
            }
        } else if (this.isValidName(s)) {
            return new TokenVariable(s); // creating TokenVariable
        } else if (this.isInteger(s)) {
            return new TokenConstantInteger(Integer.parseInt(s)); // creating TokenConstantInteger
        } else if (this.isDouble(s)) {
            return new TokenConstantDouble(Double.parseDouble(s)); // creating TokenConstantDouble
        } else if (s.startsWith("\"") && s.endsWith("\"")) {
            return new TokenConstantString(s.substring(1, s.length() - 1)); // creating TokenString
        } else {
            throw new SmartScriptParserException("Unknown data."); // unkwon token
        }
    }

}
