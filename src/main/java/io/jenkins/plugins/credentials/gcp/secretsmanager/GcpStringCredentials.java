package io.jenkins.plugins.credentials.gcp.secretsmanager;

import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import hudson.Extension;
import hudson.util.Secret;
import io.jenkins.plugins.credentials.gcp.secretsmanager.config.Messages;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

public class GcpStringCredentials extends BaseStandardCredentials implements StringCredentials {

  private final Supplier<Secret> valueSupplier;

  public GcpStringCredentials(String id, Supplier<Secret> valueSupplier) {
    super(id, id);
    this.valueSupplier = valueSupplier;
  }

  @Nonnull
  @Override
  public Secret getSecret() {
    return valueSupplier.get();
  }

  @Extension
  @SuppressWarnings("unused")
  public static class DescriptorImpl extends BaseStandardCredentialsDescriptor {
    @Override
    @Nonnull
    public String getDisplayName() {
      return Messages.secretText();
    }
  }
}
