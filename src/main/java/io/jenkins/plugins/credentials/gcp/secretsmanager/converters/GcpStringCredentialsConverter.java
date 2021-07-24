package io.jenkins.plugins.credentials.gcp.secretsmanager.converters;

import java.util.Map;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import org.jenkinsci.plugins.variant.OptionalExtension;
import io.jenkins.plugins.credentials.gcp.secretsmanager.GcpCredentialsConverter;
import io.jenkins.plugins.credentials.gcp.secretsmanager.GcpStringCredentials;
import io.jenkins.plugins.credentials.gcp.secretsmanager.SecretGetter;
import io.jenkins.plugins.credentials.gcp.secretsmanager.Type;
import io.jenkins.plugins.credentials.gcp.secretsmanager.CredentialsFactory.SecretSupplier;

@OptionalExtension(requirePlugins = { "plain-credentials" })
public class GcpStringCredentialsConverter extends GcpCredentialsConverter {

    @Override
    public boolean canResolve(String type) {
        return Type.STRING.equals(type);
    }

    @Override
    public BaseStandardCredentials resolve(String name, Map<String, String> labels, SecretGetter secretGetter) {
        return new GcpStringCredentials(name, new SecretSupplier(name, secretGetter));
    }
}
