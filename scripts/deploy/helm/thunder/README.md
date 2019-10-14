# Thunder Helm Chart

The Thunder Helm chart provides an easy way to deploy Thunder to a Kuberentes cluster.

1. Edit `values.yaml` to provide your desired values

2. Make sure you have `kubectl` and `helm` installed on your machine.

3. Connect to your Kubernetes cluster.

4. Run `helm install --name thunder scripts/deploy/helm/thunder/`

5. Wait a bit for the service to come up, and then Thunder is ready!

To delete the entire deployment, run `helm del --purge thunder`
