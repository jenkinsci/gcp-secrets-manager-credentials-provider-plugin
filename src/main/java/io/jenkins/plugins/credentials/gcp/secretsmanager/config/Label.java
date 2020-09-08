package io.jenkins.plugins.credentials.gcp.secretsmanager.config;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import java.io.Serializable;
import javax.annotation.Nonnull;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class Label extends AbstractDescribableImpl<Label> implements Serializable {

  private static final long serialVersionUID = 1L;

  private String key;
  private String value;

  @DataBoundConstructor
  public Label(String key, String value) {
    this.key = key;
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  @DataBoundSetter
  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  @DataBoundSetter
  public void setValue(String value) {
    this.value = value;
  }

  @Extension
  @Symbol("filters")
  @SuppressWarnings("unused")
  public static class DescriptorImpl extends Descriptor<Label> {
    @Override
    @Nonnull
    public String getDisplayName() {
      return "labels";
    }
  }
}
