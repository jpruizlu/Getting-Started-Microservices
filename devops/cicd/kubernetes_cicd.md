# Kubernetes CI/CC

## Tools

- Source Code: Github
- CI/CD: Jenkins/Spinnaker
- Artifacts : Nexus
- Images : Nexus
- Quality: SonarQube
- Testing: TBD

## Installation

### Pre-requsites

- Kubernetes cluster
- Helm v3

### Jenkins (Operator)

For the installation of Jenkins on kubernetes the Jenkins Operator is going to be used. For further information about the process or detailed information check the [Official Repository](https://github.com/jenkinsci/kubernetes-operator) or [Official WebSite](https://jenkinsci.github.io/kubernetes-operator/docs/installation/)

1. Create new namespace `cicd-tools` to deploy the CI/CD tools

    ```bash
    # Create a namespace called cicd
    kubectl create ns cicd-tools

    # Retrieve the namespaces
    kubectl get ns

    # Switch to previous namespace created
    kubectl config set-context --current --namespace=cicd-tools
    ```

2. Install Jenkins Custom Resource Definition into `cicd-tools`

    ```bash
    # Create the custom resources definitions used by Jenkins operator
    kubectl apply -f https://raw.githubusercontent.com/jenkinsci/kubernetes-operator/master/deploy/crds/jenkins_v1alpha2_jenkins_crd.yaml
    ```

3. Install Jenkins Operator via Helm v3.

    ```bash
    # Add Jenkins repositories to Helm
    helm3 repo add jenkins https://raw.githubusercontent.com/jenkinsci/kubernetes-operator/master/chart

    # Retrieve the helm repo list configured
    helm3 repo list

    # Update the helm repo
    helm3 repo update

    # Get the default values used to modify if necessary
    helm3 inspect values jenkins/jenkins-operator > jenkins-operator-values.yaml

    # Check the namespace set in the values, since it must be equal to the current namespace "cicd-tools"
    # Install jenkins operator
    helm3 install jenkins-operator jenkins/jenkins-operator -n cicd-tools jenkins-operator-values.yaml
    # Install jenkins operator
    helm3 install jenkins-operator jenkins/jenkins-operator -n cicd-tools --set jenkins.backup.enabled=false,jenkins.enabled=false
    # Or Install jenkins operator with values
    helm3 install jenkins-operator jenkins/jenkins-operator -n cicd-tools --set jenkins.backup.enabled=false,jenkins.namespace=cicd-tools

    # To add custom values using the install command, use the --set parameter and a list with the key/values separated by commas
    #helm3 install jenkins-operator jenkins/jenkins-operator -n cicd-tools --set jenkins.labels.LabelKey=LabelValue,jenkins.annotations.AnnotationKey=AnnotationValue
    ```

    Output:

    ```bash
    NOTES:
    1. Watch Jenkins instance being created:
    $ kubectl --namespace cicd-tools get pods -w

    1. Get Jenkins credentials:
    $ kubectl --namespace cicd-tools get secret jenkins-operator-credentials-jenkins -o 'jsonpath={.data.user}' | base64 -d
    $ kubectl --namespace cicd-tools get secret jenkins-operator-credentials-jenkins -o 'jsonpath={.data.password}' | base64 -d

    1. Connect to Jenkins (actual Kubernetes cluster):
    $ kubectl --namespace cicd-tools port-forward jenkins-jenkins 8080:8080

    Now open the browser and enter http://localhost:8080
    ```

4. Delete Jenkins Operator

    ```bash
    # Delete helm operator
    helm3 delete jenkins-operator
    ```

5. Create Jenkins Operator using the CRD

    Create a file with the documentation `jenkins-operator-instance.yaml`

    ```yaml
    apiVersion: jenkins.io/v1alpha2
    kind: Jenkins
    metadata:
      name: jenkins-instance
    spec:
      master:
        containers:
        - name: jenkins-master
          image: jenkins/jenkins:lts
          imagePullPolicy: Always
          livenessProbe:
            failureThreshold: 30
            httpGet:
              path: /login
              port: http
              scheme: HTTP
            initialDelaySeconds: 2048
            periodSeconds: 10
            successThreshold: 1
            timeoutSeconds: 5
          readinessProbe:
            failureThreshold: 30
            httpGet:
              path: /login
              port: http
              scheme: HTTP
            initialDelaySeconds: 2048
            periodSeconds: 10
            successThreshold: 1
            timeoutSeconds: 1
          resources:
            limits:
              cpu: 1500m
              memory: 3Gi
            requests:
              cpu: "1"
              memory: 500Mi
    ```

    Create the jenkins resources created previously.

    ```bash
    # Create Jenkins instance
    kubectl apply -f jenkins-operator-instance.yaml

    # Get the pods running
    kubectl get pods -w
    ```

### Nexus

1. Install Operator Lifecycle Manager (OLM), a tool to help manage the Operators running on the cluster.

    ```bash
    # Download and install OLM to manage operators
    curl -sL https://github.com/operator-framework/operator-lifecycle-manager/releases/download/0.16.1/install.sh | bash -s 0.16.1
    # verify OLM installation
    kubectl get ns
    kubectl get pods -n olm
    kubectl get pods -n operators
    ```

2. Install the operator by running the following command:

    >  This Operator will be installed in the "operators" namespace and will be usable from all namespaces in the cluster.

    ```bash
    # Install the operator in the default operators namespace 
    kubectl create -f https://operatorhub.io/install/nexus-operator-m88i.yaml

    # Download specific operator version
    VERSION=<version from GitHub releases page>
    kubectl apply -f https://github.com/m88i/nexus-operator/releases/download/${VERSION}/nexus-operator.yaml
    ```

3. After install, watch your operator come up using next command.

    ```bash
    # Get the resources created
    kubectl get csv -n cicd-tools

    NAME                    DISPLAY          VERSION   REPLACES                PHASE
    nexus-operator.v0.3.0   Nexus Operator   0.3.0     nexus-operator.v0.2.1   Succeeded
    ```

4. Create a file using the CRD to create a Nexus instance

   Create file `nexus-operator-instance.yaml` with following content.

    ```yaml
    apiVersion: apps.m88i.io/v1alpha1
    kind: Nexus
    metadata:
      name: nexus3
    spec:
      networking:
        expose: false
      persistence:
        persistent: false
      replicas: 1
      resources:
        limits:
          cpu: '2'
          memory: 2Gi
        requests:
          cpu: '1'
          memory: 2Gi
      useRedHatImage: false
    ```

     Create the nexus resources created previously.

    ```bash
    # Create nexus instance
    kubectl apply -f nexus-operator-instance.yaml

    # Get the pods running
    kubectl get pods -w
    ```

5. Verify Nexus installation

    ```bash
    # Connect to Nexus via 8081 port (admin/admin123)
    kubectl port-forward svc/nexus3 8081:8081
    ```

6. Delete Nexus Operator

    ```bash
    # Delete Nexus operator
    helm3 delete -f https://operatorhub.io/install/nexus-operator-m88i.yaml
    ```

### Sonarqube

Sonarqube hsa no operator. So the [helm chart](https://github.com/oteemo/charts) is used instead

1. Add `oteemo` helm repository

    ```bash
    # Add repo
    helm3 repo add oteemocharts https://oteemo.github.io/charts
    # Update repo
    helm3 repo update
    ```

2. Install Sonarqube with the default values

    ```bash
    # Install sonarqube
    helm3 install sonarqube oteemocharts/sonarqube -n cicd-tools

    # Wait unitl sonarqube pods is running
    kubectl get pods -w
    ```

3. Verify Sonarqube installation

    ```bash
    # Use prot-forward to connecto to sonarqube
    kubectl port-forward svc/sonarqube-sonarqube 9000:9000
    ```

4. Delete helm chart

    ```bash
    # Install sonarqube
    helm3 delete sonarqube
    ```

### Jenkins (Chaart)

1. Add `jenkins` helm repository

    ```bash
    # Add repo
    helm3 repo add jenkinscharts https://charts.jenkins.io
    # Update repo
    helm3 repo update
    ```

2. Install Jenkins with the default values

    ```bash
    # Install Jenkins
    helm3 install jenkins jenkinscharts/jenkins -n cicd-tools

    # Install with values
    helm3 install jenkins jenkinscharts/jenkins -n cicd-tools --set master.JCasC.enabled=false,master.JCasC.defaultConfig=false,master.sidecars.configAutoReload.enabled=false

    # Wait unitl Jenkins pods is running
    kubectl get pods -w
    ```

3. Verify Jenkins installation

    ```bash
    # Use prot-forward to connecto to Jenkins
    kubectl port-forward svc/jenkins 8080:8080
    ```

4. Delete helm chart

    ```bash
    # Install Jenkins
    helm3 delete jenkins
    ```
