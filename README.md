# GCP Secrets Manager Credentials Provider

Access credentials from GCP Secrets Manager in your Jenkins jobs.

## Setup

Enable the GCP secret manager API via the GCP console or by running:

```shell script
gcloud services enable secretmanager.googleapis.com --project=my-project
```

Follow the [documentation](https://cloud.google.com/secret-manager/docs/creating-and-accessing-secrets#secretmanager-create-secret-cli) for 
creating and updating secrets.

## Usage

Secrets must have the label with key `jenkins-credentials-type` and one of the following values:

* string
* file
* usernamePassword
* sshUserPrivateKey
* certificate

### IAM

Give Jenkins read access to the Secret Manager with an IAM policy.

It is required to give Jenkins the IAM role `roles/secretmanager.secretAccessor`. This can be done at the secret, project, 
folder, or organization level.

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

```
node {
    withCredentials([string(credentialsId: 'datadog-api-key', variable: 'DATADOG_API_KEY')]) {
        echo 'Hello world'
    }
}
```

### File

```shell script
gcloud secrets create file \
  --data-file=my-file.zip \
  --labels=jenkins-credentials-type=file,jenkins-credentials-file-extension=zip \
  --replication-policy=automatic \
  --project=my-project
```

### Username and Password

```shell script
echo -n 's3cr3t' | gcloud secrets create taylor-user \
  --data-file=- \
  --labels=jenkins-credentials-type=usernamePassword,jenkins-credentials-username=taylor \
  --replication-policy=automatic \
  --project=my-project
```

### SSH Key

```shell script
gcloud secrets create ssh-key \
  --data-file=id_rsa \
  --labels=jenkins-credentials-type=sshUserPrivateKey,jenkins-credentials-username=taylor \
  --replication-policy=automatic \
  --project=my-project
```

### Certificate

```shell script
gcloud secrets create certificate \
  --data-file=keystore \
  --labels=jenkins-credentials-type=certificate \
  --replication-policy=automatic \
  --project=my-project
```

## Limitations