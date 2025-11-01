#!/bin/bash

# Script to create Kafka topics for URL Shortener
# Run this after docker-compose up

set -e

KAFKA_CONTAINER="urlshortener-kafka"
BOOTSTRAP_SERVER="localhost:9092"

echo "Creating Kafka topics..."

# Create link_clicks topic
docker exec $KAFKA_CONTAINER /opt/kafka/bin/kafka-topics.sh \
  --create \
  --topic link_clicks \
  --bootstrap-server $BOOTSTRAP_SERVER \
  --partitions 3 \
  --replication-factor 1 \
  --config retention.ms=604800000 \
  --config compression.type=lz4 \
  --if-not-exists

echo "✓ Created topic: link_clicks"

# Verify topics were created
echo ""
echo "Existing topics:"
docker exec $KAFKA_CONTAINER /opt/kafka/bin/kafka-topics.sh \
  --list \
  --bootstrap-server $BOOTSTRAP_SERVER

# Show topic details
echo ""
echo "Topic details:"
docker exec $KAFKA_CONTAINER /opt/kafka/bin/kafka-topics.sh \
  --describe \
  --topic link_clicks \
  --bootstrap-server $BOOTSTRAP_SERVER

echo ""
echo "✓ Kafka topics setup complete!"
