package io.goen.net.p2p;

import com.typesafe.config.ConfigFactory;
import io.goen.core.GoenConfig;
import io.goen.core.GoenStarter;
import org.junit.Test;
import org.springframework.context.annotation.Bean;

public class P2PServerTest {

    private static class ServerConfig {

        private final String config = "server.conf";

        /**
         * Instead of supplying properties via config file for the peer
         * we are substituting the corresponding bean which returns required
         * config for this instance.
         */
        @Bean
        public GoenConfig systemGoenConfig() {
            GoenConfig props = new GoenConfig();
            props.overrideParams(ConfigFactory.parseResources(config));
            return props;
        }
    }

    private static class ClientConfig {

        private final String config = "client.conf";

        /**
         * Instead of supplying properties via config file for the peer
         * we are substituting the corresponding bean which returns required
         * config for this instance.
         */
        @Bean
        public GoenConfig systemGoenConfig() {
            GoenConfig props = new GoenConfig();
            props.overrideParams(ConfigFactory.parseResources(config));
            return props;
        }
    }

    @Test
    public void testP2PServer() {
        Thread sThread = new Thread(() -> {
            GoenStarter.start(ServerConfig.class);
        });
        sThread.start();

        Thread cThread = new Thread(() -> {
            GoenStarter.start(ClientConfig.class);
        });

        cThread.start();

        try {
            sThread.join();
            cThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
