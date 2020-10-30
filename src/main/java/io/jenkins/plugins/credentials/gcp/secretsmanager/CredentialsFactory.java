package io.jenkins.plugins.credentials.gcp.secretsmanager;

import com.cloudbees.plugins.credentials.SecretBytes;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import hudson.util.Secret;
import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class CredentialsFactory {

  public static Optional<StandardCredentials> create(
      String name, Map<String, String> labels, SecretGetter secretGetter) {
    final String type = labels.getOrDefault(Labels.TYPE, "");
    final String username = labels.getOrDefault(Labels.USERNAME, "");
    final String fileExtension = labels.getOrDefault(Labels.FILE_EXTENSION, "");
    String filename = labels.getOrDefault(Labels.FILENAME, "");

    if (!"".equals(fileExtension)) {
      filename = filename + "." + fileExtension;
    }

    switch (type) {
      case Type.STRING:
        return Optional.of(new GcpStringCredentials(name, new SecretSupplier(name, secretGetter)));
      case Type.FILE:
        return Optional.of(
            new GcpFileCredentials(name, filename, new SecretBytesSupplier(name, secretGetter)));
      case Type.USERNAME_PASSWORD:
        return Optional.of(
            new GcpUsernamePasswordCredentials(
                name, new SecretSupplier(name, secretGetter), username));
      case Type.SSH_USER_PRIVATE_KEY:
        return Optional.of(
            new GcpSshUserPrivateKey(name, new SecretSupplier(name, secretGetter), username));
      case Type.CERTIFICATE:
        return Optional.of(
            new GcpCertificateCredentials(name, new SecretBytesSupplier(name, secretGetter)));
      default:
        return Optional.empty();
    }
  }

  private static class SecretBytesSupplier implements Supplier<SecretBytes>, Serializable {

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

  private static class SecretSupplier implements Supplier<Secret>, Serializable {

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
