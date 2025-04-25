#!/bin/bash

# Exit immediately if a command exits with a non-zero status.
set -e

# --- Configuration ---
# Directory where Gradle build output libs are located
GRADLE_LIBS_DIR="build/libs"
# Directory used for Docker dependency layering
DOCKER_DEP_DIR="build/dependency"
# Pattern to find the Spring Boot executable JAR (adjust if needed)
JAR_PATTERN="*SNAPSHOT.jar"

# --- Argument Handling ---
# Check if exactly one argument (image name with tag) is provided
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <repository>/<image_name>:<tag>"
    echo "Example: $0 narutosimaha/rate-limiter:1.0.1"
    exit 1
fi

# Store the full image name from the first argument
FULL_IMAGE_NAME=$1

# --- Script Logic ---

echo "--- Starting Build and Push Process for Image: ${FULL_IMAGE_NAME} ---"

# 1. Run Gradle build
echo "Step 1: Running Gradle build..."
./gradlew build -x test

# 2. Prepare Docker dependency layer
echo "Step 2: Preparing Docker dependency layer..."
# Remove the directory if it exists to ensure a clean state
rm -rf "${DOCKER_DEP_DIR}"
# Create the dependency directory
mkdir -p "${DOCKER_DEP_DIR}"
# Extract the JAR contents into the dependency directory
# Using find to handle potential variations in JAR names more robustly
JAR_FILE=$(find "${GRADLE_LIBS_DIR}" -maxdepth 1 -name "${JAR_PATTERN}" -print -quit)
if [ -z "${JAR_FILE}" ]; then
    echo "Error: Could not find JAR file matching '${JAR_PATTERN}' in '${GRADLE_LIBS_DIR}'"
    exit 1
fi
echo "Extracting ${JAR_FILE} into ${DOCKER_DEP_DIR}..."
(cd "${DOCKER_DEP_DIR}"; jar -xf "../libs/$(basename ${JAR_FILE})")

# 3. Build Docker image
echo "Step 3: Building Docker image '${FULL_IMAGE_NAME}'..."
docker build --build-arg DEPENDENCY="${DOCKER_DEP_DIR}" -t "${FULL_IMAGE_NAME}" .

# 4. Push Docker image
echo "Step 4: Pushing Docker image '${FULL_IMAGE_NAME}'..."
docker push "${FULL_IMAGE_NAME}"

echo "--- Build and Push Process Completed Successfully ---"

exit 0
