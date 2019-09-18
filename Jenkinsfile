#!/usr/bin/env groovy

timestamps{    
    node('jdk') {

        stage("Checkout"){
            deleteDir()
            checkout scm
        }

        stage("Build & Test") {
            bat "gradlew build"
        }

        stage("Report Test Results") {
            junit '**/build/test-results/test/*.xml'
        }
    }
}
