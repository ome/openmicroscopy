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
                // Currently relying on ~/.m2/repository being populated by preceding jobs
                //sh 'mvn -f download-repo-jars/pom1.xml dependency:copy-dependencies'
                //sh 'mvn -f download-repo-jars/pom2.xml dependency:copy-dependencies'
                sh '''
                    export OMERO_BRANCH=$GIT_BRANCH
                    virtualenv venv --system-site-packages
                    . venv/bin/activate
                    pip install --upgrade 'pip<10' setuptools
                    bash docs/hudson/OMERO.sh
                '''
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
