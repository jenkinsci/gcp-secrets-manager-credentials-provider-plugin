package io.jenkins.plugins.credentials.gcp.secretsmanager.config;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import java.io.Serializable;
import javax.annotation.Nonnull;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class Filter extends AbstractDescribableImpl<Filter> implements Serializable {

  private String label;
  private String value;

  @DataBoundConstructor
  public Filter(String key, String value) {
    this.label = key;
    this.value = value;
  }

  public String getLabel() {
    return label;
  }

  @DataBoundSetter
  @SuppressWarnings("unused")
  public void setLabel(String label) {
    this.label = label;
  }

  public String getValue() {
    return value;
  }

  @DataBoundSetter
  @SuppressWarnings("unused")
  public void setValue(String value) {
    this.value = value;
  }

  @Extension
  @Symbol("filters")
  @SuppressWarnings("unused")
  public static class DescriptorImpl extends Descriptor<Filter> {

    @Override
    @Nonnull
    public String getDisplayName() {
      return "Filter";
    }
  }
}
