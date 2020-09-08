package io.jenkins.plugins.credentials.gcp.secretsmanager;

import com.cloudbees.plugins.credentials.CredentialsUnavailableException;
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;

public class GcpSecretGetter implements SecretGetter {

  private final SecretManagerServiceClient client;

  public GcpSecretGetter(final SecretManagerServiceClient client) {
    this.client = client;
  }

  @Override
  public String getSecretString(String id) {
    try {
      final AccessSecretVersionResponse response = client.accessSecretVersion(id);
      return response.getPayload().getData().toStringUtf8();
    } catch (ApiException e) {
      throw new CredentialsUnavailableException(
          "secret", "Unable to retrieve secret from secret manager", e);
    }
  }

  @Override
  public byte[] getSecretBytes(String id) {
    try {
      final AccessSecretVersionResponse response = client.accessSecretVersion(id);
      return response.getPayload().getData().toByteArray();
    } catch (ApiException e) {
      throw new CredentialsUnavailableException(
          "secret", "Unable to retrieve secret from secret manager", e);
    }
  }
}
