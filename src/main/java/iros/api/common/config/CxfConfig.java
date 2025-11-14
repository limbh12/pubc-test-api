package iros.api.common.config;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import iros.api.facility.controller.CultureFacilityImprovedRestController;
import iros.api.facility.controller.CultureFacilityRestController;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

/**
 * Apache CXF JAX-RS 설정 (Java Config)
 * 
 * <p>프로파일별로 다른 REST 컨트롤러를 활성화합니다:</p>
 * <ul>
 *   <li>mock: CultureFacilityRestController (표준형)</li>
 *   <li>improved: CultureFacilityImprovedRestController (개선형)</li>
 * </ul>
 *
 * @author PUBC Test API
 * @version 1.0
 */
@Configuration
public class CxfConfig {

    private static final Logger logger = LoggerFactory.getLogger(CxfConfig.class);

    @Autowired(required = false)
    private SpringBus cxf;


    @PostConstruct
    public void onInit() {
        logger.info("[CXF] CxfConfig initialized - bus available: {}", cxf != null);
    }

    /**
     * JAX-RS Server - Mock Profile (표준형)
     */
    @Bean
    @Profile("mock")
    public Server restApiServerMock(CultureFacilityRestController cultureFacilityRestController) {
        logger.info("[CXF] Initializing REST server for mock profile");
        return createServer(cultureFacilityRestController);
    }

    /**
     * JAX-RS Server - Improved Profile (개선형)
     */
    @Bean
    @Profile("improved")
    public Server restApiServerImproved(CultureFacilityImprovedRestController cultureFacilityImprovedRestController) {
        logger.info("[CXF] Initializing REST server for improved profile");
        return createServer(cultureFacilityImprovedRestController);
    }

    private Server createServer(Object serviceBean) {
        JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
        if (cxf != null) {
            factory.setBus(cxf);
        }
        factory.setAddress("/");
        factory.setServiceBeans(Collections.singletonList(serviceBean));
        factory.setProviders(Collections.singletonList(jacksonJaxbJsonProvider()));
        Server server = factory.create();
        logger.info("[CXF] Registered JAX-RS service: {}", serviceBean.getClass().getSimpleName());
        return server;
    }

    @Bean
    public JacksonJaxbJsonProvider jacksonJaxbJsonProvider() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        provider.setMapper(mapper);
        return provider;
    }
}
