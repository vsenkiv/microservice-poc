#!/bin/bash

# Wait for Kibana to be ready
echo "Waiting for Kibana to be ready..."
until curl -s http://localhost:5601/api/status | grep -q '"overall":{"level":"available"}'
do
  echo "Kibana is not ready yet. Waiting..."
  sleep 10
done

echo "Kibana is ready. Setting up index patterns..."

# Create index pattern for microservices logs
curl -X POST "localhost:5601/api/saved_objects/index-pattern/microservices-logs" \
  -H "Content-Type: application/json" \
  -H "kbn-xsrf: true" \
  -d '{
    "attributes": {
      "title": "microservices-logs-*",
      "timeFieldName": "@timestamp"
    }
  }'

# Create index pattern for docker logs
curl -X POST "localhost:5601/api/saved_objects/index-pattern/docker-logs" \
  -H "Content-Type: application/json" \
  -H "kbn-xsrf: true" \
  -d '{
    "attributes": {
      "title": "docker-logs-*",
      "timeFieldName": "@timestamp"
    }
  }'

echo "Index patterns created successfully!"