package hr.fer.zemris.custom.scripting.demo;

import hr.fer.zemris.custom.scripting.exec.SmartScriptEngine;
import hr.fer.zemris.custom.scripting.parser.SmartScriptParser;
import hr.fer.zemris.webserver.RequestContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DemoSmartScriptEngine {

    @SuppressWarnings("unused")
    private static void fibonnaci(String fileName) throws IOException {
        String doc = ucitaj(fileName);

        Map<String, String> parameters = new HashMap<>();
        Map<String, String> persistentParameters = new HashMap<>();
        List<RequestContext.RCCookie> cookies = new ArrayList<>();
        // create engine and execute it
        new SmartScriptEngine(new SmartScriptParser(doc).getDocumentNode(), new RequestContext(System.out, parameters,
                persistentParameters, cookies)).execute();

    }

    @SuppressWarnings("unused")
    private static void brojPoziva(String fileName) throws IOException {
        String doc = ucitaj(fileName);

        Map<String, String> parameters = new HashMap<>();
        Map<String, String> persistentParameters = new HashMap<>();
        List<RequestContext.RCCookie> cookies = new ArrayList<>();
        persistentParameters.put("brojPoziva", "3");
        RequestContext rc = new RequestContext(System.out, parameters, persistentParameters, cookies);
        new SmartScriptEngine(new SmartScriptParser(doc).getDocumentNode(), rc).execute();
        System.out.println("Vrijednost u mapi: " + rc.getPersistentParameter("brojPoziva"));

    }

    @SuppressWarnings("unused")
    private static void osnovni(String fileName) throws IOException {

        String doc = ucitaj(fileName);

        Map<String, String> parameters = new HashMap<>();
        Map<String, String> persistentParameters = new HashMap<>();
        List<RequestContext.RCCookie> cookies = new ArrayList<>();
        // put some parameter into parameters map
        parameters.put("broj", "4");
        // create engine and execute it
        new SmartScriptEngine(new SmartScriptParser(doc).getDocumentNode(), new RequestContext(System.out, parameters,
                persistentParameters, cookies)).execute();
    }

    @SuppressWarnings("unused")
    private static void zbrajanje(String fileName) throws IOException {

        String doc = ucitaj(fileName);

        Map<String, String> parameters = new HashMap<>();
        Map<String, String> persistentParameters = new HashMap<>();
        List<RequestContext.RCCookie> cookies = new ArrayList<>();
        parameters.put("a", "4");
        parameters.put("b", "2");
        // create engine and execute it
        new SmartScriptEngine(new SmartScriptParser(doc).getDocumentNode(), new RequestContext(System.out, parameters,
                persistentParameters, cookies)).execute();
    }

    private static String ucitaj(String fileName) throws IOException {
        List<String> documentBody = Files.readAllLines(Paths.get("webroot/scripts/" + fileName), StandardCharsets.UTF_8);
        String doc = "";
        for (String s : documentBody) {
            doc += s + "\r\n";
        }
        return doc;
    }

}
