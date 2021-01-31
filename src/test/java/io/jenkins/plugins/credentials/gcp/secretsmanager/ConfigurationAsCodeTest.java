package io.jenkins.plugins.credentials.gcp.secretsmanager;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import io.jenkins.plugins.credentials.gcp.secretsmanager.config.PluginConfiguration;
import org.junit.Rule;
import org.junit.Test;

public class ConfigurationAsCodeTest {

  @Rule public JenkinsConfiguredWithCodeRule r = new JenkinsConfiguredWithCodeRule();

  @Test
  @ConfiguredWithCode("configuration-as-code.yml")
  public void should_support_configuration_as_code() throws Exception {
    PluginConfiguration configuration =
        (PluginConfiguration) r.jenkins.getDescriptor(PluginConfiguration.class);
    assertThat(configuration.getProject()).isEqualTo("gcp-project");
    assertThat(configuration.getFilter().getLabel()).isEqualTo("my-label");
    assertThat(configuration.getFilter().getValue()).isEqualTo("my-value");
  }
}
