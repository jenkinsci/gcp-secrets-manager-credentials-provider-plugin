package io.jenkins.plugins.credentials.gcp.secretsmanager.converters;

import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import io.jenkins.plugins.credentials.gcp.secretsmanager.CredentialsFactory.SecretSupplier;
import io.jenkins.plugins.credentials.gcp.secretsmanager.GcpCredentialsConverter;
import io.jenkins.plugins.credentials.gcp.secretsmanager.GcpSshUserPrivateKey;
import io.jenkins.plugins.credentials.gcp.secretsmanager.Labels;
import io.jenkins.plugins.credentials.gcp.secretsmanager.SecretGetter;
import io.jenkins.plugins.credentials.gcp.secretsmanager.Type;
import java.util.Map;
import org.jenkinsci.plugins.variant.OptionalExtension;

@OptionalExtension(requirePlugins = {"ssh-credentials"})
public class GcpSshUserPrivateKeyConverter extends GcpCredentialsConverter {

  @Override
  public boolean canResolve(String type) {
    return Type.SSH_USER_PRIVATE_KEY.equals(type);
  }

  @Override
  public BaseStandardCredentials resolve(
      String name, String description, Map<String, String> labels, SecretGetter secretGetter) {
    final String username = labels.getOrDefault(Labels.USERNAME, "");

    return new GcpSshUserPrivateKey(
        name, description, new SecretSupplier(name, secretGetter), username);
  }
}
