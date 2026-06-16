// MTA DevOps Final — single CI/CD pipeline (Option B)
//
// Stages: Checkout -> Deploy to Tomcat (Oracle VM) -> Selenium (5 validations)
//         -> Gatling Load (trapezoid, 5 min) -> Gatling Stress (triangular spikes, 5 min)
//
// The availability monitor is NOT a stage here: it runs every 5 minutes
// independently as the separate `Availability-Monitor` timer job.

pipeline {
    agent any

    triggers {
        // Same cadence as the old freestyle Deploy job: poll GitHub every minute.
        // (Activates after the first manual build registers the trigger.)
        pollSCM('* * * * *')
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        APP            = 'roi-shiraz-omri-noa-arbel-app'
        PROJECT        = '/Users/roisolomon/devops-project'
        JAVA_HOME      = '/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home'

        // --- Bonus #5: production now lives on the Oracle Cloud VM (public IP) ---
        VM_HOST        = '151.145.91.183'
        VM_USER        = 'ubuntu'
        SSH_KEY        = '/Users/roisolomon/.ssh/oracle_meta.key'
        REMOTE_WEBAPPS = '/opt/tomcat/webapps'
        PUBLIC_BASE    = 'http://151.145.91.183:8080'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh 'git --no-pager log --oneline -1'
            }
        }

        stage('Deploy to Tomcat (Oracle VM)') {
            steps {
                sh '''
                    set -e
                    # Push the new jsp to the production Tomcat on the Oracle Cloud VM.
                    scp -i "${SSH_KEY}" -o StrictHostKeyChecking=accept-new \
                        app/index.jsp "${VM_USER}@${VM_HOST}:${REMOTE_WEBAPPS}/${APP}/index.jsp"
                    echo "Deployed ${APP} to ${VM_HOST} at $(date)"
                    sleep 5  # let Tomcat recompile the JSP on next request
                    CODE=$(curl -s -o /dev/null -w "%{http_code}" --max-time 15 "${PUBLIC_BASE}/${APP}/")
                    echo "Deploy smoke (public): HTTP ${CODE}"
                    [ "${CODE}" = "200" ] || { echo "Deploy smoke failed (expected 200)"; exit 1; }
                '''
            }
        }

        stage('Selenium (5 validations)') {
            steps {
                sh '''
                    set -e
                    export PATH="${PROJECT}/tools:$PATH"   # exact-match chromedriver
                    "${PROJECT}/selenium/node_modules/.bin/selenium-side-runner" \
                        --config-file "${PROJECT}/selenium/.side.yml" \
                        --base-url "${PUBLIC_BASE}" \
                        "${WORKSPACE}/selenium/MetaAppTests.side"
                '''
            }
        }

        stage('Gatling Load (5 min)') {
            steps {
                sh '''
                    set -e
                    export PATH="${JAVA_HOME}/bin:$PATH"
                    ulimit -n 65535
                    cd "${PROJECT}/gatling-sims"
                    mvn gatling:test -Dgatling.simulationClass=metaapp.LoadSimulation -DbaseUrl="${PUBLIC_BASE}"
                '''
            }
        }

        stage('Gatling Stress (5 min)') {
            steps {
                sh '''
                    set -e
                    export PATH="${JAVA_HOME}/bin:$PATH"
                    ulimit -n 65535
                    cd "${PROJECT}/gatling-sims"
                    mvn gatling:test -Dgatling.simulationClass=metaapp.StressSimulation -DbaseUrl="${PUBLIC_BASE}"
                '''
            }
        }

    }

    post {
        success { echo "Pipeline OK — ${env.APP} deployed and validated." }
        failure { echo 'Pipeline FAILED — check the failing stage log above.' }
    }
}
