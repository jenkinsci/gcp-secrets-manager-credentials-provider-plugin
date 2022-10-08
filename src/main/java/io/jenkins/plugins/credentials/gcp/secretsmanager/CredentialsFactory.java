package io.jenkins.plugins.credentials.gcp.secretsmanager;

import com.cloudbees.plugins.credentials.SecretBytes;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import hudson.util.Secret;
import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CredentialsFactory {

  private static final Logger LOGGER = Logger.getLogger(CredentialsFactory.class.getName());

  public static Optional<StandardCredentials> create(
      String name, String project, Map<String, String> labels, SecretGetter secretGetter) {
    final String description = name + " (" + project + ")";
    final String type = labels.getOrDefault(Labels.TYPE, "");

    LOGGER.log(Level.FINE, "Checking GCP secret: " + name);

    GcpCredentialsConverter lookup = GcpCredentialsConverter.lookup(type);

    if (lookup != null) {
      try {
        return Optional.of(lookup.resolve(name, description, labels, secretGetter));
      } catch (Exception ex) {
        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.log(Level.FINE, "Failed to convert Secret");
        } else {
          LOGGER.log(Level.WARNING, String.format("Failed to convert Secret %s of type %s", name, type), ex);
        }
      }
    }

    return Optional.empty();
  }

  public static class SecretBytesSupplier implements Supplier<SecretBytes>, Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final SecretGetter secretGetter;

    public SecretBytesSupplier(String id, SecretGetter secretGetter) {
      this.id = id;
      this.secretGetter = secretGetter;
    }

    @Override
    public SecretBytes get() {
      return SecretBytes.fromBytes(secretGetter.getSecretBytes(id));
    }
  }

  public static class SecretSupplier implements Supplier<Secret>, Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final SecretGetter secretGetter;

    public SecretSupplier(String id, SecretGetter secretGetter) {
      this.id = id;
      this.secretGetter = secretGetter;
    }

    @Override
    public Secret get() {
      return Secret.fromString(secretGetter.getSecretString(id));
    }
  }
}
