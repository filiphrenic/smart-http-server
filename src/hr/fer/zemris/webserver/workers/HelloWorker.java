package hr.fer.zemris.webserver.workers;

import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.Date;
import hr.fer.zemris.webserver.IWebWorker;
import hr.fer.zemris.webserver.RequestContext;

public class HelloWorker implements IWebWorker {

    /**
     * Creates a html document that has current time and a message depending on whether a parameter name was given or
     * not.
     */
    @Override
    public void processRequest(RequestContext context) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();

        context.setMimeType("text/html");
        String name = context.getParameter("name");

        try {
            context.write("<html><body>");
            context.write("<h1>Hello!!!</h1>");
            context.write("<p>Now is: " + sdf.format(now) + "</p>");

            if (name == null || name.trim().isEmpty()) {
                context.write("<p>You did not send me your name!</p>");
            } else {
                context.write("<p>Your name has " + name.trim().length() + " letters.</p>");
            }
            context.write("</body></html>");
        } catch (IOException ex) {
            // Log exception to servers log...
            ex.printStackTrace();
        }
    }
}
