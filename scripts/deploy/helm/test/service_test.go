package test

import (
	"path/filepath"
	"testing"

	"github.com/stretchr/testify/require"
	corev1 "k8s.io/api/core/v1"

	"github.com/gruntwork-io/terratest/modules/helm"
)

// Test all of the values in the service template
func TestService(t *testing.T) {
  t.Parallel()

  // Get the filepath to the chart
  helmChartPath, err := filepath.Abs("../thunder")
  require.NoError(t, err)

  // Define values.yaml args for this test
  options := &helm.Options{
    SetValues: map[string]string{
      "serviceType": "LoadBalancer",
      "serviceFrontendPort": "5000",
      "serviceBackendPort": "5001",
    },
  }

  // Render the template
  output := helm.RenderTemplate(t, options, helmChartPath, "thunder", []string{"templates/service.yaml"})

  // Convert to service struct
  var service corev1.Service
  helm.UnmarshalK8SYaml(t, output, &service)

  // Verify
  require.Equal(t, corev1.ServiceType("LoadBalancer"), service.Spec.Type)
  require.Equal(t, 1, len(service.Spec.Ports))
  require.Equal(t, int32(5000), service.Spec.Ports[0].Port)
  require.Equal(t, 5001, service.Spec.Ports[0].TargetPort.IntValue())
}
