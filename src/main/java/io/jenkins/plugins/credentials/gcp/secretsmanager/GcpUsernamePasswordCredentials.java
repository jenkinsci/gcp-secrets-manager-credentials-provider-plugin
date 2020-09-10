package io.jenkins.plugins.credentials.gcp.secretsmanager;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.Secret;
import io.jenkins.plugins.credentials.gcp.secretsmanager.config.Messages;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class GcpUsernamePasswordCredentials extends BaseStandardCredentials
    implements StandardUsernamePasswordCredentials {

  private final Supplier<Secret> password;

  private final String username;

  public GcpUsernamePasswordCredentials(String id, Supplier<Secret> password, String username) {
    super(id, id);
    this.password = password;
    this.username = username;
  }

  @NonNull
  @Override
  public Secret getPassword() {
    return password.get();
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
      return Messages.usernamePassword();
    }

    @Override
    public String getIconClassName() {
      return "icon-credentials-userpass";
    }
  }
}
