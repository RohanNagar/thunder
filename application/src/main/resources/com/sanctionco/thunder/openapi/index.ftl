<#-- @ftlvariable name="" type="com.sanctionco.thunder.openapi.SwaggerView" -->
<!-- HTML for static distribution bundle build -->
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8">
    <title>${title}</title>
    <link rel="stylesheet" type="text/css" href="${swaggerAssetsPath}/swagger-ui.css" >
    <link rel="icon" type="image/png" href="${swaggerAssetsPath}/favicon-32x32.png" sizes="32x32" />
    <link rel="icon" type="image/png" href="${swaggerAssetsPath}/favicon-16x16.png" sizes="16x16" />
    <style>
      html
      {
        box-sizing: border-box;
        overflow: -moz-scrollbars-vertical;
        overflow-y: scroll;
      }

      *,
      *:before,
      *:after
      {
        box-sizing: inherit;
      }

      body
      {
        margin:0;
        background: #fafafa;
      }
    </style>
  </head>

  <body>
    <div id="swagger-ui"></div>

    <script src="${swaggerAssetsPath}/swagger-ui-bundle.js"> </script>
    <script src="${swaggerAssetsPath}/swagger-ui-standalone-preset.js"> </script>
    <script>
    window.onload = function() {

      // Begin Swagger UI call region
      const ui = SwaggerUIBundle({
        url: "${contextPath}/openapi.json",
        <#if validatorUrl??>
        validatorUrl: "${validatorUrl}",
        <#else>
        validatorUrl: null,
        </#if>
        dom_id: "#swagger-ui",
        deepLinking: true,
        supportedSubmitMethods: ["get", "post", "put", "delete", "patch"],
        docExpansion: "none",
        jsonEditor: false,
        tagsSorter: "alpha",
        operationsSorter: "alpha",
        defaultModelRendering: "schema",
        showRequestHeaders: false,
        presets: [
          SwaggerUIBundle.presets.apis,
          SwaggerUIStandalonePreset
        ],
        plugins: [
          SwaggerUIBundle.plugins.DownloadUrl
        ],
        layout: "StandaloneLayout"
      });
      // End Swagger UI call region

      window.ui = ui
    }
  </script>
  </body>
</html>
