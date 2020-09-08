package io.jenkins.plugins.credentials.gcp.secretsmanager;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.google.common.base.Suppliers;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.Extension;
import hudson.model.ItemGroup;
import hudson.model.ModelObject;
import hudson.security.ACL;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;

@Extension
public class GcpCredentialsProvider extends CredentialsProvider {

  public final GcpCredentialsStore gcpCredentialsStore = new GcpCredentialsStore(this);

  final Supplier<Collection<StandardCredentials>> credentialsSupplier =
      memoizeWithExpiration(CredentialsSupplier.standard(), Duration.ofMinutes(5));

  private static <T> Supplier<T> memoizeWithExpiration(Supplier<T> base, Duration duration) {
    return Suppliers.memoizeWithExpiration(base::get, duration.toMillis(), TimeUnit.MILLISECONDS)
        ::get;
  }

  @NonNull
  @Override
  public <C extends Credentials> List<C> getCredentials(
      @NonNull Class<C> type,
      @Nullable ItemGroup itemGroup,
      @Nullable Authentication authentication) {
    if (ACL.SYSTEM.equals(authentication)) {
      final Collection<StandardCredentials> credentials = credentialsSupplier.get();

      return credentials.stream()
          .filter(c -> type.isAssignableFrom(c.getClass()))
          .map(type::cast)
          .collect(Collectors.toList());
    }

    return Collections.emptyList();
  }

  @Override
  public CredentialsStore getStore(ModelObject object) {
    return object == Jenkins.getInstance() ? gcpCredentialsStore : null;
  }
}
