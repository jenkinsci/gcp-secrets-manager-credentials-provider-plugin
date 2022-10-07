package io.jenkins.plugins.credentials.gcp.secretsmanager.converters;

import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import hudson.Extension;
import io.jenkins.plugins.credentials.gcp.secretsmanager.CredentialsFactory.SecretBytesSupplier;
import io.jenkins.plugins.credentials.gcp.secretsmanager.GcpCertificateCredentials;
import io.jenkins.plugins.credentials.gcp.secretsmanager.GcpCredentialsConverter;
import io.jenkins.plugins.credentials.gcp.secretsmanager.SecretGetter;
import io.jenkins.plugins.credentials.gcp.secretsmanager.Type;
import java.util.Map;

@Extension
public class GcpCertificateCredentialsConverter extends GcpCredentialsConverter {

  @Override
  public boolean canResolve(String type) {
    return Type.CERTIFICATE.equals(type);
  }

  @Override
  public BaseStandardCredentials resolve(
      String name, String description, Map<String, String> labels, SecretGetter secretGetter) {
    return new GcpCertificateCredentials(
        name, description, new SecretBytesSupplier(name, secretGetter));
  }
}
