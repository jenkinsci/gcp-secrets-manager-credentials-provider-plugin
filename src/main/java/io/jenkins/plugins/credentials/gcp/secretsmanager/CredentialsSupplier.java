package io.jenkins.plugins.credentials.gcp.secretsmanager;

import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.secretmanager.v1.ListSecretsRequest;
import com.google.cloud.secretmanager.v1.ProjectName;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import io.jenkins.plugins.credentials.gcp.secretsmanager.config.Filter;
import io.jenkins.plugins.credentials.gcp.secretsmanager.config.Messages;
import io.jenkins.plugins.credentials.gcp.secretsmanager.config.PluginConfiguration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

public class CredentialsSupplier implements Supplier<Collection<StandardCredentials>> {

  public static Supplier<Collection<StandardCredentials>> standard() {
    return new CredentialsSupplier();
  }

  @Override
  public Collection<StandardCredentials> get() {
    PluginConfiguration configuration = PluginConfiguration.getInstance();
    String projectId = configuration.getProject();
    Filter filter = configuration.getFilter();

    String[] filters = new String[0];

    if (filter.getValue() != null) {
      filters = filter.getValue().split(",");
    }

    if (projectId == null || "".equals(projectId)) {
      return Collections.emptyList();
    }

    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      ListSecretsRequest listSecretsRequest =
          ListSecretsRequest.newBuilder().setParent(ProjectName.of(projectId).toString()).build();

      SecretManagerServiceClient.ListSecretsPagedResponse secrets =
          client.listSecrets(listSecretsRequest);

      final Collection<StandardCredentials> credentials = new ArrayList<>();

      for (Secret secret : secrets.iterateAll()) {
        Map<String, String> labelsMap = secret.getLabelsMap();

        if (filter.getLabel() != null && filter.getValue() != null) {
          final String matchingLabel = filter.getLabel();

          if (labelsMap.containsKey(matchingLabel)) {
            final String labelValue = labelsMap.get(matchingLabel);

            if (!matchesLabel(labelValue, filters)) {
              continue;
            }

          } else {
            continue;
          }
        }

        if (labelsMap.containsKey(Labels.TYPE.toLowerCase())) {
          final String secretName = secret.getName();
          final String name = secretName.substring(secretName.lastIndexOf("/") + 1);
          final Map<String, String> labels = secret.getLabelsMap();
          CredentialsFactory.create(name, labels, new GcpSecretGetter(projectId))
              .ifPresent(credentials::add);
        }
      }

      return credentials;

    } catch (IOException | ApiException e) {
      throw new CredentialsUnavailableException(
          "secret", Messages.couldNotRetrieveCredentialError(), e);
    }
  }

  private boolean matchesLabel(final String labelValue, String[] filters) {
    for (String filter : filters) {
      if (labelValue.equals(filter)) {
        return true;
      }
    }
    return false;
  }
}
