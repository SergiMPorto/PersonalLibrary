pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                sh 'chmod +x "Backend API/jenkins/build/build.sh"'
                sh '"Backend API/jenkins/build/build.sh"'
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