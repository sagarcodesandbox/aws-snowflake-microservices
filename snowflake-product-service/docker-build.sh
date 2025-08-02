#!/bin/bash

# Snowflake Product Service Docker Build Script

# Set variables
IMAGE_NAME="snowflake-product-service"
TAG="latest"
CONTAINER_NAME="snowflake-product-service-container"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}Building Docker image for Snowflake Product Service...${NC}"

# Build the Docker image
docker build -t ${IMAGE_NAME}:${TAG} .

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Docker image built successfully!${NC}"
    
    # Stop and remove existing container if it exists
    echo -e "${YELLOW}Stopping existing container if running...${NC}"
    docker stop ${CONTAINER_NAME} 2>/dev/null || true
    docker rm ${CONTAINER_NAME} 2>/dev/null || true
    
    echo -e "${GREEN}Running the container...${NC}"
    # Run the container
    docker run -d \
        --name ${CONTAINER_NAME} \
        -p 8080:8080 \
        --restart unless-stopped \
        ${IMAGE_NAME}:${TAG}
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Container started successfully!${NC}"
        echo -e "${GREEN}Application is running at: http://localhost:8080${NC}"
        echo -e "${GREEN}Health check: http://localhost:8080/actuator/health${NC}"
        echo -e "${YELLOW}To view logs: docker logs ${CONTAINER_NAME}${NC}"
        echo -e "${YELLOW}To stop: docker stop ${CONTAINER_NAME}${NC}"
    else
        echo -e "${RED}✗ Failed to start container${NC}"
        exit 1
    fi
else
    echo -e "${RED}✗ Docker build failed${NC}"
    exit 1
fi
