package io.jenkins.plugins.credentials.gcp.secretsmanager;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.CredentialsStoreAction;
import com.cloudbees.plugins.credentials.domains.Domain;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.model.ModelObject;
import hudson.security.ACL;
import hudson.security.Permission;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;
import org.jenkins.ui.icon.Icon;
import org.jenkins.ui.icon.IconSet;
import org.jenkins.ui.icon.IconType;
import org.kohsuke.stapler.export.ExportedBean;

public class GcpCredentialsStore extends CredentialsStore {

  private final GcpCredentialsProvider provider;
  private final GcpCredentialsStoreAction action = new GcpCredentialsStoreAction(this);

  public GcpCredentialsStore(GcpCredentialsProvider provider) {
    super(GcpCredentialsProvider.class);
    this.provider = provider;
  }

  @NonNull
  @Override
  public ModelObject getContext() {
    return Jenkins.getInstance();
  }

  @Override
  public boolean hasPermission(
      @NonNull Authentication authentication, @NonNull Permission permission) {
    return CredentialsProvider.VIEW.equals(permission)
        && Jenkins.getInstance().getACL().hasPermission(authentication, permission);
  }

  @NonNull
  @Override
  public List<Credentials> getCredentials(@NonNull Domain domain) {
    if (Domain.global().equals(domain)
        && Jenkins.getInstance().hasPermission(CredentialsProvider.VIEW)) {
      return provider.getCredentials(Credentials.class, Jenkins.getInstance(), ACL.SYSTEM);
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public boolean addCredentials(@NonNull Domain domain, @NonNull Credentials credentials)
      throws IOException {
    throw new UnsupportedOperationException(
        "Jenkins may not add credentials to GCP Secrets Manager");
  }

  @Override
  public boolean removeCredentials(@NonNull Domain domain, @NonNull Credentials credentials)
      throws IOException {
    throw new UnsupportedOperationException(
        "Jenkins may not add credentials to GCP Secrets Manager");
  }

  @Override
  public boolean updateCredentials(
      @NonNull Domain domain, @NonNull Credentials credentials, @NonNull Credentials credentials1)
      throws IOException {
    throw new UnsupportedOperationException(
        "Jenkins may not add credentials to GCP Secrets Manager");
  }

  @Nullable
  @Override
  public CredentialsStoreAction getStoreAction() {
    return action;
  }

  @ExportedBean
  public static class GcpCredentialsStoreAction extends CredentialsStoreAction {

    private static final String ICON_CLASS = "icon-gcp-secrets-manager-credentials-store";

    private final GcpCredentialsStore store;

    public GcpCredentialsStoreAction(GcpCredentialsStore store) {
      this.store = store;

      IconSet.icons.addIcon(
          new Icon(
              ICON_CLASS + " icon-sm",
              "gcp-secrets-manager-credentials-provider/images/16x16/icon.png",
              Icon.ICON_SMALL_STYLE,
              IconType.PLUGIN));
      IconSet.icons.addIcon(
          new Icon(
              ICON_CLASS + " icon-md",
              "gcp-secrets-manager-credentials-provider/images/24x24/icon.png",
              Icon.ICON_MEDIUM_STYLE,
              IconType.PLUGIN));
      IconSet.icons.addIcon(
          new Icon(
              ICON_CLASS + " icon-lg",
              "gcp-secrets-manager-credentials-provider/images/32x32/icon.png",
              Icon.ICON_LARGE_STYLE,
              IconType.PLUGIN));
      IconSet.icons.addIcon(
          new Icon(
              ICON_CLASS + " icon-xlg",
              "gcp-secrets-manager-credentials-provider/images/48x48/icon.png",
              Icon.ICON_XLARGE_STYLE,
              IconType.PLUGIN));
    }

    @NonNull
    @Override
    public CredentialsStore getStore() {
      return store;
    }
  }
}
