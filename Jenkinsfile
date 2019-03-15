pipeline {
    agent {
        label 'testintegration'
    }

    environment {
        // Default URL for testing on devspace
        MAVEN_SNAPSHOTS_REPO_URL = 'http://nexus:8081/nexus/repository/maven-internal/'
        // TODO: This is currently ignored, instead use the .m2 folder left by the previous jobs
    }

    stages {
        stage('Build') {
            steps {
                // Currently relying on ~/.m2/repository being populated by preceding jobs
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

}
