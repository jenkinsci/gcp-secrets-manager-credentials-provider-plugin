package io.jenkins.plugins.credentials.gcp.secretsmanager;

import static org.assertj.core.api.Assertions.assertThat;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class CredentialsFactoryTest {

  @Rule public JenkinsRule jenkins = new JenkinsRule();

  @Test
  public void shouldGetStringCredentials() {
    Map<String, String> labels = new HashMap<>();
    labels.put(Labels.TYPE, Type.STRING);

    final SecretGetter mockSecretGetter =
        new SecretGetter() {
          @Override
          public String getSecretString(String id) {
            return "bar";
          }

          @Override
          public byte[] getSecretBytes(String id) {
            return new byte[0];
          }
        };

    Optional<StandardCredentials> credential =
        CredentialsFactory.create("foo", labels, mockSecretGetter);

    assertThat(credential).isNotEmpty();
    assertThat(credential.get()).isInstanceOf(GcpStringCredentials.class);
    assertThat(((GcpStringCredentials) credential.get()).getSecret().getPlainText())
        .isEqualTo("bar");
  }

  @Test
  public void shouldGetFileCredentials() throws Exception {
    final String jsonFile = "{\n" + "  \"foo\": \"bar\"\n" + "}";

    Map<String, String> labels = new HashMap<>();
    labels.put(Labels.TYPE, Type.FILE);
    labels.put(Labels.FILENAME, "my_file");
    labels.put(Labels.FILE_EXTENSION, "json");

    final SecretGetter mockSecretGetter =
        new SecretGetter() {
          @Override
          public String getSecretString(String id) {
            return "";
          }

          @Override
          public byte[] getSecretBytes(String id) {
            return jsonFile.getBytes();
          }
        };

    Optional<StandardCredentials> credential =
        CredentialsFactory.create("foo", labels, mockSecretGetter);

    assertThat(credential).isNotEmpty();
    assertThat(credential.get()).isInstanceOf(GcpFileCredentials.class);

    final GcpFileCredentials gcpFileCredentials = (GcpFileCredentials) credential.get();
    final InputStream is = gcpFileCredentials.getContent();

    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    int nRead;
    byte[] data = new byte[1024];
    while ((nRead = is.read(data, 0, data.length)) != -1) {
      buffer.write(data, 0, nRead);
    }

    buffer.flush();
    byte[] byteArray = buffer.toByteArray();
    is.close();

    String text = new String(byteArray, StandardCharsets.UTF_8);
    assertThat(text).isEqualTo(jsonFile);
    assertThat(gcpFileCredentials.getFileName()).isEqualTo("my_file.json");
  }

  @Test
  public void shouldGetUsernamePasswordCredentials() {
    Map<String, String> labels = new HashMap<>();
    labels.put(Labels.TYPE, Type.USERNAME_PASSWORD);
    labels.put(Labels.USERNAME, "taylor");

    final SecretGetter mockSecretGetter =
        new SecretGetter() {
          @Override
          public String getSecretString(String id) {
            return "hunter2";
          }

          @Override
          public byte[] getSecretBytes(String id) {
            return new byte[0];
          }
        };

    Optional<StandardCredentials> credential =
        CredentialsFactory.create("foo", labels, mockSecretGetter);

    assertThat(credential).isNotEmpty();
    assertThat(credential.get()).isInstanceOf(GcpUsernamePasswordCredentials.class);

    final GcpUsernamePasswordCredentials gcpCredential =
        ((GcpUsernamePasswordCredentials) credential.get());
    assertThat(gcpCredential.getUsername()).isEqualTo("taylor");
    assertThat(gcpCredential.getPassword().getPlainText()).isEqualTo("hunter2");
  }

  @Test
  public void shouldGetSshUserPrivateKeyCredentials() {
    // Fake key generated with:
    // ssh-keygen -t rsa -m PEM
    final String key =
        "-----BEGIN RSA PRIVATE KEY-----\n"
            + "MIIG4wIBAAKCAYEAxnCtdhZI1CEodKk82DTDdfn4yUp/inXJt+0dyia88b8YCOxS\n"
            + "mqez5lAnpU/UKzaH8FOlxVHt1Y9V8DTGLaTnUachQXtx61EMTl0QIZa6PLtRW2lF\n"
            + "sO3Hqk+YxzNMrFjAQohnZNGO/6ZtlUrxFSCvc2jIwlyl812MqW7fTVfY8vG3H8V3\n"
            + "9caeKMDvraW1ElLQTtBCxGS7cjdZUzBkJrPjhOIiHK8t6nPoItu47dLEWc913buo\n"
            + "XUN2AIRxRbpI+BzmJ1u1g8faHN2KX1Ij/6reMozIc3q3h5rzkcdqg7kYJGYriXXK\n"
            + "5713JRFDkP0Y6lAj4DlazekpPbqvugLXopSE4CjbCdcUr74HoAffZK+ANiHW3YaK\n"
            + "2DxNVUaesz7cvkVXudeiS+Ma6GOMf7nIvwc1W/uqGTrptYw8NGGeA3DHK4W0IscG\n"
            + "jm3kYLy/CEUL+ydk1B0EaJHsFSMEaJ1Y9LUhYi4EfjZH2yomMAFLXfKPRnl8qupF\n"
            + "msnvNTn2WbkP8YorAgMBAAECggGAQ6DdDPSVR24xemi+6rQ4tPlHF1rDUq3H3F38\n"
            + "HAvNstm8WDYqBLkIr4I7sLQfC8ce1wG66h983Z5m3GEv9RhRGVvLEqT+cwMBHafL\n"
            + "upbCy7XPCUc8UAsJU/qih6jtBYONql8QVaSxhmDy0cFjnh6wjLV2Tf8DM4h7SrcV\n"
            + "dSduMYW9Mh+7PVC+UAdI57YkmqUMhv+k8Yee9lL0p0xDpYkDDiN/GqV9EX0E7PRS\n"
            + "9wIwlqcEdVfiDj7SdWStseTFv4EhOQkZgUA+LVyzGo8Nk74Du4/g1PEWb9sqbDPa\n"
            + "cvzlBZ7FTb0k8yucg9vfXXczicvPfSWWs9XuoMFEWeGQQuruwRH4p2IJhmygETzD\n"
            + "pDNOtvJYnDMz2fE/kh530NtSQFEnvlzok7WbxEoo3y393wmtNJYv/H5G0vIVuJvx\n"
            + "CnNgiFGpmfGekXi9DrMZFnPPXTMN/dckO3Wq1fKiz+/6mcUQXFw5nO55RFKriQuV\n"
            + "Xg0PLPqwZZV+V15V7r/i1dGMh60JAoHBAO3TOgCwXcHygQ4H8BHZOyUdmghN4/0e\n"
            + "6Y+FIiHaLYS/6nVQXITfx3PbZhoWJvWenYpaqg00miT36YsTWAVDM30uZ6UO7vnH\n"
            + "ldfdoKms3UJeG8rqx2fRWMHAOAD5x63oNQ70/O6a3rtJTKYTYoEiGq5H4RjDR2LO\n"
            + "boVcqRVx2A254wrHOvhb16CZeCuMHy4kle+qSpgniuIkVNzRjRNP5by3dIrW9Gzl\n"
            + "/KAwz7uoiKWA0+K76/eWi7/p366JqQStjwKBwQDVmu4I4ItXHTvfai4HSrD7w74A\n"
            + "yY0TAs3aWH0BYZHeZ8PjARPtAkyKwGPlerj0x/mnRVsVCMCUvqKdlsszJPzAn+fL\n"
            + "xRl5yU1Yp6CdbYrA3KRzxcU8qp62I6iBqJ2XBjJdQiKZyXwTuhUyY5loNKthkXwb\n"
            + "qcmT0p84jcH6MqlXGmN3QYXiwhMVexac8D3tdGGa4nR2kUE8j2H0UaQ5s4QLvear\n"
            + "SbTknEJ1y29czXqiMBWb4eCusG8Cw/AjLLdLA6UCgcB65hvkPT/GORZd9NYnsxVw\n"
            + "YbK2teGqbW4O/6Ka0c+R+Uck5qlY9PdyNJ+3vVjWtuf8lkuoVVWO4xoqB44F8itK\n"
            + "dk+TKXtvUqdpjRs4c59Ha7B7FQaE7NcsELQgNPPuN8ArgYivmRKewAs/C2dtCf21\n"
            + "sGlvTYK3WawdeLvb3B/lP/lHGZusunFcidJi1p9KThe5aUvxAwYvRM0h65w2raQv\n"
            + "+LHeCaT4HYWGFpNTC43J0SAt3CTdpkuDmk1AOd1BEJsCgcEAwLuLPa1FbLPu+vY2\n"
            + "U9P7/x6uCM3TIa0tCm8/U4iy8kcUQ3YMgZTKdYOEX2GtEwiA5YG53i/IfC3KUoDm\n"
            + "Fd6wxClEH+NWykFtLaoKTSGyybQMGn3/Km8Ux2lDOMJodwVbWjQ7PiHDd5U2XQHr\n"
            + "QFANtvprThaT0HKBwah+tv8RNouT5d3ULTdJut8WF0LZzbBuBS+BQB5uC3OgQa+M\n"
            + "i5fDdEjDohfeRUwM13ZYpBavKTpTtRRJA7YkZA1hVouZUvEVAoHAOOaLn4cnVypU\n"
            + "I77voNZFd5ZjJ4oh02I+TPkuAFNfrMWCsNIH0rEdZKTmLsDH8w87mgfR0Fw5/RWN\n"
            + "UwHhsuvt0BKdBKHt+ZmDjjeBaoB56h59ISkhHwPUlqWPxXe/sm4RPFmc03kClgBe\n"
            + "hv7M27bvxgfLrdKsaT72B5qqOqfttV3KRdH1A+/5Rg0ythhAc4wCEnDLZMbKLAGg\n"
            + "3mB1kUvn0qLmBRXKiHNpwoadIzH9e30AETid1lLogNmdX2yWwATj\n"
            + "-----END RSA PRIVATE KEY-----";

    Map<String, String> labels = new HashMap<>();
    labels.put(Labels.TYPE, Type.SSH_USER_PRIVATE_KEY);
    labels.put(Labels.USERNAME, "taylor");

    final SecretGetter mockSecretGetter =
        new SecretGetter() {
          @Override
          public String getSecretString(String id) {
            return key;
          }

          @Override
          public byte[] getSecretBytes(String id) {
            return new byte[0];
          }
        };

    Optional<StandardCredentials> credential =
        CredentialsFactory.create("foo", labels, mockSecretGetter);

    assertThat(credential).isNotEmpty();
    assertThat(credential.get()).isInstanceOf(GcpSshUserPrivateKey.class);

    final GcpSshUserPrivateKey gcpCredential = ((GcpSshUserPrivateKey) credential.get());
    assertThat(gcpCredential.getUsername()).isEqualTo("taylor");
    assertThat(gcpCredential.getPrivateKey()).isEqualTo(key);
  }

  @Test
  public void shouldGetCertificateCredentials() throws Exception {
    Map<String, String> labels = new HashMap<>();
    labels.put(Labels.TYPE, Type.CERTIFICATE);

    URL resource = CredentialsFactoryTest.class.getClassLoader().getResource("test-keystore");

    final SecretGetter mockSecretGetter =
        new SecretGetter() {
          @Override
          public String getSecretString(String id) {
            return "";
          }

          @Override
          public byte[] getSecretBytes(String id) {
            try {
              return Files.readAllBytes(Paths.get(resource.toURI()));
            } catch (Exception e) {
              return new byte[0];
            }
          }
        };

    Optional<StandardCredentials> credential =
        CredentialsFactory.create("foo", labels, mockSecretGetter);

    assertThat(credential).isNotEmpty();
    assertThat(credential.get()).isInstanceOf(GcpCertificateCredentials.class);

    final GcpCertificateCredentials gcpCredential = ((GcpCertificateCredentials) credential.get());
    assertThat(gcpCredential.getKeyStore()).isNotNull();
  }
}
