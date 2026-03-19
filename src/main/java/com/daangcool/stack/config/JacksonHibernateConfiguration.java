package com.daangcool.stack.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.datatype.hibernate7.Hibernate7Module;
import tools.jackson.datatype.hibernate7.Hibernate7Module.Feature;

@Configuration
public class JacksonHibernateConfiguration {

    /*
     * Hibernate 엔티티 직렬화 지원 (Jackson 3)
     */
    @Bean
    public Hibernate7Module hibernate7Module() {
        return new Hibernate7Module().configure(Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS, true);
    }
}
