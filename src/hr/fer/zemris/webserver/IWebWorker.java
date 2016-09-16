package hr.fer.zemris.webserver;

/**
 * Classes that implement this interface can process given {@link RequestContext} and creates contet for the client.
 * 
 * @author Filip Hrenić
 * @version 1.0
 */
public interface IWebWorker {

    /**
     * This method must be implemented in such way that it processes the context and creates some contet it returnes to
     * the client
     * 
     * @param context context to process
     */
    void processRequest(RequestContext context);

}
