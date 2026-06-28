pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                dir('Backend API/jenkins/build') {
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