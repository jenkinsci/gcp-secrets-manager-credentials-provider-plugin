package io.jenkins.plugins.casc.secretsmanager;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import hudson.Extension;
import hudson.ExtensionList;
import io.jenkins.plugins.casc.SecretSource;
import io.jenkins.plugins.credentials.gcp.secretsmanager.GcpCredentialsProvider;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

@Extension
public class GcpSecretSource extends SecretSource {

  private static final Logger LOGGER = Logger.getLogger(GcpSecretSource.class.getName());

  @Override
  public Optional<String> reveal(String id) throws IOException {

    // GcpCredentialsProvider contains logic for fetching secrets from GCP's Secret Manager
    //   as well as caching results locally
    // Since GcpSecretSource and GcpCredentialsProvider are initialized separately,
    //   there is no good way to inject the dependency from one to the other;
    //   instead, we assume that Jenkins core initializes GcpCredentialsProvider before GcpSecretSource
    //   and rely on ExtensionList to locate the GcpCredentialsProvider
    GcpCredentialsProvider gcpCredentialsProvider = ExtensionList.lookupSingleton(GcpCredentialsProvider.class);

    final Collection<StandardCredentials> credentials = gcpCredentialsProvider.getSupplier().get();
    if (credentials == null) {
        LOGGER.info("No credentials found, skipping jcasc secret resolution");
        return Optional.empty();
    }

    for (StandardCredentials credential : credentials) {
      if (credential.getId().equals(id)) {

        // For the time being, we only support accessing "Secret text" type credentials
        if (credential instanceof StringCredentials) {
          LOGGER.fine("The requested credential " + credential.getId() + " exists in GCP Credentials Provider, and is of type " + credential.getClass().getName());
          return Optional.ofNullable(((StringCredentials) credential).getSecret().getPlainText());
        } else {
          LOGGER.warning("The requested credential " + credential.getId() + " exists in GCP Credentials Provider, but is of type " + credential.getClass().getName() + " which is not supported by GcpSecretSource; we do not return any result");
          return Optional.empty();
        }
      }
    }

    return Optional.empty();
  }
}
