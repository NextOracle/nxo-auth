package org.nextoracle;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Minimal smoke test – verifies the library compiles and its key classes are loadable
 * without needing a real datasource or full application context.
 */
class NxoAuthApplicationTests {

    @Test
    void autoConfigurationClassIsLoadable() throws Exception {
        Class<?> clazz = Class.forName("org.nextoracle.NxoAuthAutoConfiguration");
        assertThat(clazz).isNotNull();
        assertThat(clazz.getPackageName()).isEqualTo("org.nextoracle");
    }
}
