package org.springframework.samples.petclinic.testlocal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Component
public class ValueInjectionTest {

	@Value("${server.port:8080}")
	private String serverPort;

	@Value("#{environment.getProperty('java.version')}")
	private String javaVersion;

	@DatabaseUrl
	private String databaseUrl;

	private final String appName;

	public ValueInjectionTest(@Value("${spring.application.name}") String appName) {
		this.appName = appName;
	}

	private String profile;

	private String customConfig;

	public void setProfile(@Value("${spring.profiles.active:default}") String profile) {
		this.profile = profile;
	}

	public void setCustomConfig(@TimeoutValue Integer timeout) {
		this.customConfig = "timeout-" + timeout;
	}

	public void configure(@Value("${app.version}") String version, @Value("${app.build.number}") Integer buildNumber) {
	}

	@Bean
	@Value("${bean.value.property:bean-default}")
	public String beanWithValue() {
		return "bean from @Value";
	}

	@Component
	public static class InnerComponent {

		@Value("${inner.component.value}")
		private String innerValue;

		@DatabaseUrl
		private String innerDbUrl;

		public InnerComponent(@DatabaseUrl String param) {
		}

	}

	@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@Value("${database.url:jdbc:h2:mem:testdb}")
	public @interface DatabaseUrl {

	}

	@Target({ ElementType.FIELD, ElementType.PARAMETER })
	@Retention(RetentionPolicy.RUNTIME)
	@Value("${app.timeout:5000}")
	public @interface TimeoutValue {

	}

	public String getServerPort() {
		return serverPort;
	}

	public String getJavaVersion() {
		return javaVersion;
	}

	public String getDatabaseUrl() {
		return databaseUrl;
	}

	public String getAppName() {
		return appName;
	}

	public String getProfile() {
		return profile;
	}

	public String getCustomConfig() {
		return customConfig;
	}

}
