package test

import (
	"path/filepath"
	"testing"

	"github.com/stretchr/testify/require"
	corev1 "k8s.io/api/core/v1"

	"github.com/gruntwork-io/terratest/modules/helm"
	"gopkg.in/yaml.v2"
)

type ThunderConfig struct {
	Server       Server       `yaml:"server"`
	Database     Database     `yaml:"database"`
	Email        Email        `yaml:"email"`
	Auth         Auth         `yaml:"auth"`
	PasswordHash PasswordHash `yaml:"passwordHash"`
	Properties   []Properties `yaml:"properties"`
	Openapi      Openapi      `yaml:"openapi"`
}

type Server struct {
	ApplicationConnectors []ApplicationConnectors `yaml:"applicationConnectors"`
	AdminConnectors       []AdminConnectors       `yaml:"adminConnectors"`
}

type Database struct {
	Type             string `yaml:"type"`
	Endpoint         string `yaml:"endpoint"`
	Region           string `yaml:"region"`
	TableName        string `yaml:"tableName"`
	ConnectionString string `yaml:"connectionString"`
	DatabaseName     string `yaml:"databaseName"`
	CollectionName   string `yaml:"collectionName"`
}

type Keys struct {
	Secret      string `yaml:"secret"`
	Application string `yaml:"application"`
}

type Properties struct {
	Name string `yaml:"name"`
	Type string `yaml:"type"`
}

type ApplicationConnectors struct {
	Type string `yaml:"type"`
	Port int    `yaml:"port"`
}

type AdminConnectors struct {
	Type string `yaml:"type"`
	Port int    `yaml:"port"`
}

type Email struct {
	MessageOptions MessageOptions `yaml:"messageOptions"`
	Type           string         `yaml:"type"`
	Endpoint       string         `yaml:"endpoint"`
	Region         string         `yaml:"region"`
	FromAddress    string         `yaml:"fromAddress"`
}

type MessageOptions struct {
	Subject              string `yaml:"subject"`
	BodyHtmlFilePath     string `yaml:"bodyHtmlFilePath"`
	BodyTextFilePath     string `yaml:"bodyTextFilePath"`
	UrlPlaceholderString string `yaml:"urlPlaceholderString"`
	SuccessHtmlFilePath  string `yaml:"successHtmlFilePath"`
}

type Auth struct {
	Type string `yaml:"type"`
	Keys []Keys `yaml:"keys"`
}

type PasswordHash struct {
	Algorithm           string `yaml:"algorithm"`
	ServerSideHash      bool   `yaml:"serverSideHash"`
	HeaderCheck         bool   `yaml:"headerCheck"`
	AllowCommonMistakes bool   `yaml:"allowCommonMistakes"`
}

type Openapi struct {
	Title        string `yaml:"title"`
	Description  string `yaml:"description"`
	Contact      string `yaml:"contact"`
	ContactEmail string `yaml:"contactEmail"`
	License      string `yaml:"license"`
	LicenseUrl   string `yaml:"licenseUrl"`
	Enabled      bool   `yaml:"enabled"`
}

func TestConfigmapDynamodb(t *testing.T) {
  t.Parallel()

  // Get the filepath to the chart
  helmChartPath, err := filepath.Abs("../thunder")
  require.NoError(t, err)

  // Define values.yaml args for this test
  options := &helm.Options{
    SetValues: map[string]string{
      "database.type": "dynamodb",
      "database.endpoint": "http://dynamo-endpoint",
      "database.region": "testRegion",
      "database.tableName": "testTableName",
    },
  }

  // Render the template
  output := helm.RenderTemplate(t, options, helmChartPath, "thunder", []string{"templates/configmap.yaml"})

  // Convert to configmap struct
  var configmap corev1.ConfigMap
  helm.UnmarshalK8SYaml(t, output, &configmap)

  var thunderconfig ThunderConfig
  require.NoError(t, yaml.Unmarshal([]byte(configmap.Data["config.yaml"]), &thunderconfig))

  // Verify
  require.Equal(t, "dynamodb", thunderconfig.Database.Type)
  require.Equal(t, "http://dynamo-endpoint", thunderconfig.Database.Endpoint)
  require.Equal(t, "testRegion", thunderconfig.Database.Region)
  require.Equal(t, "testTableName", thunderconfig.Database.TableName)

  require.Equal(t, "", thunderconfig.Email.Type)
}

func TestConfigmapMongodb(t *testing.T) {
  t.Parallel()

  // Get the filepath to the chart
  helmChartPath, err := filepath.Abs("../thunder")
  require.NoError(t, err)

  // Define values.yaml args for this test
  options := &helm.Options{
    SetValues: map[string]string{
      "database.type": "mongodb",
      "database.connectionString": "http://mongo-conn",
      "database.databaseName": "testDBName",
      "database.collectionName": "testCollName",
    },
  }

  // Render the template
  output := helm.RenderTemplate(t, options, helmChartPath, "thunder", []string{"templates/configmap.yaml"})

  // Convert to configmap struct
  var configmap corev1.ConfigMap
  helm.UnmarshalK8SYaml(t, output, &configmap)

  var thunderconfig ThunderConfig
  require.NoError(t, yaml.Unmarshal([]byte(configmap.Data["config.yaml"]), &thunderconfig))

  // Verify
  require.Equal(t, "mongodb", thunderconfig.Database.Type)
  require.Equal(t, "http://mongo-conn", thunderconfig.Database.ConnectionString)
  require.Equal(t, "testDBName", thunderconfig.Database.DatabaseName)
  require.Equal(t, "testCollName", thunderconfig.Database.CollectionName)

  require.Equal(t, "", thunderconfig.Email.Type)
}

func TestEmail(t *testing.T) {
  t.Parallel()

  // Get the filepath to the chart
  helmChartPath, err := filepath.Abs("../thunder")
  require.NoError(t, err)

  // Define values.yaml args for this test
  options := &helm.Options{
    SetValues: map[string]string{
      "emailEnabled": "true",
      "emailEndpoint": "http://ses",
      "emailRegion": "myTestRegion",
      "emailFromAddress": "address@gmail.com",
      "messageOptionsSubject": "testSubj",
      "messageOptionsBodyHtmlFilePath": "testBodyHtmlPath",
      "messageOptionsBodyTextFilePath": "testBodyTextPath",
      "messageOptionsUrlPlaceholderString": "url-placeholder",
      "messageOptionsSuccessHtmlFilePath": "testSuccessHtmlPath",
    },
  }

  // Render the template
  output := helm.RenderTemplate(t, options, helmChartPath, "thunder", []string{"templates/configmap.yaml"})

  // Convert to configmap struct
  var configmap corev1.ConfigMap
  helm.UnmarshalK8SYaml(t, output, &configmap)

  var thunderconfig ThunderConfig
  require.NoError(t, yaml.Unmarshal([]byte(configmap.Data["config.yaml"]), &thunderconfig))

  // Verify
  require.Equal(t, "ses", thunderconfig.Email.Type)
  require.Equal(t, "http://ses", thunderconfig.Email.Endpoint)
  require.Equal(t, "myTestRegion", thunderconfig.Email.Region)
  require.Equal(t, "address@gmail.com", thunderconfig.Email.FromAddress)

  require.Equal(t, "testSubj", thunderconfig.Email.MessageOptions.Subject)
  require.Equal(t, "testBodyHtmlPath", thunderconfig.Email.MessageOptions.BodyHtmlFilePath)
  require.Equal(t, "testBodyTextPath", thunderconfig.Email.MessageOptions.BodyTextFilePath)
  require.Equal(t, "url-placeholder", thunderconfig.Email.MessageOptions.UrlPlaceholderString)
  require.Equal(t, "testSuccessHtmlPath", thunderconfig.Email.MessageOptions.SuccessHtmlFilePath)
}

func TestAuthKeys(t *testing.T) {
  t.Parallel()

  // Get the filepath to the chart
  helmChartPath, err := filepath.Abs("../thunder")
  require.NoError(t, err)

  // Define values.yaml args for this test
  options := &helm.Options{
    SetValues: map[string]string{
      "applicationKeys[0].name": "myname",
      "applicationKeys[0].secret": "mysecret",
      "applicationKeys[1].name": "mysecondname",
      "applicationKeys[1].secret": "mysecondsecret",
    },
  }

  // Render the template
  output := helm.RenderTemplate(t, options, helmChartPath, "thunder", []string{"templates/configmap.yaml"})

  // Convert to configmap struct
  var configmap corev1.ConfigMap
  helm.UnmarshalK8SYaml(t, output, &configmap)

  var thunderconfig ThunderConfig
  require.NoError(t, yaml.Unmarshal([]byte(configmap.Data["config.yaml"]), &thunderconfig))

  // Verify
  require.Equal(t, "basic", thunderconfig.Auth.Type)
  require.Equal(t, 2, len(thunderconfig.Auth.Keys))

  require.Equal(t, "myname", thunderconfig.Auth.Keys[0].Application)
  require.Equal(t, "mysecret", thunderconfig.Auth.Keys[0].Secret)
  require.Equal(t, "mysecondname", thunderconfig.Auth.Keys[1].Application)
  require.Equal(t, "mysecondsecret", thunderconfig.Auth.Keys[1].Secret)
}

func TestPasswordHash(t *testing.T) {
  t.Parallel()

  // Get the filepath to the chart
  helmChartPath, err := filepath.Abs("../thunder")
  require.NoError(t, err)

  // Define values.yaml args for this test
  options := &helm.Options{
    SetValues: map[string]string{
      "passwordHashAlgorithm": "bcrypt",
      "passwordHashServerSideHash": "true",
      "passwordHashHeaderCheck": "false",
      "passwordHashAllowCommonMistakes": "true",
    },
  }

  // Render the template
  output := helm.RenderTemplate(t, options, helmChartPath, "thunder", []string{"templates/configmap.yaml"})

  // Convert to configmap struct
  var configmap corev1.ConfigMap
  helm.UnmarshalK8SYaml(t, output, &configmap)

  var thunderconfig ThunderConfig
  require.NoError(t, yaml.Unmarshal([]byte(configmap.Data["config.yaml"]), &thunderconfig))

  // Verify
  require.Equal(t, "bcrypt", thunderconfig.PasswordHash.Algorithm)
  require.Equal(t, true, thunderconfig.PasswordHash.ServerSideHash)
  require.Equal(t, false, thunderconfig.PasswordHash.HeaderCheck)
  require.Equal(t, true, thunderconfig.PasswordHash.AllowCommonMistakes)
}

func TestProperties(t *testing.T) {
  t.Parallel()

  // Get the filepath to the chart
  helmChartPath, err := filepath.Abs("../thunder")
  require.NoError(t, err)

  // Define values.yaml args for this test
  options := &helm.Options{
    SetValues: map[string]string{
      "properties[0].name": "prop1",
      "properties[0].type": "list",
      "properties[1].name": "prop2",
      "properties[1].type": "string",
    },
  }

  // Render the template
  output := helm.RenderTemplate(t, options, helmChartPath, "thunder", []string{"templates/configmap.yaml"})

  // Convert to configmap struct
  var configmap corev1.ConfigMap
  helm.UnmarshalK8SYaml(t, output, &configmap)

  var thunderconfig ThunderConfig
  require.NoError(t, yaml.Unmarshal([]byte(configmap.Data["config.yaml"]), &thunderconfig))

  // Verify
  require.Equal(t, 2, len(thunderconfig.Properties))

  require.Equal(t, "prop1", thunderconfig.Properties[0].Name)
  require.Equal(t, "list", thunderconfig.Properties[0].Type)
  require.Equal(t, "prop2", thunderconfig.Properties[1].Name)
  require.Equal(t, "string", thunderconfig.Properties[1].Type)
}

func TestOpenApi(t *testing.T) {
  t.Parallel()

  // Get the filepath to the chart
  helmChartPath, err := filepath.Abs("../thunder")
  require.NoError(t, err)

  // Define values.yaml args for this test
  options := &helm.Options{
    SetValues: map[string]string{
      "openApiEnabled": "true",
      "openApiTitle": "apiTitle",
      "openApiDescription": "apiDescription",
      "openApiContact": "apiContact",
      "openApiContactEmail": "apiContactEmail",
      "openApiLicense": "apiLicense",
      "openApiLicenseUrl": "apiLicenseUrl",
    },
  }

  // Render the template
  output := helm.RenderTemplate(t, options, helmChartPath, "thunder", []string{"templates/configmap.yaml"})

  // Convert to configmap struct
  var configmap corev1.ConfigMap
  helm.UnmarshalK8SYaml(t, output, &configmap)

  var thunderconfig ThunderConfig
  require.NoError(t, yaml.Unmarshal([]byte(configmap.Data["config.yaml"]), &thunderconfig))

  // Verify
  require.Equal(t, true, thunderconfig.Openapi.Enabled)

  require.Equal(t, "apiTitle", thunderconfig.Openapi.Title)
  require.Equal(t, "apiDescription", thunderconfig.Openapi.Description)
  require.Equal(t, "apiContact", thunderconfig.Openapi.Contact)
  require.Equal(t, "apiContactEmail", thunderconfig.Openapi.ContactEmail)
  require.Equal(t, "apiLicense", thunderconfig.Openapi.License)
  require.Equal(t, "apiLicenseUrl", thunderconfig.Openapi.LicenseUrl)
}

func TestServerConfig(t *testing.T) {
  t.Parallel()

  // Get the filepath to the chart
  helmChartPath, err := filepath.Abs("../thunder")
  require.NoError(t, err)

  // Define values.yaml args for this test
  options := &helm.Options{
    SetValues: map[string]string{
      "serviceBackendPort": "8000",
      "serviceAdminPort": "8001",
    },
  }

  // Render the template
  output := helm.RenderTemplate(t, options, helmChartPath, "thunder", []string{"templates/configmap.yaml"})

  // Convert to configmap struct
  var configmap corev1.ConfigMap
  helm.UnmarshalK8SYaml(t, output, &configmap)

  var thunderconfig ThunderConfig
  require.NoError(t, yaml.Unmarshal([]byte(configmap.Data["config.yaml"]), &thunderconfig))

  // Verify
  require.Equal(t, 1, len(thunderconfig.Server.ApplicationConnectors))
  require.Equal(t, 8000, thunderconfig.Server.ApplicationConnectors[0].Port)
  require.Equal(t, 8001, thunderconfig.Server.AdminConnectors[0].Port)
}
