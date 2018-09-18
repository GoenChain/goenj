package io.goen.core.component;

import io.goen.core.GoenConfig;
import io.goen.net.p2p.P2PServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = "io.goen")
public class CommonComponents {
    private static final Logger logger = LoggerFactory.getLogger("component");
    private static CommonComponents instance;

    @Autowired
    P2PServer p2PServer;


    @Bean
    public GoenConfig systemGoenConfig() {
        return GoenConfig.getSystem();
    }


}
