require 'aws-sdk-secretsmanager'

asm = Aws::SecretsManager::Client.new(region: ENV['AWS_REGION'], endpoint: 'http://secretsmanager:4566')

# Wait for localstack to be available
begin
  retries ||= 0
  asm.list_secrets
rescue Seahorse::Client::NetworkingError
  sleep 1
  retry if (retries += 1) < 20
end

asm.create_secret(name: 'THUNDER_AUTH_SECRET', secret_string: 'secret')

puts 'Created THUNDER_AUTH_SECRET.'
