# GCP Secrets Manager Credentials Provider

[![GCP Secrets Manager Credentials Provider Plugin](https://img.shields.io/jenkins/plugin/v/gcp-secrets-manager-credentials-provider?style=flat-square)](https://plugins.jenkins.io/gcp-secrets-manager-credentials-provider/)

Access credentials from [Google Cloud Secrets Manager](https://cloud.google.com/secret-manager) in your Jenkins jobs.

## Setup

Enable the Secrets Manager API via the GCP console or by running:

```shell script
gcloud services enable secretmanager.googleapis.com --project=my-project
```

Follow the [documentation](https://cloud.google.com/secret-manager/docs/creating-and-accessing-secrets#secretmanager-create-secret-cli) for 
creating and updating secrets.

## Usage

To enable the plugin, go to "Configure System" and find the "GCP Secrets Manager" section.
Input the name of the GCP projects that contain the secrets.

Secret names (not values) are cached in-memory for 5 minutes. This is not currently configurable.

Secrets created in GCP Secret Manager must have the label with key `jenkins-credentials-type` and one of the following values:

* string
* file
* username-password
* ssh-user-private-key
* certificate

### IAM

Give Jenkins read access to the Secrets Manager with an [Google Cloud IAM policy](https://cloud.google.com/iam/docs).

At minimum, give Jenkins an IAM role with the following permissions:

* secretmanager.secrets.list (project-level)
* secretmanager.secrets.get (project-level)
* secretmanager.versions.list
* secretmanager.versions.get
* secretmanager.versions.access

The easiest option is to give the Jenkins service account the pre-built roles `roles/secretmanager.secretAccessor` and 
`roles/secretmanager.viewer` at the project-level.

Jenkins will attempt to list all secrets for the configured projects. If it doesn't have access to list secrets in the projects,
no secrets will be added to the credential store.

If you are running Jenkins on GCP, attach a [default service account](https://cloud.google.com/iam/docs/service-accounts#default)
to the instance running Jenkins. You can use [Workload Identity](https://cloud.google.com/kubernetes-engine/docs/how-to/workload-identity) 
if running Jenkins on Google Kubernetes Engine.

If you are not running Jenkins on GCP, set the environment variable `GOOGLE_APPLICATION_CREDENTIALS` for the Jenkins process
to the path of a [JSON service account key](https://cloud.google.com/iam/docs/creating-managing-service-account-keys) with the above permissions.

When using JSON service account keys, both the master and agents must have the environment variable `GOOGLE_APPLICATION_CREDENTIALS`
set to an accessible file. For example, when using the Kubernetes plugin it is recommended to provide 
a secret volume that mounts the file into the agent pod:

```groovy
podTemplate(yaml: """
apiVersion: v1
kind: Pod
metadata:
  labels:
    some-label: some-label-value
spec:
  containers:
  - name: busybox
    image: busybox
    env: 
    - name: GOOGLE_APPLICATION_CREDENTIALS 
      value: /jenkins/sa.json 
    volumeMounts: 
    - name: gcp-sa-secret
      mountPath: "/jenkins" 
      readOnly: true
  volumes:
  - name: gcp-sa-secret
    secret:
      secretName: gcp-sa-secret 
"""
) {
    node(POD_LABEL) {
        ...
    }
}
```

Where the secret was created with the following command:

```shell script
kubectl create secret generic gcp-sa-secret --from-file=/tmp/sa.json
```

### Filtering

If you are sharing a GCP project across multiple Jenkins instances, you can use the filtering feature to control which
secrets get added to the credential store. This feature allows you to specify a custom label and value(s) that each 
secret must have in order to be added to the store. Note that Jenkins will still need IAM permissions to list and get all other secrets - 
GCP Secrets Manager does not currently support "server-side" filtering.

You can use a comma-separated string for the label value, which will tell Jenkins to add the secret to the store
if it matches any of the provided values.

### JCasC

You can use [JCasC](https://www.jenkins.io/projects/jcasc/) to set the GCP project and label filters.

```yaml
unclassified:
  gcpCredentialsProvider:
    filter:
      label: "my-label"
      value: "my-value-1,my-value-2"
    project: "my-gcp-project1,my-gcp-project2"
```

## Examples

### Secret Text

Set the label `jenkins-credentials-type=string` to use the credential type.

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

Set the label `jenkins-credentials-type=file` to use the credential type.

Additional labels:

* jenkins-credentials-filename
* jenkins-credentials-file-extension

```shell script
gcloud secrets create serviceacount \
  --data-file=my-file.json \
  --labels=jenkins-credentials-type=file,jenkins-credentials-filename=serviceaccount,jenkins-credentials-file-extension=json \
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

Set the label `jenkins-credentials-type=username-password` to use the credential type.

Additional labels:

* jenkins-credentials-username

```shell script
echo -n 's3cr3t' | gcloud secrets create nexus-creds \
  --data-file=- \
  --labels=jenkins-credentials-type=username-password,jenkins-credentials-username=nexus-user \
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

Set the label `jenkins-credentials-type=ssh-user-private-key` to use the credential type.

Additional labels:

* jenkins-credentials-username

```shell script
gcloud secrets create ssh-key \
  --data-file=id_rsa \
  --labels=jenkins-credentials-type=ssh-user-private-key,jenkins-credentials-username=taylor \
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

Set the label `jenkins-credentials-type=certificate` to use the credential type.

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

* Labels must contain only hyphens (-), underscores (_), lowercase characters, and numbers. Any usernames or 
filenames in labels that have other characters will not be allowed.

* The secret manager API does not support server-side filtering. 

* The secret manager API does not support descriptions. The description of the secret will be the 
same as the id.
