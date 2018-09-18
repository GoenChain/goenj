package io.goen.core.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(CommonComponents.class)
public class DefaultComponents {
    private static Logger logger = LoggerFactory.getLogger("component");

    @Autowired
    CommonComponents commonComponents;
}
