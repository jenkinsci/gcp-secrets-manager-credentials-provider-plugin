package io.jenkins.plugins.credentials.gcp.secretsmanager.config;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class PluginConfiguration extends GlobalConfiguration {

  private String project;
  private Filters filters;

  public PluginConfiguration() {
    load();
  }

  public static PluginConfiguration getInstance() {
    return all().get(PluginConfiguration.class);
  }

  public String getProject() {
    return project;
  }

  @DataBoundSetter
  @SuppressWarnings("unused")
  public void setProject(String project) {
    this.project = project;
    save();
  }

  public Filters getFilters() {
    return filters;
  }

  @DataBoundSetter
  @SuppressWarnings("unused")
  public void setFilters(Filters filters) {
    this.filters = filters;
    save();
  }

  @Override
  public synchronized boolean configure(StaplerRequest req, JSONObject json) {
    this.project = null;
    this.filters = null;

    req.bindJSON(this, json);
    save();
    return true;
  }
}
