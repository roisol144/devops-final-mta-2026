#!/usr/bin/env bash
# Runs the Selenium IDE .side file headlessly via selenium-side-runner.
# Designed to be called from Jenkins or locally.
set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/.." && pwd )"

# Use the matching chromedriver we downloaded to tools/
export PATH="$PROJECT_ROOT/tools:$PATH"

# Pin Chrome to headless via runner config
CONFIG="$SCRIPT_DIR/.side.yml"

cd "$SCRIPT_DIR"
./node_modules/.bin/selenium-side-runner \
  --config-file "$CONFIG" \
  "$SCRIPT_DIR/MetaAppTests.side"
