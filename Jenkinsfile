pipeline {
    agent any

    environment {
        IMAGE_NAME = 'ecopulse-backend'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test Backend') {
            steps {
                sh './mvnw clean verify'
            }
        }

        stage('Build Backend Docker Image') {
            steps {
                sh "docker build -t ${IMAGE_NAME}:${BUILD_NUMBER} ."
            }
        }

        stage('Build Frontend') {
            steps {
                dir('frontend') {
                    sh 'npm ci'
                    sh 'npm run build'
                }
            }
        }

        stage('Push Image (optional)') {
            when {
                expression { return env.DOCKER_REGISTRY?.trim() }
            }
            steps {
                echo 'Configure DOCKER_REGISTRY and credentials to push images.'
            }
        }
    }

    post {
        always {
            junit 'target/surefire-reports/*.xml'
        }
    }
}
