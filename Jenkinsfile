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
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_TOKEN'
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
                dir('banckend-api/jenkins/deploy') {
                    sh 'chmod +x deploy.sh'
                    sh './deploy.sh'
                }
            }
            post {
                success {
                    echo "Deployment of build ${BUILD_TAG} completed successfully."
                }
                failure {
                    echo "Deployment of build ${BUILD_TAG} failed."
                }
            }
        }
    }
}