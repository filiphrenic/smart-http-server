package hr.fer.zemris.webserver;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is used to send HTTP response from the sever to the client.
 * 
 * @author Filip Hrenić
 * @version 1.0
 */
public class RequestContext {

    /** Line separator. */
    private static final String LINE_SEP = "\r\n";

    private OutputStream outputStream;
    private Charset charset;

    private String encoding = "UTF-8";
    private int statusCode = 200;
    private String statusText = "OK";
    private String mimeType = "text/html";

    private Map<String, String> parameters;
    private Map<String, String> temporaryParameters = new HashMap<>();
    private Map<String, String> persistentParameters;
    private List<RequestContext.RCCookie> outputCookies;

    private boolean headerGenerated = false;

    /**
     * Creates a new {@link RequestContext} with given parameters.
     * 
     * @param outputStream can't be null, used for writing context
     * @param parameters parameters stored within this context, can be null
     * @param persistentParameters persistent parameters stored within this context, can be null
     * @param outputCookies cookies that will be added to the header of this context, can be null
     */
    public RequestContext(final OutputStream outputStream, final Map<String, String> parameters,
            final Map<String, String> persistentParameters, final List<RequestContext.RCCookie> outputCookies) {
        if (outputStream == null) {
            throw new IllegalArgumentException("Output stream can't be null.");
        }

        this.outputStream = outputStream;
        this.parameters = (parameters == null ? new HashMap<>() : parameters);
        this.persistentParameters = (persistentParameters == null ? new HashMap<>()
                : persistentParameters);
        this.outputCookies = (outputCookies == null ? new ArrayList<>() : outputCookies);
    }

    /**
     * @param data data you want to write to the output stream
     * @return this request context
     * @throws IOException if there was a problem with writing data to output stream
     */
    public RequestContext write(final String data) throws IOException {
        if (charset == null) {
            charset = Charset.forName(encoding);
        }
        return write(data.getBytes(charset));
    }

    /**
     * @param data data you want to write to the output stream
     * @return this request context
     * @throws IOException if there was a problem with writing data to output stream
     */
    public RequestContext write(final byte[] data) throws IOException {
        if (!headerGenerated) {
            outputStream.write(generateHeader());
        }

        outputStream.write(data);
        return this;
    }

    /**
     * Generates the header for this request context.
     * 
     * @return byte representation of the header in <code>ISO_8859_1</code> encoding
     */
    private byte[] generateHeader() {

        String header = "";

        header += "HTTP/1.1 " + statusCode + " " + statusText + LINE_SEP;

        header += "Content-type: " + mimeType;
        if (mimeType.startsWith("text/")) {
            header += "; charset=" + encoding;
        }
        header += LINE_SEP;

        for (RCCookie cookie : outputCookies) {
            header += "Set-Cookie: " + cookie + LINE_SEP;
        }
        header += LINE_SEP;

        charset = Charset.forName(encoding);
        headerGenerated = true;

        return header.getBytes(StandardCharsets.ISO_8859_1);

    }

    /**
     * @param cookie cookie to add to output cookies
     */
    public void addRCCookie(final RCCookie cookie) {
        outputCookies.add(cookie);
    }

    // --------------------------------- PARAMETERS --------------------------------------- //

    /**
     * @param name parameter name
     * @return parameter value
     */
    public String getParameter(final String name) {
        return parameters.get(name);
    }

    /**
     * @return parameter names
     */
    public Set<String> getParameterNames() {
        return parameters.keySet();
    }

    /**
     * @return the parameters
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    // ---------------------------- PERSISTENT PARAMETERS ---------------------------------- //

    /**
     * @return the persistentParameters
     */
    public Map<String, String> getPersistentParameters() {
        return persistentParameters;
    }

    /**
     * @param name persistent parameter name
     * @return parameters value
     */
    public String getPersistentParameter(final String name) {
        return persistentParameters.get(name);
    }

    /**
     * @return persistent paraeter names
     */
    public Set<String> getPersistentParameterNames() {
        return persistentParameters.keySet();
    }

    /**
     * @param name name of the persistent parameter
     * @param value it's value
     */
    public void setPersistentParameter(final String name, final String value) {
        persistentParameters.put(name, value);
    }

    /**
     * @param name removes persistent parameter with given name
     */
    public void removePersistentParameter(final String name) {
        persistentParameters.remove(name);
    }

    /**
     * @param persistentParameters the persistentParameters to set
     */
    public void setPersistentParameters(final Map<String, String> persistentParameters) {
        this.persistentParameters = persistentParameters;
    }

    // ---------------------------- TEMPORARY PARAMETERS ---------------------------------- //

    /**
     * @return the temporaryParameters
     */
    public Map<String, String> getTemporaryParameters() {
        return temporaryParameters;
    }

    /**
     * @param name temporary parameter name
     * @return parameters value
     */
    public String getTemporaryParameter(final String name) {
        return temporaryParameters.get(name);
    }

    /**
     * @return temporary parameter names
     */
    public Set<String> getTemporaryParameterNames() {
        return temporaryParameters.keySet();
    }

    /**
     * @param name name of the temporary parameter
     * @param value it's value
     */
    public void setTemporaryParameter(final String name, final String value) {
        temporaryParameters.put(name, value);
    }

    /**
     * @param name removes temporary parameter with given name
     */
    public void removeTemporaryParameter(final String name) {
        temporaryParameters.remove(name);
    }

    /**
     * @param temporaryParameters the temporaryParameters to set
     */
    public void setTemporaryParameters(final Map<String, String> temporaryParameters) {
        this.temporaryParameters = temporaryParameters;
    }

    // ----------------------------------------------------------------------------------- //

    /**
     * @param encoding the encoding to set
     */
    public void setEncoding(final String encoding) {
        checkIfHeaderWasGenerated("encoding");
        this.encoding = encoding;
    }

    /**
     * @param statusCode the status to set
     */
    public void setStatusCode(final int statusCode) {
        checkIfHeaderWasGenerated("status code");
        this.statusCode = statusCode;
    }

    /**
     * @param statusText the statusText to set
     */
    public void setStatusText(final String statusText) {
        checkIfHeaderWasGenerated("status text");
        this.statusText = statusText;
    }

    /**
     * @param mimeType the mimeType to set
     */
    public void setMimeType(final String mimeType) {
        checkIfHeaderWasGenerated("mime");
        this.mimeType = mimeType;
    }

    /**
     * Checks if the header was already created. If it was, than an {@link RuntimeException} is thrown.
     * 
     * @param nameOfTheProperty name of the property that someone tried to change
     */
    private void checkIfHeaderWasGenerated(final String nameOfTheProperty) {
        if (headerGenerated) {
            throw new RuntimeException("You can't change '" + nameOfTheProperty + "' once the header has been created.");
        }
    }

    /**
     * Class used to represent a cookie.
     * 
     * @author Filip Hrenić
     * @version 1.0
     */
    public static class RCCookie {
        private final String name;
        private final String value;
        private final String domain;
        private final String path;
        private final Integer maxAge;
        private boolean httpOnly;

        /**
         * Creates a new {@link RCCookie} with wanted parameters.
         * 
         * @param name name of the cookie
         * @param value cookie's value
         * @param domain cookie's domain
         * @param path cookie's path
         * @param maxAge cookie's max age
         */
        public RCCookie(final String name, final String value, final Integer maxAge, final String domain,
                final String path) {
            this.name = name;
            this.value = value;
            this.domain = domain;
            this.path = path;
            this.maxAge = maxAge;
            httpOnly = false;
        }

        /**
         * Sets http only parameter.
         * 
         * @param httpOnly
         */
        public void setHttpOnly(boolean httpOnly) {
            this.httpOnly = httpOnly;
        }

        @Override
        public String toString() {
            String text = name + "=\"" + value + "\"";
            if (domain != null) {
                text += "; Domain=" + domain;
            }
            if (path != null) {
                text += "; Path=" + path;
            }
            if (maxAge != null) {
                text += "; Max-Age=" + maxAge;
            }
            if (httpOnly) {
                text += "; HttpOnly";
            }
            return text;
        }
    }
}
