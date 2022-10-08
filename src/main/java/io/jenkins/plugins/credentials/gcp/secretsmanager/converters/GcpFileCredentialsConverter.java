package io.jenkins.plugins.credentials.gcp.secretsmanager.converters;

import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import io.jenkins.plugins.credentials.gcp.secretsmanager.CredentialsFactory.SecretBytesSupplier;
import io.jenkins.plugins.credentials.gcp.secretsmanager.GcpCredentialsConverter;
import io.jenkins.plugins.credentials.gcp.secretsmanager.GcpFileCredentials;
import io.jenkins.plugins.credentials.gcp.secretsmanager.Labels;
import io.jenkins.plugins.credentials.gcp.secretsmanager.SecretGetter;
import io.jenkins.plugins.credentials.gcp.secretsmanager.Type;
import java.util.Map;
import org.jenkinsci.plugins.variant.OptionalExtension;

@OptionalExtension(requirePlugins = {"plain-credentials"})
public class GcpFileCredentialsConverter extends GcpCredentialsConverter {

  @Override
  public boolean canResolve(String type) {
    return Type.FILE.equals(type);
  }

  @Override
  public BaseStandardCredentials resolve(
      String name, String description, Map<String, String> labels, SecretGetter secretGetter) {
    final String fileExtension = labels.getOrDefault(Labels.FILE_EXTENSION, "");
    String filename = labels.getOrDefault(Labels.FILENAME, "");

    if (!"".equals(fileExtension)) {
      filename = filename + "." + fileExtension;
    }

    return new GcpFileCredentials(
        name, description, filename, new SecretBytesSupplier(name, secretGetter));
  }
}
