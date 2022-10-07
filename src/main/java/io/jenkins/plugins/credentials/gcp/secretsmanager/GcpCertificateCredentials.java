package io.jenkins.plugins.credentials.gcp.secretsmanager;

import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.SecretBytes;
import com.cloudbees.plugins.credentials.common.CertificateCredentials;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.Secret;
import io.jenkins.plugins.credentials.gcp.secretsmanager.config.Messages;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class GcpCertificateCredentials extends BaseStandardCredentials
    implements CertificateCredentials {

  private static final char[] NO_PASSWORD = {};
  private static final Secret NO_SECRET = Secret.fromString("");

  private final Supplier<SecretBytes> keyStore;

  public GcpCertificateCredentials(String id, String description, Supplier<SecretBytes> keyStore) {
    super(id, description);
    this.keyStore = keyStore;
  }

  @NonNull
  @Override
  public KeyStore getKeyStore() {
    final SecretBytes secretBytes = keyStore.get();

    try (InputStream stream = new ByteArrayInputStream(secretBytes.getPlainData())) {
      final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      keyStore.load(stream, NO_PASSWORD);
      return keyStore;
    } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException ex) {
      throw new CredentialsUnavailableException(
          "keyStore", Messages.couldNotRetrieveCredentialError(), ex);
    }
  }

  @NonNull
  @Override
  public Secret getPassword() {
    return NO_SECRET;
  }

  @Extension
  @SuppressWarnings("unused")
  public static class DescriptorImpl extends BaseStandardCredentialsDescriptor {
    @Override
    @Nonnull
    public String getDisplayName() {
      return Messages.certificate();
    }

    @Override
    public String getIconClassName() {
      return "icon-credentials-certificate";
    }
  }
}
