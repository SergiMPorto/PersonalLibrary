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

            post {
                success {
                    echo "Tests completed successfully for build ${BUILD_TAG}."
                }
                failure {
                    echo "Tests failed for build ${BUILD_TAG}."
                }
            }
        }

        stage('SAST') {
            steps {
                withCredentials([
                    string(
                        credentialsId: 'sonarcloud-token',
                        variable: 'SONAR_TOKEN'
                    )
                ]) {
                    sh 'chmod +x backend-api/jenkins/sast/sast.sh'
                    sh './backend-api/jenkins/sast/sast.sh'
                }
            }

            post {
                success {
                    echo "SAST completed successfully for build ${BUILD_TAG}."
                }
                failure {
                    echo "SAST analysis for build ${BUILD_TAG} failed."
                }
            }
        }

        stage('SCA') {
            steps {
                sh 'chmod +x backend-api/jenkins/sca/sca.sh'
                sh './backend-api/jenkins/sca/sca.sh'
                    
                }
            }

            post {
                success {
                    echo "SCA completed successfully for build ${BUILD_TAG}."
                }
                failure {
                    echo "SCA analysis for build ${BUILD_TAG} failed."
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
                dir('backend-api/jenkins/deploy') {
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