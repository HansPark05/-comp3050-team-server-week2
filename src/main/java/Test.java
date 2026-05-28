import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;
import comp3050.server.LoginHandler;
import comp3050.server.LogoutHandler;

public class Test {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        server.createContext("/test", new MyHandler());
        server.createContext("/hello", new HelloHandler());
        server.createContext("/move", new MoveHandler());
        server.createContext("/info", new InfoHandler());
        server.createContext("/login",  new LoginHandler());
        server.createContext("/logout", new LogoutHandler());
        server.createContext("/take",  new TakeHandler());
        server.createContext("/place", new PlaceHandler());
        server.createContext("/use",   new UseHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8000...");
    }
}
