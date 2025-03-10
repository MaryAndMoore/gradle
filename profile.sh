#!/usr/bin/env bash


/Users/asodja/workspace/gradle-profiler/build/install/gradle-profiler/bin/gradle-profiler --profile jprofiler \
    --jprofiler-alloc \
    --jprofiler-heapdump \
    --jprofiler-home="/Applications/JProfiler.app/" \
    --studio-install-dir="/Applications/Android Studio Meerkat.app" \
    --studio-sandbox-dir="/Users/asodja/workspace/gradle-performance/profile-out/studio-sandbox" \
    --gradle-version="8.13" \
    --gradle-version="9.0-branch-provider_api_migration_public_api_changes-20250304171043+0000" \
    --project-dir="/Users/asodja/workspace/gradle-performance" \
    --scenario-file "/Users/asodja/workspace/gradle-performance/scenario.config" "runSanityCheck"

