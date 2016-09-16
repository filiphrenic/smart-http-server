package hr.fer.zemris.webserver;

import hr.fer.zemris.custom.scripting.exec.SmartScriptEngine;
import hr.fer.zemris.custom.scripting.nodes.DocumentNode;
import hr.fer.zemris.custom.scripting.parser.SmartScriptParser;
import hr.fer.zemris.util.IOUtil;
import hr.fer.zemris.util.NumUtil;
import hr.fer.zemris.webserver.RequestContext.RCCookie;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class is used as a http server that can run smart scripts (.smscr) and all sorts of things.
 * 
 * @author Filip Hrenić
 * @version 1.0
 */
public class SmartHttpServer {

    private String address;
    private int port;
    private int workerThreads;
    private long sessionTimeout;

    private final Map<String, String> mimeTypes = new HashMap<>();
    private final Map<String, IWebWorker> workersMap = new HashMap<>();

    private final Map<String, SessionMapEntry> sessions = new HashMap<>();
    private final Random sessionRandom = new Random();

    private GarbageThread garbageThread;
    private ServerThread serverThread;
    private ExecutorService threadPool;
    private Path documentRoot;

    /**
     * Main method that starts the server.
     * 
     * @param args only one argument is expected: path to server.properties file
     * @throws IOException
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Must provide a single argument: path to server.properties.");
        }

        final SmartHttpServer server = new SmartHttpServer(args[0]);
        server.start();

        System.out.println("To shutdown the server, simply type 'stop' and press enter.");
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        while (true) {
            String line;
            try {
                line = r.readLine();
            } catch (IOException e) {
                throw new RuntimeException();
            }
            if (line.equalsIgnoreCase("stop")) {
                System.out.println("Server was shutdown.");
                System.exit(1);
                // break;
            }
        }

    }

    /**
     * Creates a new {@link SmartHttpServer} with given config file
     * 
     * @param configFileName path to the config file
     * @throws IOException
     */
    private SmartHttpServer(String configFileName) {
        Properties properties = new Properties();
        try {
            properties.load(Files.newInputStream(Paths.get(configFileName)));
        } catch (IOException e) {
            throw new RuntimeException("Couldn't load server config.");
        }

        readProperties(properties);
    }

    /**
     * Reads server config properties file and initializes the fileds with those properties.
     * 
     * @param properties properties file
     */
    private void readProperties(Properties properties) {
        address = properties.getProperty(ADDRESS);
        port = NumUtil.getInt(properties.getProperty(PORT));
        workerThreads = NumUtil.getInt(properties.getProperty(WORKER_THREADS));
        sessionTimeout = (long) NumUtil.getInt(properties.getProperty(TIMEOUT));
        documentRoot = Paths.get(properties.getProperty(DOCUMENT_ROOT));

        final Path mimeConfig = Paths.get(properties.getProperty(MIME_CONFIG));
        Properties mimeProperties = new Properties();
        try {
            mimeProperties.load(Files.newInputStream(mimeConfig));
        } catch (IOException e) {
            throw new RuntimeException("Couldn't load mimes config.");
        }

        for (Entry<Object, Object> e : mimeProperties.entrySet()) {
            mimeTypes.put(e.getKey().toString(), e.getValue().toString());
        }

        readWorkerConfig(Paths.get(properties.getProperty(WORKERS)));
    }

    /**
     * Reads workers.config and stores tham into workers map.
     * 
     * @param workersPath path of the workers.config file
     */
    private void readWorkerConfig(Path workersPath) {

        List<String> duplicateCheck = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(Files.newBufferedReader(workersPath, StandardCharsets.UTF_8));
            List<String> lines = IOUtil.readFromReader(reader);
            reader.close();

            for (String l : lines) {
                final String line = l.trim();
                if (duplicateCheck.contains(line)) {
                    throw new RuntimeException("Workers config can't contain same workers.");
                }
                duplicateCheck.add(line);

                if (!line.startsWith("#") && !line.isEmpty()) {
                    final String[] args = line.split("\\s+=\\s+");
                    if (args.length != 2) {
                        throw new RuntimeException("Wrong worker.config file format.");
                    }

                    final String path = args[0];
                    final String fqcn = args[1];

                    workersMap.put(path, getWorkerByName(fqcn));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while reading worker config.");
        }

    }

    /**
     * Starts the server. Creates a new server thread (if not already created) and starts it
     */
    private synchronized void start() {
        threadPool = Executors.newFixedThreadPool(workerThreads);

        if (serverThread == null) {
            serverThread = new ServerThread();
            serverThread.setDaemon(true);
        }
        if (garbageThread == null) {
            garbageThread = new GarbageThread();
            garbageThread.setDaemon(true);
        }

        if (!garbageThread.isAlive()) {
            garbageThread.start();
        }
        if (!serverThread.isAlive()) {
            serverThread.start();
        }
    }

    /**
     * Stops this server. Stops the server thread and shuts down the thread pool.
     */
    protected synchronized void stop() {
        serverThread.kill();
        threadPool.shutdown();
    }

    /**
     * This thread is used to periodically check and remove expired sessions.
     * 
     * @author Filip Hrenić
     * @version 1.0
     */
    private class GarbageThread extends Thread {

        @Override
        public void run() {
            //noinspection InfiniteLoopStatement
            while (true) {

                // do something (remove expired sessions)
                final Set<String> sids = new HashSet<>(sessions.keySet());
                for (String sid : sids) {
                    SessionMapEntry entry = sessions.get(sid);
                    final long currTime = new Date().getTime() / 1000;
                    if (entry.validUntil < currTime) {
                        sessions.remove(sid);
                    }
                }

                // sleep five minutes
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                }

            }
        }
    }

    /**
     * This class is used as a thread that server runs to accept clients.
     * 
     * @author Filip Hrenić
     * @version 1.0
     */
    class ServerThread extends Thread {

        private boolean finished;

        @Override
        public void run() {
            finished = false;
            try {
                ServerSocket serverSocket = new ServerSocket(port);

                while (true) {
                    Socket client = serverSocket.accept();
                    if (finished) {
                        break;
                    }
                    ClientWorker cw = new ClientWorker(client);
                    threadPool.submit(cw);
                }

                serverSocket.close();
            } catch (IOException e) {
                throw new RuntimeException("Can't open a server socket on port " + port + "");
            }
        }

        /**
         * Makes this thread to shutdown.
         */
        public synchronized void kill() {
            finished = true;
        }
    }

    /**
     * This class represents a client on this server. It has a single public method that declares what to do when some
     * client connects.
     * 
     * @author Filip Hrenić
     * @version 1.0
     */
    private class ClientWorker implements Runnable {

        private final Socket csocket;
        private PushbackInputStream istream;
        private OutputStream ostream;
        private String version;
        private String method;

        private final Map<String, String> params = new HashMap<>();
        private Map<String, String> permParams = null;
        private final List<RCCookie> outputCookies = new ArrayList<>();

        /**
         * Creates a new {@link ClientWorker} that works with given client socket.
         * 
         * @param csocket client's socket
         */
        public ClientWorker(Socket csocket) {
            this.csocket = csocket;
        }

        @Override
        public void run() {

            // read clients request
            List<String> request;
            try {
                istream = new PushbackInputStream(csocket.getInputStream());
                ostream = csocket.getOutputStream();
                request = readRequest();
            } catch (IOException e) {
                throw new RuntimeException("Exception occured while reading client's request.");
            }

            // check if the first line is ok
            String[] firstLine = request.get(0).split(" ");
            if (firstLine.length != 3) {
                sendErrorStatusCode(BAD_REQUEST);
                return;
            }
            method = firstLine[0];
            String requestedPath = firstLine[1];
            version = firstLine[2];

            if (!method.equals("GET") || !(version.equals("HTTP/1.0") || version.equals("HTTP/1.1"))) {
                sendErrorStatusCode(BAD_REQUEST);
                return;
            }

            // check the session
            checkSession(request);

            // get absolute path resolved to the document root path
            String path = extractPath(requestedPath);
            Path reqPath = Paths.get(documentRoot + path);
            if (!reqPath.startsWith(documentRoot)) {
                sendErrorStatusCode(FORBIDDEN);
                return;
            }

            RequestContext rc = new RequestContext(ostream, params, permParams, outputCookies);

            if (path.startsWith("/ext/")) {
                final String workerName = path.substring(5); // 4 is index of '/'
                getWorkerByName(WORKERS_PACKAGE + "" + workerName).processRequest(rc);
                return;
            }

            final String potentialWorker = "/" + reqPath.getFileName().toString();
            IWebWorker iww = workersMap.get(potentialWorker);
            if (iww != null) {
                iww.processRequest(rc);
                return;
            }

            // at this point it should be a file
            final boolean exists = Files.exists(reqPath);
            final boolean isFile = !Files.isDirectory(reqPath);
            final boolean isRead = Files.isReadable(reqPath);

            if (!(exists && isFile && isRead)) {
                sendErrorStatusCode(404);
                return;
            }

            final Path file = reqPath.getFileName();
            final String fileName = file.toString();
            final String extension = fileName.substring(fileName.lastIndexOf('.') + 1);

            String mime = mimeTypes.get(extension);
            if (mime == null) {
                mime = DEFAULT_MIME_TYPE;
            }

            rc.setStatusCode(OK_STATUS);

            if (extension.equals("smscr")) {
                new SmartScriptEngine(getDocNode(reqPath), rc).execute();
            } else {
                rc.setMimeType(mime);
                try {
                    byte[] data = Files.readAllBytes(reqPath);
                    rc.write(data);
                } catch (IOException e) {
                    throw new RuntimeException("Error while writing to output stream or reading file.");
                }
            }

            closeSocket();
        }

        /**
         * Checks this session.
         * 
         * @param headerLines lines of the request that was sent
         */
        private synchronized void checkSession(List<String> headerLines) {
            String sidCandidate = null;

            for (String s : headerLines) {
                if (s.startsWith("Cookie:")) {
                    String[] cookies = s.replaceFirst("Cookie:\\s+", "").split(";");
                    for (String c : cookies) {
                        String[] cookie = c.split("=");

                        String cookieName = cookie[0];
                        String cookieValue = cookie[1].replace("\"", "");

                        if (cookieName.equals("sid")) {
                            sidCandidate = cookieValue;
                        }
                    }
                }
            }

            SessionMapEntry entry;

            if (sidCandidate == null || (entry = sessions.get(sidCandidate)) == null) {
                entry = createCookie();
            } else {
                long currTime = new Date().getTime() / 1000;
                entry.setValidUntil(currTime + sessionTimeout);
            }

            permParams = entry.getMap();
        }

        /**
         * Creates a new new session entry for this session.
         * 
         * @return session entry
         */
        private SessionMapEntry createCookie() {
            String newSid = ""; // generate new SID
            for (int i = 0; i < SID_LEN; i++) {
                newSid += ALPHABET.charAt(sessionRandom.nextInt(ALPHABET.length()));
            }

            long validUntil = (new Date().getTime() / 1000) + sessionTimeout;

            SessionMapEntry entry = new SessionMapEntry(newSid, validUntil);
            sessions.put(newSid, entry);

            RCCookie cookie = new RCCookie("sid", newSid, null, address, "/");
            cookie.setHttpOnly(true);
            outputCookies.add(cookie);

            return entry;
        }

        /**
         * Splits the given request to check if it has some parameters with it.
         * 
         * @param request request that was sent
         * @return extracted path
         */
        private String extractPath(String request) {
            final int indexOfSplit = request.indexOf('?');
            String path;
            if (indexOfSplit == -1) {
                path = request;
            } else {
                path = request.substring(0, indexOfSplit);
                parseParameters(request.substring(indexOfSplit + 1));
            }
            return path;
        }

        /**
         * Used to parse given paramString as multiple parameters. Puts parsed parameters to the 'params' map of
         * paramters.
         * 
         * @param paramString string to parse
         */
        private synchronized void parseParameters(String paramString) {
            String[] parameters = paramString.split("&");
            for (String s : parameters) {
                String[] sSplit = s.split("=");
                if (sSplit.length != 2) {
                    throw new RuntimeException("Wrong parameter format: " + s);
                }
                params.put(sSplit[0], sSplit[1]);
            }
        }

        /**
         * Closes the client socket.
         */
        private void closeSocket() {
            try {
                csocket.close();
            } catch (IOException e) {
                closeSocket();
            }
        }

        /**
         * Sends some status code to the client.
         * 
         * @param statusCode status code to send
         */
        private void sendErrorStatusCode(int statusCode) {
            String statusText = "";
            statusText += version + " " + statusCode;
            switch (statusCode) {
                case BAD_REQUEST:
                    statusText += " Bad Request";
                    break;
                case FORBIDDEN:
                    statusText += " Forbidden";
                    break;
                case FILE_NOT_FOUND:
                    statusText += " File Not Found";
                    break;
                default:
                    statusText += " Unknown Error";
            }
            statusText += "\n";

            RequestContext rc = new RequestContext(ostream, params, permParams, outputCookies);
            rc.setStatusCode(statusCode);
            rc.setStatusText(statusText);
            try {
                rc.write(statusText);
            } catch (IOException e) {
                throw new RuntimeException("Unexpected error ocurred while giving the client some content.");
            }

            closeSocket();
        }

        /**
         * Returns a list of lines read from the request.
         * 
         * @return list of lines
         * @throws IOException
         */
        private List<String> readRequest() throws IOException {
            BufferedReader r = new BufferedReader(new InputStreamReader(new BufferedInputStream(istream),
                    StandardCharsets.ISO_8859_1));
            return IOUtil.readFromReader(r);
        }

        /**
         * Gets the document node of the given script.
         * 
         * @param path file name of the given script to run
         * @return document node of the given script
         * @throws IOException
         */
        private DocumentNode getDocNode(final Path path) {
            List<String> documentBody;
            try {
                documentBody = Files.readAllLines(path, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException("Couldn't read script: " + path.getFileName());
            }

            String doc = "";
            for (String s : documentBody) {
                doc += s + "\r\n";
            }

            return new SmartScriptParser(doc).getDocumentNode();
        }
    }

    /**
     * Returns a wanted {@link IWebWorker} by it's name from package hr.fer.zemris.java.webserver.workers.
     * 
     * @param fqcn fully qualified class name
     * @return worker
     */
    private IWebWorker getWorkerByName(String fqcn) {
        Object newObject;
        try {
            Class<?> referenceToClass = this.getClass().getClassLoader().loadClass(fqcn);
            newObject = referenceToClass.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("There isn't a class named: " + fqcn);
        }
        return (IWebWorker) newObject;
    }

    /**
     * This class is used to represent one session.
     * 
     * @author Filip Hrenić
     * @version 1.0
     */
    private static class SessionMapEntry {
        @SuppressWarnings("unused")
        String sid;
        long validUntil;
        final Map<String, String> map = new ConcurrentHashMap<>();

        /**
         * @param sid session id
         * @param validUntil expiration time
         */
        public SessionMapEntry(String sid, long validUntil) {
            this.sid = sid;
            this.validUntil = validUntil;
        }

        /**
         * Sets the valid-until parameter to the given parameter.
         * 
         * @param newValidUntil parameter to set
         */
        public void setValidUntil(long newValidUntil) {
            validUntil = newValidUntil;
        }

        /**
         * Returns this entry's map.
         * 
         * @return map
         */
        public Map<String, String> getMap() {
            return map;
        }

    }

    private static final String ADDRESS = "server.address";
    private static final String PORT = "server.port";
    private static final String WORKER_THREADS = "server.workerThreads";
    private static final String DOCUMENT_ROOT = "server.documentRoot";
    private static final String MIME_CONFIG = "server.mimeConfig";
    private static final String TIMEOUT = "session.timeout";
    private static final String WORKERS = "server.workers";

    private static final String DEFAULT_MIME_TYPE = "application/octet-stream";

    private static final String WORKERS_PACKAGE = "hr.fer.zemris.java.webserver.workers";

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVXYZ";
    private static final int SID_LEN = 20;

    private static final int OK_STATUS = 200;
    private static final int BAD_REQUEST = 400;
    private static final int FORBIDDEN = 403;
    private static final int FILE_NOT_FOUND = 404;
}
