package io.jenkins.plugins.credentials.gcp.secretsmanager;

import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.LocationName;
import com.google.cloud.secretmanager.v1.ProjectName;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import io.jenkins.plugins.credentials.gcp.secretsmanager.config.Messages;
import java.io.IOException;
import java.io.Serializable;

public class GcpSecretGetter implements SecretGetter, Serializable {

  private static final long serialVersionUID = 1L;

  private final String projectId;

  private final String locationId;

  public GcpSecretGetter(String projectId, String locationId) {
    this.projectId = projectId;
    this.locationId = locationId;
  }

  @Override
  public String getSecretString(String id) {
    return getPayload(id).getData().toStringUtf8();
  }

  @Override
  public byte[] getSecretBytes(String id) {
    return getPayload(id).getData().toByteArray();
  }

  private SecretPayload getPayload(String id) {
    SecretManagerServiceClient client;
    final SecretVersionName secretVersionName;

    if (locationId.equals("global")) {
      try {
        client = SecretManagerServiceClient.create();
        final ProjectName project = ProjectName.of(projectId);
        secretVersionName =
            SecretVersionName.newBuilder()
                .setProject(project.getProject())
                .setSecret(id)
                .setSecretVersion("latest")
                .build();
      } catch (IOException e) {
        throw new CredentialsUnavailableException(
            "secret", Messages.couldNotRetrieveCredentialError(), e);
      }
    } else {
      String apiEndpoint = "secretmanager." + locationId + ".rep.googleapis.com:443";
      SecretManagerServiceSettings secretManagerServiceSettings;
      try {
        secretManagerServiceSettings = SecretManagerServiceSettings.newBuilder().setEndpoint(apiEndpoint).build();
        client = SecretManagerServiceClient.create(secretManagerServiceSettings);

        final LocationName locationName = LocationName.of(projectId, locationId);
        secretVersionName = SecretVersionName.newProjectLocationSecretSecretVersionBuilder()
            .setProject(locationName.getProject())
            .setLocation(locationName.getLocation())
            .setSecret(id)
            .setSecretVersion("latest")
            .build();
      } catch (IOException | ApiException e) {
        throw new CredentialsUnavailableException(
            "secret", Messages.couldNotRetrieveCredentialError(), e);
      }
    }
    final AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);
    return response.getPayload();
  }
}
