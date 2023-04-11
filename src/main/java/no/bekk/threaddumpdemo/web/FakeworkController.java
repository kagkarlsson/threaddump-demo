package no.bekk.threaddumpdemo.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CountDownLatch;

import static no.bekk.threaddumpdemo.config.Config.MAX_POOL_SIZE;

@RestController
@RequestMapping("/fakework")
public class FakeworkController {
    private static final Logger LOG = LoggerFactory.getLogger(FakeworkController.class);
    private static final String LONG_STRING = "asdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdf";
    private JdbcTemplate jdbc;
    private TransactionTemplate tx;
    private HttpClient httpClient;
    private final Object lock = new Object();

    public FakeworkController(JdbcTemplate jdbc, TransactionTemplate tx, HttpClient httpClient) {
        this.jdbc = jdbc;
        this.tx = tx;
        this.httpClient = httpClient;
    }

    @GetMapping(path = "database_read")
    public void database_read() {
        jdbc.execute("select pg_sleep(20)");
    }

    @GetMapping(path = "tcp_connect")
    public void tcp_connect() throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("203.0.113.1", 80), 20_000);
        }
    }

    @GetMapping(path = "http_client_get")
    public void http_client_get() throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://httpstat.us/200?sleep=20000"))
                .GET()
                .build();

        httpClient.send(request, HttpResponse.BodyHandlers.discarding());
    }

    @GetMapping(path = "db_pool_get_connection")
    public void db_pool_get_connection() throws InterruptedException {
        CountDownLatch connectionCheckouts = new CountDownLatch(2);

        // Checkout all available connections in other threads
        for (int i = 0; i < MAX_POOL_SIZE; i++) {
            new Thread(() -> {
                tx.executeWithoutResult(status -> {
                    connectionCheckouts.countDown();
                    jdbc.execute("select pg_sleep(20)");
                });
            }).start();
        }

        // Make sure the other connections are checked out from the pool
        connectionCheckouts.await();
        // This should hang as all connections
        jdbc.execute("select pg_sleep(20)");
    }

    @GetMapping(path = "lock_contention")
    public void lock_contention() throws InterruptedException {
        CountDownLatch locksHeld = new CountDownLatch(1);

        // Get another thread to hold the lock for some time
        new Thread(() -> {
            synchronized (lock) {
                locksHeld.countDown();
                Utils.sleep(20_000);
            }
        }).start();

        // Make sure the lock is held by other thread
        locksHeld.await();
        // This should hang until sleep above is done
        synchronized (lock) {
            LOG.info("Finally got lock!");
        }
    }

    @GetMapping(path = "cpu_loop")
    public void cpu() throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - 20_000 < start) {
            // let's just hash a String...
            md.digest(LONG_STRING.getBytes());
        }
    }
}
