package io.jenkins.plugins.credentials.gcp.secretsmanager;

public interface SecretGetter {

  String getSecretString(String id);

  byte[] getSecretBytes(String id);
}
