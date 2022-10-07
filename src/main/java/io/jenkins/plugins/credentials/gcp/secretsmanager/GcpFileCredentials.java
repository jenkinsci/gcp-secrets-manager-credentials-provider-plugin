package io.jenkins.plugins.credentials.gcp.secretsmanager;

import com.cloudbees.plugins.credentials.SecretBytes;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import hudson.Extension;
import io.jenkins.plugins.credentials.gcp.secretsmanager.config.Messages;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import org.jenkinsci.plugins.plaincredentials.FileCredentials;

public class GcpFileCredentials extends BaseStandardCredentials implements FileCredentials {

  private final String filename;

  private final Supplier<SecretBytes> content;

  public GcpFileCredentials(
      String id, String description, String filename, Supplier<SecretBytes> content) {
    super(id, description);
    this.filename = filename;
    this.content = content;
  }

  @Nonnull
  @Override
  public String getFileName() {
    return filename;
  }

  @Nonnull
  @Override
  public InputStream getContent() {
    final SecretBytes sb = content.get();
    return new ByteArrayInputStream(sb.getPlainData());
  }

  @Extension
  @SuppressWarnings("unused")
  public static class DescriptorImpl extends BaseStandardCredentialsDescriptor {
    @Override
    @Nonnull
    public String getDisplayName() {
      return Messages.file();
    }
  }
}
