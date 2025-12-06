#!/bin/bash

# Kafka initialization script
# This script waits for Kafka to be ready and then creates topics

echo "Waiting for Kafka to be ready..."

# Wait for Kafka to be available
cub kafka-ready -b localhost:9092 1 60

echo "Kafka is ready. Creating topics..."

# Create link_clicks topic
kafka-topics --create \
  --if-not-exists \
  --topic link_clicks \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1 \
  --config retention.ms=604800000 \
  --config compression.type=lz4

echo "Topics created successfully!"
