package hr.fer.zemris.webserver.workers;

import java.io.IOException;
import java.util.Set;

import hr.fer.zemris.webserver.IWebWorker;
import hr.fer.zemris.webserver.RequestContext;

public class EchoParams implements IWebWorker {

    /**
     * Creates a html table of given parameters.
     */
    @Override
    public void processRequest(RequestContext context) {

        String html = "<html><body>";
        Set<String> params = context.getParameterNames();
        if (params.size() != 0) {
            html += "<h1>Parameters</h1><table border=\"1\" style=\"width:300px;text-align:center\">";
            html += "<tr><th>" + "Parameter key</th><th>Parameter</th></tr>";
            for (String paramKey : params) {
                html += "<tr><td>" + paramKey + "</td><td>" + context.getParameter(paramKey) + "</td></tr>";
            }
            html += "</table>";
        } else {
            html += "<h3>No parameters</h3>";
        }
        html += "</body></html>";

        try {
            context.write(html);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
