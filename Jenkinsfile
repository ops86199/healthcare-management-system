pipeline {
    agent any
    
    environment {
        // Define any environment variables here
        AWS_ECR_REPOSITORY = 'your-ecr-repository'
        AWS_REGION = 'your-aws-region'
        FRONTEND_IMAGE = 'HealthCMS-frontend-image'
        BACKEND_IMAGE = 'HealthCMS-backend-image'
        DB_IMAGE = 'HealthCMS-db-image'
        IMAGE_TAG = 'latest'
        AWS_CREDENTIALS = credentials('aws-credentials-id') // Replace with your Jenkins credentials ID
    }
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code from GitHub...'
                // Add your SCM checkout commands here
                checkout scmGit(branch: [[name: 'main']], userRemoteConfig: [url: 'https://github.com/ops86199/healthcare-management-system.git'])
            }
        }
    
        stage('Build & Test Backend') {
            steps {
                dir('backend') {
                    echo 'Building and testing backend...'
                    // Add your backend build and test commands here
                    sh 'mvn clean package'
                    sh 'mvn test'
                }
                
            }
        }
        stage('Build & Test Frontend') {
            steps {
                dir('frontend') {
                    echo 'Building and testing frontend...'
                   // Add your frontend build and test commands here
                   sh 'npm install'
                   sh 'npm run build'
                   sh 'npm test'
                    }
                }
            }
        stage('Build Docker Images') {
            steps {
                echo 'Building dockerimages for all 3 tayers...'
                //Build backend image
                dir('backend') {
                    sh "docker build -t $AWS_ECR_REPOSITORY/$BACKEND_IMAGE:$IMAGE_TAG ."
                    sh 'docker rm -f backend_container || true'
                    sh "docker run --name backend_container -d -p 8081:8080 $AWS_ECR_REPOSITORY/$BACKEND_IMAGE:$IMAGE_TAG" 
                }
                //Build frontend image
                dir('frontend') {
                    sh "docker build -t $AWS_ECR_REPOSITORY/$FRONTEND_IMAGE:$IMAGE_TAG ."
                    sh 'docker rm -f frontend_container || true'
                    sh "docker run --name frontend_container -d -p 3000:3000 $AWS_ECR_REPOSITORY/$FRONTEND_IMAGE:$IMAGE_TAG"
                }
            }
        }
                //Build database image
        stage('Run Database Container') {
            steps {
                echo 'Pulling PostgreSQL image from Docker Hub...'
                   sh 'docker rm -f db_container || true'
                   sh 'docker pull postgres:15'
                   sh """
                      docker run --name db_container -d -p 5432:5432 -e POSTGRES_DB=healthcare_db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres \
                      postgres:15
                      """
            }
        }   
        
        stage('Security Vulnerability Scan') {
            steps {
                echo 'Running Trivy/SonarQube for scan to check for vulnerabilities...'
                // Explain Trivy scan command
                sh "trivy image --severity HIGH,CRITICAL $AWS_ECR_REPOSITORY/$BACKEND_IMAGE:$IMAGE_TAG"
                sh "trivy image --severity HIGH,CRITICAL $AWS_ECR_REPOSITORY/$FRONTEND_IMAGE:$IMAGE_TAG"

            }
        }
        stage('Push Images to ECR') {
            steps {
                echo 'Pushing images to AWS ECR...'
                // Add your AWS ECR login and push commands here
                sh 'aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $AWS_ECR_REPOSITORY'
                sh "docker push $AWS_ECR_REPOSITORY/$BACKEND_IMAGE:$IMAGE_TAG"
                sh "docker push $AWS_ECR_REPOSITORY/$FRONTEND_IMAGE:$IMAGE_TAG"
                sh "docker push $AWS_ECR_REPOSITORY/$DB_IMAGE:$IMAGE_TAG"
            }
        }
        stage('deploy to k8s cluster') {
             steps {
                withKubeConfig([credentialsId: 'kubeconfig-credentials-id']) {
                   sh "kubectl apply -f frontend/deployment.yaml"
                   sh "kubectl apply -f frontend/service.yaml"
                    sh "kubectl apply -f backend/deployment.yaml"
                    sh "kubectl apply -f backend/service.yaml"
                    sh "kubectl apply -f db/deployment.yaml"
                    sh "kubectl apply -f db/service.yaml"

                }
            }
        }
    }
}