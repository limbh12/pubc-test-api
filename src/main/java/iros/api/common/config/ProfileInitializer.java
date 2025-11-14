package iros.api.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Spring Profile 부트스트랩 초기화기.
 * <p>
 * 기존 Spring MVC 환경에서는 {@code application.properties}의 {@code spring.profiles.active}
 * 값이 자동으로 적용되지 않기 때문에, 초기화 시점에 직접 읽어와 활성 프로파일을 설정한다.
 * </p>
 */
public class ProfileInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger logger = LoggerFactory.getLogger(ProfileInitializer.class);
    private static final String PROFILE_PROPERTY_KEY = "spring.profiles.active";
    private static final String PROPERTY_LOCATION = "config/application.properties";

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();

        if (environment.getActiveProfiles().length > 0) {
            logger.info("[ProfileInitializer] Active profiles already set: {}", (Object) environment.getActiveProfiles());
            return;
        }

        String profileFromProperties = loadProfileFromProperties();
        if (StringUtils.hasText(profileFromProperties)) {
            environment.setActiveProfiles(StringUtils.commaDelimitedListToStringArray(profileFromProperties));
            logger.info("[ProfileInitializer] Applied active profile(s) from {}: {}", PROPERTY_LOCATION, profileFromProperties);
        } else {
            logger.warn("[ProfileInitializer] No active profile configured. Falling back to StandardEnvironment defaults.");
        }
    }

    private String loadProfileFromProperties() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(PROPERTY_LOCATION)) {
            if (inputStream == null) {
                logger.warn("[ProfileInitializer] Could not find {} on classpath", PROPERTY_LOCATION);
                return null;
            }

            Properties properties = new Properties();
            properties.load(inputStream);
            return properties.getProperty(PROFILE_PROPERTY_KEY);
        } catch (IOException e) {
            logger.error("[ProfileInitializer] Failed to load {}", PROPERTY_LOCATION, e);
            return null;
        }
    }
}
