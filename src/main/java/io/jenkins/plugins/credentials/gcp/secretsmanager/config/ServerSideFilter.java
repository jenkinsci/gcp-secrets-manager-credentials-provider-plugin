package io.jenkins.plugins.credentials.gcp.secretsmanager.config;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import java.io.Serializable;
import javax.annotation.Nonnull;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class ServerSideFilter extends AbstractDescribableImpl<ServerSideFilter> implements Serializable {

    private String filter;

    @DataBoundConstructor
    public ServerSideFilter(String filter) {
        this.filter = filter;
    }

    public String getFilter() {
        return filter;
    }

    @DataBoundSetter
    @SuppressWarnings("unused")
    public void setLabel(String filter) {
        this.filter = filter;
    }

    @Extension
    @Symbol("serversidefilter")
    @SuppressWarnings("unused")
    public static class DescriptorImpl extends Descriptor<ServerSideFilter> {

        @Override
        @Nonnull
        public String getDisplayName() {
            return "Server-side Filter";
        }
    }
}
