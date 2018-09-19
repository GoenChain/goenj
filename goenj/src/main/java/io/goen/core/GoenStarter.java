package io.goen.core;

import io.goen.core.component.DefaultComponents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class GoenStarter {
    private static final Logger logger = LoggerFactory.getLogger("goen loader");

    public static void start() {
        logger.info("Starting GoenJ...");
        ApplicationContext context = new AnnotationConfigApplicationContext(DefaultComponents.class);
    }

    public static void start(Class selfDefindedConfig) {
        if (selfDefindedConfig == null) {
            start(new Class[]{DefaultComponents.class});
        } else {
            start(DefaultComponents.class, selfDefindedConfig);
        }
    }

    public static void start(Class... springConfigurations) {
        logger.info("Starting GoenJ...");
        ApplicationContext context = new AnnotationConfigApplicationContext(springConfigurations);
    }
}
