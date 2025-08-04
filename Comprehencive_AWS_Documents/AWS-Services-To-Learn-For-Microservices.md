### AWS Services for a Microservices Project

Here is a comprehensive list of AWS services to learn for building a microservices project, organized by category.

---

### 1. Foundational Services

These services are the bedrock of any microservices project on AWS.

* **Compute & Orchestration**
    * **Amazon EKS (Elastic Kubernetes Service)**: A managed service for running **Kubernetes** on AWS.
    * **Amazon ECS (Elastic Container Service)**: AWS's own container orchestration service, often simpler to use than EKS.
    * **AWS Fargate**: A **serverless compute engine** that runs containers without managing the underlying servers.
    * **AWS Lambda**: A **serverless compute service** for running code in response to events, perfect for event-driven microservices.
* **Networking & API Management**
    * **Amazon API Gateway**: Acts as a **single entry point** for all your microservices, handling routing and security.
    * **Elastic Load Balancing (ELB)**: Distributes traffic to your microservices. The **Application Load Balancer (ALB)** is key for path-based routing.
    * **Route 53**: A highly available **DNS service** for mapping domain names to your microservices.
    * **Amazon VPC (Virtual Private Cloud)**: Provides a **private network** for your AWS resources.
* **Storage & Databases**
    * **Amazon RDS (Relational Database Service)**: For traditional, structured databases like PostgreSQL and MySQL.
    * **Amazon DynamoDB**: A fast, fully managed **NoSQL** database for high-performance key-value and document storage.
    * **Amazon S3 (Simple Storage Service)**: Object storage for static assets, logs, and other unstructured data.

---

### 2. Advanced and Supporting Services

These services help with messaging, monitoring, security, and more complex workflows.

* **Messaging & Asynchronous Communication**
    * **Amazon SQS (Simple Queue Service)**: A **message queuing service** for decoupling microservices.
    * **Amazon SNS (Simple Notification Service)**: A **publish/subscribe messaging service** for sending messages to multiple subscribers.
    * **Amazon EventBridge**: A **serverless event bus** for building event-driven applications.
* **Monitoring & Logging**
    * **Amazon CloudWatch**: A central hub for collecting **metrics, logs, and events** from all your AWS resources.
    * **AWS X-Ray**: Helps you **analyze and debug** distributed applications by tracing requests across microservices.
    * **AWS CloudTrail**: Records all **API calls** in your AWS account for auditing and security.
* **Security & Configuration Management**
    * **AWS IAM (Identity and Access Management)**: Manages **secure access** to your AWS resources.
    * **AWS Secrets Manager**: For securely **storing and rotating sensitive data** like API keys and database credentials.
    * **AWS KMS (Key Management Service)**: Manages the **cryptographic keys** used to encrypt your data.
    * **AWS Systems Manager Parameter Store**: A centralized store for managing application configurations.
* **Advanced Service Communication & Performance**
    * **AWS Step Functions**: A **serverless workflow service** for orchestrating complex business processes.
    * **AWS App Mesh**: A **service mesh** that standardizes communication and provides greater visibility between microservices.
    * **Amazon CloudFront**: A **Content Delivery Network (CDN)** that caches content at edge locations to reduce latency and improve performance.

##  CI-CD Devops ##

### EKS DevOps CI/CD Pipeline

Here is a breakdown of a typical, automated pipeline for an AWS microservices project using EKS, converted into Markdown format.

---

#### 1. Code & Commit (Source Stage)

* **Developer Action**: A developer writes code for a microservice and pushes it to a Git repository.
* **AWS Services**:
    * **AWS CodeCommit**: A fully managed source control service that hosts private Git repositories. (You can also use GitHub, GitLab, etc.)

CodePipeline is configured to watch this repository for any new commits. When a new commit is detected, the pipeline automatically starts.

---

#### 2. Build & Test (Build Stage)

* **Automated Action**: CodePipeline triggers a build job.
* **AWS Services**:
    * **AWS CodeBuild**: A fully managed build service. It compiles the source code, runs unit tests, and importantly, creates a **Docker container image** of the microservice. The container image is tagged with a unique identifier (e.g., the Git commit hash).

---

#### 3. Store & Tag (Container Registry)

* **Automated Action**: The newly created container image is pushed to a central repository.
* **AWS Services**:
    * **Amazon ECR (Elastic Container Registry)**: A fully managed container registry. It stores the Docker image securely and makes it available for deployment. ECR is where your microservices "live" as container images.

---

#### 4. Deploy (Continuous Delivery)

This is the stage where the new microservice version is deployed to your live EKS cluster.

* **Automated Action**: CodePipeline takes the ECR image tag from the previous stage and uses it to update your Kubernetes deployment manifest.
* **AWS Services**:
    * **AWS CodeDeploy**: A service that automates code deployments. In an EKS context, it often works with a tool like `kubectl` to apply the updated Kubernetes manifest.
    * **EKS**: The cluster receives the updated deployment manifest and schedules the new pods to run on the available worker nodes (either EC2 or Fargate). It gracefully terminates the old pods once the new ones are ready and healthy.

---

#### 5. Run & Orchestrate (Runtime)

* **Live Application**: The new version of your microservice is now live.
* **AWS Services**:
    * **EKS**: Manages the pod lifecycle, ensuring the right number of pods are running.
    * **ELB (as Ingress)**: The Application Load Balancer routes live traffic to the new pods.
    * **Route 53**: Directs all incoming requests to the ALB.

---

#### 6. Monitor & Observe (Feedback Loop)

* **Continuous Monitoring**: The pipeline is not complete until you have a feedback loop.
* **AWS Services**:
    * **Amazon CloudWatch**: Gathers logs, metrics, and events from your EKS cluster and microservices.
    * **AWS X-Ray**: Provides a visual trace of every request as it flows through your microservices, helping you identify bottlenecks and errors.

This feedback loop allows your team to catch errors and performance issues quickly, which can then trigger a new commit, restarting the entire cycle.