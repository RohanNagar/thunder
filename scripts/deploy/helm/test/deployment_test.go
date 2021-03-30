package test

import (
	"path/filepath"
	"testing"

	"github.com/stretchr/testify/require"
	appsv1 "k8s.io/api/apps/v1"

	"github.com/gruntwork-io/terratest/modules/helm"
)

// Test all of the values in the deployment template without a mongo container
func TestDeploymentWithoutMongoContainer(t *testing.T) {
  t.Parallel()

  // Get the filepath to the chart
  helmChartPath, err := filepath.Abs("../thunder")
  require.NoError(t, err)

  // Define values.yaml args for this test
  options := &helm.Options{
    SetValues: map[string]string{
      "thunderImageTag": "2.0.0",
      "replicaCount": "3",
      "awsAccessKeyId": "access-key",
      "awsSecretAccessKey": "secret-key",
      "serviceBackendPort": "9000",
      "serviceAdminPort": "9001",
      "database.useLocal": "false",
    },
  }

  // Render the template
  output := helm.RenderTemplate(t, options, helmChartPath, "thunder", []string{"templates/deployment.yaml"})

  // Convert to deployment struct
  var deployment appsv1.Deployment
  helm.UnmarshalK8SYaml(t, output, &deployment)

  // Verify
  deploymentContainers := deployment.Spec.Template.Spec.Containers

  require.Equal(t, int32(3), *deployment.Spec.Replicas)

  require.Equal(t, 1, len(deploymentContainers))
  require.Equal(t, "rohannagar/thunder:2.0.0", deploymentContainers[0].Image)

  require.Equal(t, 2, len(deploymentContainers[0].Env))
  require.Equal(t, "access-key", deploymentContainers[0].Env[0].Value)
  require.Equal(t, "secret-key", deploymentContainers[0].Env[1].Value)

  require.Equal(t, 2, len(deploymentContainers[0].Ports))
  require.Equal(t, int32(9000), deploymentContainers[0].Ports[0].ContainerPort)
  require.Equal(t, int32(9001), deploymentContainers[0].Ports[1].ContainerPort)

  require.Equal(t, 9001, deploymentContainers[0].LivenessProbe.HTTPGet.Port.IntValue())
  require.Equal(t, 9001, deploymentContainers[0].ReadinessProbe.HTTPGet.Port.IntValue())
}

// Test including a mongo container
func TestDeploymentWithMongoContainer(t *testing.T) {
  t.Parallel()

  // Get the filepath to the chart
  helmChartPath, err := filepath.Abs("../thunder")
  require.NoError(t, err)

  // Define values.yaml args for this test
  options := &helm.Options{
    SetValues: map[string]string{
      "database.useLocal": "true",
    },
  }

  // Render the template
  output := helm.RenderTemplate(t, options, helmChartPath, "thunder", []string{"templates/deployment.yaml"})

  // Convert to deployment struct
  var deployment appsv1.Deployment
  helm.UnmarshalK8SYaml(t, output, &deployment)

  // Verify
  deploymentContainers := deployment.Spec.Template.Spec.Containers

  require.Equal(t, int32(1), *deployment.Spec.Replicas)

  require.Equal(t, 2, len(deploymentContainers))
  require.Equal(t, "mongo:latest", deploymentContainers[0].Image)
  require.Equal(t, "rohannagar/thunder:edge", deploymentContainers[1].Image)
}
