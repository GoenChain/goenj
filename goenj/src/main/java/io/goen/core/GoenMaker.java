package io.goen.core;

import io.goen.core.component.DefaultComponents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class GoenMaker {
    private static final Logger logger = LoggerFactory.getLogger("goen loader");

    public static Goen createGoen() {
        logger.info("Starting GoenJ...");
        ApplicationContext context = new AnnotationConfigApplicationContext(DefaultComponents.class);
        return context.getBean(Goen.class);
    }

    public static Goen createGoen(Class selfDefindedConfig) {
        return selfDefindedConfig == null ? createGoen(new Class[]{DefaultComponents.class}) :
                createGoen(DefaultComponents.class, selfDefindedConfig);
    }

    public static Goen createGoen(Class... springConfigurations) {
        logger.info("Starting GoenJ...");
        ApplicationContext context = new AnnotationConfigApplicationContext(springConfigurations);
//        ((AnnotationConfigApplicationContext) context).setAllowBeanDefinitionOverriding(true);
        return context.getBean(Goen.class);
    }
}
