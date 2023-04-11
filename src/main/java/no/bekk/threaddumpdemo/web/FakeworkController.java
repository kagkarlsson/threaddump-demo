package no.bekk.threaddumpdemo.web;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

@RestController
@RequestMapping("/fakework")
public class FakeworkController {
    private JdbcTemplate jdbc;

    public FakeworkController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping(path = "database")
    public void database() {
        jdbc.execute("select pg_sleep(20)");
    }

    @GetMapping(path = "tcp_connect")
    public void tcp_connect() throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("203.0.113.1", 80), 30_000);
        }
    }

}
