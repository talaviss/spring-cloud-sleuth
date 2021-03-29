/*
 * Copyright 2013-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.sleuth.autoconfig;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

/**
 * Adds default properties for the application:
 * <ul>
 * <li>logging pattern level that prints trace information (e.g. trace ids)</li>
 * </ul>
 *
 * @author Dave Syer
 * @author Marcin Grzejszczak
 * @since 2.0.0
 * @deprecated This type should have never been public and will be hidden or removed in
 * 3.0
 */
@Deprecated
public class TraceEnvironmentPostProcessor implements EnvironmentPostProcessor {

	private static final String PROPERTY_SOURCE_NAME = "defaultProperties";

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment,
			SpringApplication application) {
		Map<String, Object> map = new HashMap<String, Object>();
		// This doesn't work with all logging systems but it's a useful default so you see
		// traces in logs without having to configure it.
		if (sleuthEnabled(environment)
				&& sleuthDefaultLoggingPatternEnabled(environment)) {
			map.put("logging.pattern.level", "%5p [${spring.zipkin.service.name:"
					+ "${spring.application.name:}},%X{X-B3-TraceId:-},%X{X-B3-SpanId:-},%X{X-Span-Export:-}]");
		}
		addOrReplace(environment.getPropertySources(), map);
	}

	private boolean sleuthEnabled(ConfigurableEnvironment environment) {
		return Boolean
				.parseBoolean(environment.getProperty("spring.sleuth.enabled", "true"));
	}

	private boolean sleuthDefaultLoggingPatternEnabled(
			ConfigurableEnvironment environment) {
		return Boolean.parseBoolean(environment
				.getProperty("spring.sleuth.default-logging-pattern-enabled", "true"));
	}

	private void addOrReplace(MutablePropertySources propertySources,
			Map<String, Object> map) {
		MapPropertySource target = null;
		if (propertySources.contains(PROPERTY_SOURCE_NAME)) {
			PropertySource<?> source = propertySources.get(PROPERTY_SOURCE_NAME);
			if (source instanceof MapPropertySource) {
				target = (MapPropertySource) source;
				for (String key : map.keySet()) {
					if (!target.containsProperty(key)) {
						target.getSource().put(key, map.get(key));
					}
				}
			}
		}
		if (target == null) {
			target = new MapPropertySource(PROPERTY_SOURCE_NAME, map);
		}
		if (!propertySources.contains(PROPERTY_SOURCE_NAME)) {
			propertySources.addLast(target);
		}
	}

}
