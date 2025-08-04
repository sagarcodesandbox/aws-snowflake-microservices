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