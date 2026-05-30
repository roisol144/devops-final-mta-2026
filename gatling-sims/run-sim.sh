#!/usr/bin/env bash
# Runs a single Gatling simulation against localhost:8080/roi-shiraz-omri-noa-arbel-app/.
# Usage: ./run-sim.sh <SimulationClassName>
# Example: ./run-sim.sh MaxLimitSimulation
set -e

if [ -z "$1" ]; then
  echo "Usage: $0 <SimulationClassName>"
  echo "Examples: MaxLimitSimulation | LoadSimulation | StressSimulation"
  exit 1
fi

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Force Java 21 — system default JAVA_HOME may point to Java 8 (too old for Gatling)
export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

echo "Using JAVA_HOME=$JAVA_HOME"
java -version 2>&1 | head -1

mvn gatling:test "-Dgatling.simulationClass=metaapp.$1"
