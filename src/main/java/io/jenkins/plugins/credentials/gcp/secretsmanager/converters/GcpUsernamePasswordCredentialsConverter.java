package io.jenkins.plugins.credentials.gcp.secretsmanager.converters;

import java.util.Map;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import hudson.Extension;
import io.jenkins.plugins.credentials.gcp.secretsmanager.GcpCredentialsConverter;
import io.jenkins.plugins.credentials.gcp.secretsmanager.GcpUsernamePasswordCredentials;
import io.jenkins.plugins.credentials.gcp.secretsmanager.Labels;
import io.jenkins.plugins.credentials.gcp.secretsmanager.SecretGetter;
import io.jenkins.plugins.credentials.gcp.secretsmanager.Type;
import io.jenkins.plugins.credentials.gcp.secretsmanager.CredentialsFactory.SecretSupplier;

@Extension
public class GcpUsernamePasswordCredentialsConverter extends GcpCredentialsConverter {

    @Override
    public boolean canResolve(String type) {
        return Type.USERNAME_PASSWORD.equals(type);
    }

    @Override
    public BaseStandardCredentials resolve(String name, Map<String, String> labels, SecretGetter secretGetter) {
        final String username = labels.getOrDefault(Labels.USERNAME, "");

        return new GcpUsernamePasswordCredentials(name, new SecretSupplier(name, secretGetter), username);
    }
}
