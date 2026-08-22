pipeline {
    agent any

    environment {
        BUILD_TAG = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Test') {
            steps {
                dir('backend-api/jenkins/test') {
                    sh 'chmod +x test.sh'
                    sh './test.sh'
                }
            }
        }

        stage('Build') {
            steps {
                dir('backend-api/jenkins/build') {
                    sh 'chmod +x build.sh'
                    sh './build.sh'
                }
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying...'
            }
        }
    }
}
