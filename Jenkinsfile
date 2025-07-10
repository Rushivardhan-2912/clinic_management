pipeline {
    agent any

    environment {
        JAVA_HOME = tool name: 'JDK', type: 'jdk'
        PATH = "${JAVA_HOME}/bin:${env.PATH}"
    }

    tools {
        maven 'Maven 3.8.4'
    }

    stages {
        stage('Checkout') {
            steps {
                git credentialsId: 'github-credentials-id',
                    url: 'https://github.com/Rushivardhan-2912/clinic_management.git',
                    branch: 'master'
            }
        }

        stage('Build Project') {
            steps {
                bat 'mvn clean install'
            }
        }
    }

    post {
        success {
            echo 'Build completed successfully!'
        }
        failure {
            echo 'Build failed!'
        }
    }
}
