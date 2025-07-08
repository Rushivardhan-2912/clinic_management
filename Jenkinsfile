pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout([
                    $class: 'GitSCM',
                    gitTool: 'DefaultGit',
                    branches: [[name: '*/main']],
                    userRemoteConfigs: [[
                        url: 'https://innersource.soprasteria.com/rb.gathram/clinic_management.git',
                        credentialsId: 'innersource-gitlab-token'
                    ]]
                ])
            }
        }

        stage('Build') {
            steps {
                sh './mvnw clean package'  // Use 'mvn' if you don't have a Maven wrapper
            }
        }

    }
}
