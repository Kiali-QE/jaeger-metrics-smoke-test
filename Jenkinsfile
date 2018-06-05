pipeline {
    /*  This Jenkinfile can be used standalone to validate the metrics tests, but in general
        these tests are intended to be run as part of the jaeger client images tests located
        in the jaeger-*/
    agent any
    options {
        disableConcurrentBuilds()
        timeout(time: 1, unit: 'HOURS')
    }
    tools {
        maven 'maven-3.5.3'
        jdk 'jdk8'
    }
    stages {
    /* TODO delete ES, application? */
        stage('Clean all') {
            steps {
                sh 'oc delete all,template,daemonset,configmap -l jaeger-infra || true'
                sh 'env | sort'
            }
        }
        stage('all-in-one: deploy Jaeger') {
            steps {
                sh 'oc process -f https://raw.githubusercontent.com/jaegertracing/jaeger-openshift/master/all-in-one/jaeger-all-in-one-template.yml | oc create -f -'
                openshiftVerifyService apiURL: '', authToken: '', namespace: '', svcName: 'jaeger-query', verbose: 'false'
                openshiftVerifyService apiURL: '', authToken: '', namespace: '', svcName: 'jaeger-collector', verbose: 'false'
            }
        }
        stage('all-in-one: micrometer metrics smoke test'){
            steps{
                deleteDir()
                /*checkout scm*/
                git 'https://github.com/Kiali-QE/jaeger-metrics-smoke-test.git'
                sh '''
                    pwd
                    ls -alF
                    mvn --activate-profiles openshift -DskipITs clean fabric8:deploy
                '''
                openshiftVerifyService apiURL: '', authToken: '', namespace: '', svcName: 'jaeger-micrometer-smoke-tests', verbose: 'false'
                sh '''
                    export TARGET_URL="http://"`oc get route jaeger-micrometer-smoke-tests --output=go-template={{.spec.host}}`
                    echo ${TARGET_URL}
                    mvn verify
                '''
            }
        }
        stage('all-in-one: delete Jaeeger') {
            steps {
                sh 'oc delete all,template,daemonset,configmap -l jaeger-infra || true'
            }
        }
        stage('Delete example app') {
            steps {
                sh 'mvn  --activate-profiles openshift fabric8:undeploy || true'
            }
        }
    }
}
