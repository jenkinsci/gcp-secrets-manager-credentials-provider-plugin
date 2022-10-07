package io.jenkins.plugins.credentials.gcp.secretsmanager;

import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.Secret;
import io.jenkins.plugins.credentials.gcp.secretsmanager.config.Messages;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class GcpSshUserPrivateKey extends BaseStandardCredentials implements SSHUserPrivateKey {
  private static final Secret NO_PASSPHRASE = Secret.fromString("");

  private final Supplier<Secret> privateKey;
  private final String username;

  public GcpSshUserPrivateKey(
      String id, String description, Supplier<Secret> privateKey, String username) {
    super(id, description);
    this.privateKey = privateKey;
    this.username = username;
  }

  @NonNull
  @Override
  public List<String> getPrivateKeys() {
    final String privateKeyText = privateKey.get().getPlainText();
    return Collections.singletonList(privateKeyText);
  }

  @NonNull
  @Override
  public String getPrivateKey() {
    return privateKey.get().getPlainText();
  }

  @Override
  public Secret getPassphrase() {
    return NO_PASSPHRASE;
  }

  @NonNull
  @Override
  public String getUsername() {
    return username;
  }

  @Extension
  @SuppressWarnings("unused")
  public static class DescriptorImpl extends BaseStandardCredentialsDescriptor {
    @Override
    @Nonnull
    public String getDisplayName() {
      return Messages.sshUserPrivateKey();
    }

    @Override
    public String getIconClassName() {
      return "icon-fingerprint";
    }
  }
}
