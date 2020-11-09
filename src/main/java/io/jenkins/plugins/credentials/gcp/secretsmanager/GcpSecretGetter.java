package io.jenkins.plugins.credentials.gcp.secretsmanager;

import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.ProjectName;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import io.jenkins.plugins.credentials.gcp.secretsmanager.config.Messages;
import java.io.IOException;
import java.io.Serializable;

public class GcpSecretGetter implements SecretGetter, Serializable {

  private static final long serialVersionUID = 1L;

  private final String projectId;

  public GcpSecretGetter(String projectId) {
    this.projectId = projectId;
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
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      final ProjectName project = ProjectName.of(projectId);
      final SecretVersionName secretVersionName =
          SecretVersionName.newBuilder()
              .setProject(project.getProject())
              .setSecret(id)
              .setSecretVersion("latest")
              .build();
      final AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);
      return response.getPayload();
    } catch (IOException | ApiException e) {
      throw new CredentialsUnavailableException(
          "secret", Messages.couldNotRetrieveCredentialError(), e);
    }
  }
}
