package io.jenkins.plugins.credentials.gcp.secretsmanager.converters;

import java.util.Map;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import hudson.Extension;
import io.jenkins.plugins.credentials.gcp.secretsmanager.GcpCredentialsConverter;
import io.jenkins.plugins.credentials.gcp.secretsmanager.GcpCertificateCredentials;
import io.jenkins.plugins.credentials.gcp.secretsmanager.SecretGetter;
import io.jenkins.plugins.credentials.gcp.secretsmanager.Type;
import io.jenkins.plugins.credentials.gcp.secretsmanager.CredentialsFactory.SecretBytesSupplier;

@Extension
public class GcpCertificateCredentialsConverter extends GcpCredentialsConverter {

    @Override
    public boolean canResolve(String type) {
        return Type.CERTIFICATE.equals(type);
    }

    @Override
    public BaseStandardCredentials resolve(String name, Map<String, String> labels, SecretGetter secretGetter) {
        return new GcpCertificateCredentials(name, new SecretBytesSupplier(name, secretGetter));
    }
}
