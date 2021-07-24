package io.jenkins.plugins.credentials.gcp.secretsmanager;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import java.util.Map;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;


public abstract class GcpCredentialsConverter implements ExtensionPoint {

    public abstract boolean canResolve(String type);
    public abstract BaseStandardCredentials resolve(String name, Map<String, String> labels, SecretGetter secretGetter);

    public static final ExtensionList<GcpCredentialsConverter> all() {
        return ExtensionList.lookup(GcpCredentialsConverter.class);
    }

    @CheckForNull
    static final GcpCredentialsConverter lookup(String type) {
        ExtensionList<GcpCredentialsConverter> all = all();
        for (GcpCredentialsConverter stcc : all) {
            if (stcc.canResolve(type)) {
                return stcc;
            }
        }
        return null;
    }
}
