package io.jenkins.plugins.credentials.gcp.secretsmanager;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.secretmanager.v1.ListSecretsRequest;
import com.google.cloud.secretmanager.v1.ListSecretsRequest.Builder;
import com.google.cloud.secretmanager.v1.ProjectName;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import io.jenkins.plugins.credentials.gcp.secretsmanager.config.Filter;
import io.jenkins.plugins.credentials.gcp.secretsmanager.config.Messages;
import io.jenkins.plugins.credentials.gcp.secretsmanager.config.PluginConfiguration;
import io.jenkins.plugins.credentials.gcp.secretsmanager.config.ServerSideFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CredentialsSupplier implements Supplier<Collection<StandardCredentials>> {

  private static final Logger LOGGER = Logger.getLogger(CredentialsSupplier.class.getName());

  private static final String DEFAULT_FILTER = "labels.jenkins-credentials-type:*";

  public static Supplier<Collection<StandardCredentials>> standard() {
    return new CredentialsSupplier();
  }

  @Override
  public Collection<StandardCredentials> get() {
    PluginConfiguration configuration = PluginConfiguration.getInstance();
    String project = configuration.getProject();
    Filter filter = configuration.getFilter();
    ServerSideFilter serverSideFilter = configuration.getServerSideFilter();

    String[] projectIds;
    String[] filters = new String[0];
    String fullServerSideFilter = DEFAULT_FILTER;

    if (filter != null && filter.getValue() != null) {
      filters = filter.getValue().split(",");
    }

    if (project == null || "".equals(project)) {
      return Collections.emptyList();
    } else {
      projectIds = project.split(",");
    }

    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      final Collection<StandardCredentials> credentials = new ArrayList<>();
      for (String projectId : projectIds) {
        projectId = projectId.trim();

        ListSecretsRequest listSecretsRequest;

        Builder builder =
            ListSecretsRequest.newBuilder().setParent(ProjectName.of(projectId).toString());

        if (serverSideFilter != null && serverSideFilter.getFilter() != null) {
          fullServerSideFilter += " AND (" + serverSideFilter.getFilter() + ")";
        }

        LOGGER.info(String.format("Using filter \"%s\" to list secrets for %s", fullServerSideFilter, projectId));

        listSecretsRequest = builder.setFilter(fullServerSideFilter).build();

        SecretManagerServiceClient.ListSecretsPagedResponse secrets =
            client.listSecrets(listSecretsRequest);

        for (Secret secret : secrets.iterateAll()) {
          Map<String, String> labelsMap = secret.getLabelsMap();

          if (filter != null && filter.getLabel() != null && filter.getValue() != null) {
            final String matchingLabel = filter.getLabel();

            if (labelsMap.containsKey(matchingLabel)) {
              final String labelValue = labelsMap.get(matchingLabel);

              if (!matchesLabel(labelValue, filters)) {
                LOGGER.info(String.format("Secret %s does not match provided filter", secret.getName()));
                continue;
              }
            } else {
              LOGGER.info(String.format("Secret %s does not match provided filter", secret.getName()));
              continue;
            }
          }

          if (labelsMap.containsKey(Labels.TYPE.toLowerCase())) {
            final String secretName = secret.getName();
            final String name = secretName.substring(secretName.lastIndexOf("/") + 1);
            final Map<String, String> labels = secret.getLabelsMap();
            CredentialsFactory.create(name, projectId, labels, new GcpSecretGetter(projectId))
                .ifPresent(credentials::add);
          }
        }
      }

      return credentials;

    } catch (IOException | ApiException e) {
      LOGGER.log(Level.WARNING, Messages.couldNotRetrieveCredentialError(), e);
      return Collections.emptyList();
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
