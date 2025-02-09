#!/bin/bash

# Run Gradle dependencies and extract sponge-mixin versions
MIXIN_VERSIONS=$(./gradlew dependencies | grep 'sponge-mixin' | sort)

# Check if we found versions
if [[ -z "$MIXIN_VERSIONS" ]]; then
    echo "❌ Failed to find sponge-mixin versions in Gradle dependencies!" >&2
    exit 1
fi

# Find the most frequently occurring version
MIXIN_VERSION=$(echo "$MIXIN_VERSIONS" | uniq -c | sort -nr | head -n 1 | awk '{print $3}')

# Replace all colons ":" with slashes "/"
MIXIN_VERSION=${MIXIN_VERSION//:/\/}

echo "✅ Most common sponge-mixin version: $MIXIN_VERSION" >&2

# Find the JAR matching the version
MIXIN_JAR=$(find ~/.gradle/caches -type f -name "sponge-mixin-*.jar" ! -name "*-sources.jar" ! -name "*-javadoc.jar" | grep $MIXIN_VERSION)

# Check if the JAR exists
if [[ -z "$MIXIN_JAR" ]]; then
    echo "❌ Could not find JAR for sponge-mixin-$MIXIN_VERSION" >&2
    exit 1
fi

echo "✅ Found JAR: $MIXIN_JAR" >&2

echo "$MIXIN_JAR"
