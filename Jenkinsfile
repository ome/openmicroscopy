pipeline {
    agent {
        label 'testintegration'

        // docker {
        //     // Node label
        //     label 'docker'
        //
        //     image 'docker.io/manics/omero-buildenv-docker:latest'
        // }
    }

    environment {
        // Default URL for testing on devspace
        MAVEN_SNAPSHOTS_REPO_URL = 'http://nexus:8081/nexus/repository/maven-internal/'
    }

    stages {
        stage('Build') {
            steps {
                // Currently running on a build node with multiple jobs so incorrect jar may be cached so override local m2 repository
                // (Moving to Docker should fix this)
                sh 'mvn -Dmaven.repo.local="$PWD/m2/repository" -f download-repo-jars/pom1.xml dependency:copy-dependencies'
                sh 'mvn -Dmaven.repo.local="$PWD/m2/repository" -f download-repo-jars/pom2.xml dependency:copy-dependencies'
                sh 'OMERO_BRANCH=$GIT_BRANCH BUILD_PY_ARGS=-Dmaven.repo.local="$PWD/m2/repository" bash docs/hudson/OMERO.sh'
                archiveArtifacts artifacts: './target/*.zip,./target/*.egg,./target/*.log,./target/*INFO'
            }
        }
    }

    // post {
    //     always {
    //         // Cleanup workspace
    //         deleteDir()
    //     }
    // }
}
