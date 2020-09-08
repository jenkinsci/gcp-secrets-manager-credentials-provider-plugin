package io.jenkins.plugins.credentials.gcp.secretsmanager.config;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import java.io.Serializable;
import javax.annotation.Nonnull;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class Filters extends AbstractDescribableImpl<Filters> implements Serializable {

  private Label label;

  @DataBoundConstructor
  public Filters(Label label) {
    this.label = label;
  }

  public Label getLabel() {
    return label;
  }

  @DataBoundSetter
  @SuppressWarnings("unused")
  public void setLabel(Label label) {
    this.label = label;
  }

  @Extension
  @Symbol("filters")
  @SuppressWarnings("unused")
  public static class DescriptorImpl extends Descriptor<Filters> {

    @Override
    @Nonnull
    public String getDisplayName() {
      return "wheee";
    }
  }
}
