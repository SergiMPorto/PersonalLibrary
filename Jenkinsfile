pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                dir('backend-api-app/jenkins/build') {
                    sh 'chmod +x build.sh'
                    sh './build.sh'
                }
             
            }
        }

        stage('Test') {
            steps {
                echo 'Testing...'
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying...'
            }
        }
    }
}