#!/usr/bin/env bash
echo "Create Kubernetes Cluster (KVM)"

echo "- Loading the entire kubernetes cluster"
WORKER_COUNT=2 vagrant up  

echo "- Performing the post-process"
vagrant ssh k8s-master -c "sudo bash /vagrant/post-provision/all-install.sh"

echo ""
echo "-- Process Finished --"
echo ""
echo "- You can access to the following nodes via ssh on ansible"
echo ""
vagrant status

echo ""
echo "- Run the following command on k8s-master to access to the dashboard:"
echo ""
echo "     sudo kubectl proxy --port=9999 --address=10.0.0.11 --accept-hosts='^*$' & "
echo ""
echo "  or"
echo ""
echo "     vagrant ssh k8s-master -c 'sudo kubectl proxy --port=9999 --address=10.0.0.11 --accept-hosts='^*$' &'' " 
echo ""
echo "  Following is the link to access to the dashboard:"
echo ""
echo "      http://10.0.0.11:9999/api/v1/namespaces/kube-system/services/https:kubernetes-dashboard:/proxy/#!/login"

