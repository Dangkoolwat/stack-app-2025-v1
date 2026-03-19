package com.daangcool.stack.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.introspect.AnnotatedMember;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;

@Configuration
public class JacksonConfiguration {

    /**
     * Spring Boot 4 / Jackson 3 공통 설정
     * API와 Cache에서 동일한 ObjectMapper 설정을 유지하기 위해 Customizer를 사용합니다.
     * Jackson 3(tools.jackson)은 ObjectMapper가 불변(Immutable)이므로 Builder를 통해 설정해야 합니다.
     * 
     * [Note] 현재 Jackson 3.1.0 생태계에서도 어노테이션(jackson-annotations)은 Jackson 2 패키지(com.fasterxml.jackson.annotation)를 
     * 브리지로 사용하므로, 도메인 엔티티들의 어노테이션은 구 패키지를 그대로 유지하는 것이 표준입니다.
     */

    @Bean
    public JsonMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            // 캐성 재구성 시 @JsonIgnore를 무시하여 데이터 무결성을 확보하기 위한 introspector 커스텀
            // 주의: API 응답 결과에도 영향을 줄 수 있으므로 신중히 적용됨.
            builder.annotationIntrospector(new JacksonAnnotationIntrospector() {
                @Override
                public boolean hasIgnoreMarker(tools.jackson.databind.cfg.MapperConfig<?> config, AnnotatedMember m) {
                    // 모든 IgnoreMarker 무시 (캐시 직렬화/역직렬화 안정성 확보)
                    return false;
                }
            });

            // Hibernate MixIns 등록
            builder.addMixIn(org.hibernate.collection.spi.PersistentSet.class, HibernateSetMixIn.class);
            builder.addMixIn(org.hibernate.collection.spi.PersistentBag.class, HibernateBagMixIn.class);
        };
    }

    // MixIn definition (Shared)
    @com.fasterxml.jackson.annotation.JsonTypeInfo(use = com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NONE)
    @tools.jackson.databind.annotation.JsonDeserialize(as = java.util.HashSet.class)
    abstract static class HibernateSetMixIn {}

    @com.fasterxml.jackson.annotation.JsonTypeInfo(use = com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NONE)
    @tools.jackson.databind.annotation.JsonDeserialize(as = java.util.ArrayList.class)
    abstract static class HibernateBagMixIn {}
}
