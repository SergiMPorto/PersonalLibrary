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
                withCredentials([
                    usernamePassword(
                        credentialsId: 'docker-hub-credentials',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )
                ]) {
                    dir('backend-api/jenkins/build') {
                        sh 'chmod +x build.sh'
                        sh './build.sh'
                    }
                }
            }

            post {
                success {
                    echo "Build ${BUILD_TAG} completed successfully."
                }
                failure {
                    echo "Build ${BUILD_TAG} failed."
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