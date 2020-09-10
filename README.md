# GCP Secrets Manager Credentials Provider

Access credentials from [Google Cloud Secrets Manager](https://cloud.google.com/secret-manager) in your Jenkins jobs.

## Setup

Enable the GCP secret manager API via the GCP console or by running:

```shell script
gcloud services enable secretmanager.googleapis.com --project=my-project
```

Follow the [documentation](https://cloud.google.com/secret-manager/docs/creating-and-accessing-secrets#secretmanager-create-secret-cli) for 
creating and updating secrets.

## Usage

Once the plugin is installed, navigate to "Configure System" and find the "GCP Secrets Manager" section.
Input the name of the GCP project that where the secrets are stored.

Secret names (not values) are cached in-memory for 5 minutes. This is not currently configurable.

Secrets created in GCP Secret Manager must have the label with key `jenkins-credentials-type` and one of the following values:

* string
* file
* usernamePassword
* sshUserPrivateKey
* certificate

### IAM

Give Jenkins read access to the Secret Manager with an [Google Cloud IAM policy](https://cloud.google.com/iam/docs).

At minimum, give Jenkins an IAM role with the following permissions:

* secretmanager.secrets.list
* secretmanager.secrets.get
* secretmanager.versions.list
* secretmanager.versions.get
* secretmanager.versions.access

The easiest option is to give the Jenkins service account the pre-built roles `roles/secretmanager.secretAccessor` and 
`roles/secretmanager.viewer`. This can be done at the secret, project, folder, or organization level.


If you are running Jenkins on GCP, attach a [default service account](https://cloud.google.com/iam/docs/service-accounts#default)
to the instance running Jenkins. You can use [Workload Identity](https://cloud.google.com/kubernetes-engine/docs/how-to/workload-identity) 
if running Jenkins on Google Kubernetes Engine.

If you are not running Jenkins on GCP, set the environment variable `GOOGLE_APPLICATION_CREDENTIALS` for the Jenkins process
to the path of a JSON service account key with the above permissions.


## Examples

### Secret Text

```shell script
echo -n 's3cr3t' | gcloud secrets create datadog-api-key \
  --data-file=- \
  --labels=jenkins-credentials-type=string \
  --replication-policy=automatic \
  --project=my-project
```

Scripted pipeline:

```
node {
    withCredentials([string(credentialsId: 'datadog-api-key', variable: 'DATADOG_API_KEY')]) {
        echo 'My string: $DATADOG_API_KEY'
    }
}
```

### File

```shell script
gcloud secrets create serviceacount \
  --data-file=my-file.json \
  --labels=jenkins-credentials-type=file,jenkins-credentials-file-extension=json \
  --replication-policy=automatic \
  --project=my-project
```

Scripted pipeline:

```
node {
    withCredentials([file(credentialsId: 'serviceaccount', variable: 'MY_FILE')]) {
        echo 'My file path: $MY_FILE'
    }
}
```

### Username and Password

```shell script
echo -n 's3cr3t' | gcloud secrets create nexus-creds \
  --data-file=- \
  --labels=jenkins-credentials-type=usernamePassword,jenkins-credentials-username=nexus-user \
  --replication-policy=automatic \
  --project=my-project
```

Scripted pipeline:

```
node {
    withCredentials([
        usernamePassword(
            credentialsId: 'nexus-creds',
            usernameVariable: 'NEXUS_USERNAME',
            passwordVariable: 'NEXUS_PASSWORD'
        )
    ]) {
        echo 'My credentials: $NEXUS_USERNAME:$NEXUS_PASSWORD'
    }
}
```

### SSH Key

```shell script
gcloud secrets create ssh-key \
  --data-file=id_rsa \
  --labels=jenkins-credentials-type=sshUserPrivateKey,jenkins-credentials-username=taylor \
  --replication-policy=automatic \
  --project=my-project
```

Scripted pipeline:

```
node {
    sshagent(credentials: ['ssh-key']) {
        sh "ssh -T git@github.com"
    }
}
```

### Certificate

```shell script
gcloud secrets create certificate \
  --data-file=keystore \
  --labels=jenkins-credentials-type=certificate \
  --replication-policy=automatic \
  --project=my-project
```

Scripted pipeline:

```
node {
    withCredentials([
        certificate(
            credentialsId: 'certificate',
            keystoreVariable: 'KEYSTORE_VARIABLE'
        )
    ]) {
        echo 'My keystore: $KEYSTORE_VARIABLE'
    }
}
```

## Limitations

* The secret manager API does not support server-side filtering. 

* The secret manager API does not support descriptions. The description of the secret will be the 
same as the id.