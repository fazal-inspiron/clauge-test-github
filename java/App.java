import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Minimal Java HTTP server for quick API experiments.
 * Run: javac *.java && java App
 */
public class App {
  public static void main(String[] args) throws IOException {
    int port = 8080;
    HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

    server.createContext("/health", new HealthApi());
    server.createContext("/items", new ItemsApi());

    server.setExecutor(null);
    server.start();
    System.out.println("Java APIs listening on http://localhost:" + port);
  }
}
