package io.jenkins.plugins.credentials.gcp.secretsmanager.config;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

@Extension
@Symbol("gcpCredentialsProvider")
public class PluginConfiguration extends GlobalConfiguration {

  private String project;
  private Filter filter;
  private ServerSideFilter serverSideFilter;

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

  public Filter getFilter() {
    return filter;
  }

  @DataBoundSetter
  @SuppressWarnings("unused")
  public void setFilter(Filter filter) {
    this.filter = filter;
    save();
  }

  public ServerSideFilter getServerSideFilter() {
    return serverSideFilter;
  }

  @DataBoundSetter
  @SuppressWarnings("unused")
  public void setServerSideFilter(ServerSideFilter serverSideFilter) {
    this.serverSideFilter = serverSideFilter;
    save();
  }

  @Override
  public synchronized boolean configure(StaplerRequest req, JSONObject json) {
    this.project = null;
    this.filter = null;
    this.serverSideFilter = null;

    req.bindJSON(this, json);
    save();
    return true;
  }
}
