package io.jenkins.plugins.credentials.gcp.secretsmanager;

import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.secretmanager.v1.ListSecretsRequest;
import com.google.cloud.secretmanager.v1.ProjectName;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import io.jenkins.plugins.credentials.gcp.secretsmanager.config.Messages;
import io.jenkins.plugins.credentials.gcp.secretsmanager.config.PluginConfiguration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class CredentialsSupplier implements Supplier<Collection<StandardCredentials>> {

  private static final Logger LOG = Logger.getLogger(CredentialsSupplier.class.getName());

  public static Supplier<Collection<StandardCredentials>> standard() {
    return new CredentialsSupplier();
  }

  @Override
  public Collection<StandardCredentials> get() {
    PluginConfiguration configuration = PluginConfiguration.getInstance();

    if (configuration.getProject() == null || "".equals(configuration.getProject())) {
      return Collections.emptyList();
    }

    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      final SecretGetter gcpSecretGetter = new GcpSecretGetter(client);

      ListSecretsRequest listSecretsRequest =
          ListSecretsRequest.newBuilder()
              .setParent(ProjectName.of(configuration.getProject()).toString())
              .build();

      SecretManagerServiceClient.ListSecretsPagedResponse secrets =
          client.listSecrets(listSecretsRequest);

      final Collection<StandardCredentials> credentials = new ArrayList<>();

      for (Secret secret : secrets.iterateAll()) {
        if (secret.getLabelsMap().containsKey(Labels.TYPE.toLowerCase())) {
          final String secretName = secret.getName();
          final String name = secretName.substring(secretName.lastIndexOf("/") + 1);
          final Map<String, String> labels = secret.getLabelsMap();
          CredentialsFactory.create(name, labels, gcpSecretGetter).ifPresent(credentials::add);
        }
      }

      return credentials;

    } catch (IOException | ApiException e) {
      throw new CredentialsUnavailableException(
          "secret", Messages.couldNotRetrieveCredentialError(), e);
    }
  }
}
