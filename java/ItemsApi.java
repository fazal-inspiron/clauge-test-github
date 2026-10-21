import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Minimal in-memory items API.
 * GET  /items — list
 * POST /items — create (body: {"name":"..."})
 */
public class ItemsApi implements HttpHandler {
  private static final List<Item> items = new ArrayList<>();
  private static final AtomicInteger nextId = new AtomicInteger(1);

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    String method = exchange.getRequestMethod();

    if ("GET".equalsIgnoreCase(method)) {
      listItems(exchange);
    } else if ("POST".equalsIgnoreCase(method)) {
      createItem(exchange);
    } else {
      exchange.sendResponseHeaders(405, -1);
    }
  }

  private void listItems(HttpExchange exchange) throws IOException {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < items.size(); i++) {
      if (i > 0) sb.append(",");
      Item item = items.get(i);
      sb.append("{\"id\":").append(item.id)
        .append(",\"name\":\"").append(escape(item.name)).append("\"}");
    }
    sb.append("]");
    writeJson(exchange, 200, sb.toString());
  }

  private void createItem(HttpExchange exchange) throws IOException {
    String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    String name = extractName(body);
    Item item = new Item(nextId.getAndIncrement(), name);
    items.add(item);
    String json = "{\"id\":" + item.id + ",\"name\":\"" + escape(item.name) + "\"}";
    writeJson(exchange, 201, json);
  }

  private static String extractName(String body) {
    // Minimal parse: look for "name":"..."
    int key = body.indexOf("\"name\"");
    if (key < 0) return "";
    int colon = body.indexOf(':', key);
    int start = body.indexOf('"', colon + 1);
    int end = body.indexOf('"', start + 1);
    if (start < 0 || end < 0) return "";
    return body.substring(start + 1, end);
  }

  private static String escape(String s) {
    return s.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  private static void writeJson(HttpExchange exchange, int status, String json) throws IOException {
    byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type", "application/json");
    exchange.sendResponseHeaders(status, bytes.length);
    try (OutputStream os = exchange.getResponseBody()) {
      os.write(bytes);
    }
  }

  static class Item {
    final int id;
    final String name;

    Item(int id, String name) {
      this.id = id;
      this.name = name;
    }
  }
}
