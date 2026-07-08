// DRILL 09 (part 2) — CI/CD WITH JENKINS
//
// Infosys Framework Q13/Q14: "What is Jenkins? How do you integrate Jenkins with your framework?"
// The spoken answer, then point at this file:
//   "Jenkins is a CI server. I keep a Jenkinsfile in the repo root — pipeline-as-code —
//    so the pipeline is versioned with the tests. Jenkins checks out the repo, builds with
//    Maven, runs the suite headless, and publishes the JUnit-format XML that surefire
//    produces. Triggers are a webhook on push, or a nightly cron for the full regression."
//
// This is a DECLARATIVE pipeline (pipeline{} block) — the modern style. The older
// scripted style starts with node{} and is plain Groovy. Know that distinction.

pipeline {
    // 'any' = run on any available agent (build node). Real setups use labels,
    // e.g. agent { label 'linux-chrome' }, so browser jobs land on machines with browsers.
    agent any

    tools {
        // Names must match Manage Jenkins -> Tools. Common convention shown here.
        jdk 'JDK17'
        maven 'Maven3'
    }

    options {
        timestamps()                          // prefix every log line — first thing you want when debugging CI
        timeout(time: 30, unit: 'MINUTES')    // never let a hung browser wedge the executor forever
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    triggers {
        // Poll fallback; in practice a GitHub webhook fires the build on push instead.
        pollSCM('H/15 * * * *')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm   // clones the repo/branch this Jenkinsfile came from
            }
        }

        stage('Compile') {
            steps {
                // Fail fast on compile errors before spending time on browser startup
                sh 'mvn -B clean test-compile'
            }
        }

        stage('Test - JUnit + Cucumber') {
            steps {
                sh 'mvn -B test -Dheadless=true'
            }
        }

        stage('Test - TestNG parallel suite') {
            steps {
                sh 'mvn -B test -Pparallel -Dheadless=true'
            }
        }
    }

    post {
        // 'always' runs whether tests passed or failed — reports matter MOST on failure.
        always {
            junit 'target/surefire-reports/*.xml'   // publishes trend graphs + per-test results
            archiveArtifacts artifacts: 'target/screenshots/**', allowEmptyArchive: true
        }
        failure {
            echo 'Tests failed — screenshots for failed TestNG tests are in the archived artifacts.'
            // Real teams notify here: slackSend / emailext
        }
    }
}
