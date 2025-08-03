# Route 53 and EKS Architecture

## The Problem Route 53 Solves

Imagine you have a phone number for your friend, and they change their number every hour. You couldn't call them! The internet has the same problem: your microservices in EKS have constantly changing addresses (IP addresses).

The role of Route 53 is to give your applications a stable, easy-to-remember name (like flashapp.com) and make sure that name always points to the correct, changing address of your service.

Think of Route 53 as a "dynamic phone book".
--------------------------------------------------------

The Key Players in Your Architecture
Before we get to the steps, let's understand the simple roles of each component:

 - You, the User: The person typing a web address into a browser.

 - The Domain Name: The easy name you buy (e.g., api.flashapp.com).

 - Route 53: AWS's DNS service. It's the phone book. It holds a record that says, "When someone asks for api.flashapp.com, give them this address: 52.200.1.1."

 - The Load Balancer: A single entry point for all your traffic. Think of it as a "doorman". It has one stable address that Route 53 points to. Its job is to look at a request and send it to the right microservice.

 - EKS Cluster: The environment where your microservices live.

 - Pods: The individual microservices (e.g., your product service, user service, order service). These are the apartments in the EKS cluster building.
--------------------------------------------------------
### Step-by-Step Flow with ASCII Diagrams

Let's follow one request from a user's browser all the way to a single microservice.

**Step 1: The User's Request**
The user wants to buy something, so they type a web address into their browser.
<pre>
┌──────────────────────────────────────────────────────────┐
│                                                          │
│                                                          │
│                                                          │
│ +-----------+                                            │
│ |   User    |                                            │
│ +-----------+                                            │
│         |                                                │
│         |  "Hey, where is 'api.flashapp.com/products'?"  │
│         v                                                │
│ +------------------+                                     │
│ | Web Browser      |                                     │
│ | (in India, say)  |                                     │
│ +------------------+                                     │
│                                                          │
│                                                          │
│                                                          │
└──────────────────────────────────────────────────────────┘
</pre>

The browser doesn't know the IP address for api.flashapp.com. So, it needs to look it up.

**Step 2: The DNS Lookup (Route 53)**
The browser's first stop is the internet's phone book, which is Route 53.
                                 
<pre>
┌────────────────────────────────────────────────────────────┐
│                                                            │
│ +------------------+                                       │
│ | Web Browser      |                                       │
│ +------------------+                                       │
│     |                                                      │
│     |  1. "What is the IP address for 'api.flashapp.com'?" │
│     v                                                      │
│ +------------------------+                                 │
│ | Amazon Route 53        |                                 │
│ | (The Phone Book)       |                                 │
│ +------------------------+                                 │
│     |                                                      │
│     |  2. "The address is 52.200.1.1"                      │
│     v                                                      │
│ +------------------+                                       │
│ | Web Browser      |                                       │
│ +------------------+                                       │
│                                                            │
│                                                            │
└────────────────────────────────────────────────────────────┘
</pre>

Route 53's role is complete here. It gave the browser the correct IP address for your load balancer.

**Step 3: Hitting the Load Balancer**
Now the browser knows the address, so it sends the request directly to that IP address. This IP belongs to your Application Load Balancer (ALB).

<pre>
┌───────────────────────────────────────┐
│                                       │
│                                       │
│                                       │
│                                       │
│                                       │
│   +------------------+                │
│   | Web Browser      |                │
│   +------------------+                │
│       |                               │
│       |  3. "Go to 52.200.1.1"        │
│       v                               │
│   +------------------------+          │
│   | Application Load       |          │
│   | Balancer (ALB)         |          │
│   | (The Doorman)          |          │
│   +------------------------+          │
│                                       │
│                                       │
│                                       │
│                                       │
│                                       │
│                                       │
└───────────────────────────────────────┘
</pre>

The Load Balancer accepts the request and looks inside it. It sees the request is for the /products path.

**Step 4: The Routing to the Microservice**
The Load Balancer uses its internal rules to send the request to the correct microservice running in EKS.
<pre>
┌──────────────────────────────────────────────────────────────────────┐
│                                                                      │
│                                                                      │
│                                                                      │
│      +------------------------+                                      │
│      | Application Load       |                                      │
│      | Balancer (ALB)         |                                      │
│      +------------------------+                                      │
│          |                                                           │
│          |  4. "Ah, this is a request for '/products'.               │
│          |     Send it to the Product Microservice."                 │
│          v                                                           │
│      +------------------------------------+                          │
│      | EKS Cluster (The Building)         |                          │
│      |   +--------------------------+     |                          │
│      |   |  Node 1 (EC2 Instance)   |     |                          │
│      |   |  +--------------------+  |     |                          │
│      |   |  | Pod (User Service) |  |     |                          │
│      |   |  +--------------------+  |     |                          │
│      |   |  +--------------------+  |     |                          │
│      |   |  | Pod (Order Service)|  |     |                          │
│      |   |  +--------------------+  |     |                          │
│      |   +--------------------------+     |                          │
│      |                                    |                          │
│      |   +--------------------------+     |                          │
│      |   |  Node 2 (EC2 Instance)   |     |                          │
│      |   |  +--------------------+  |     |                          │
│      |   |  | Pod (Product Service)| <---+ (Request arrives here!)   │
│      |   |  +--------------------+  |     |                          │
│      |   |  +--------------------+  |     |                          │
│      |   |  | Pod (Pod-X)        |  |     |                          │
│      |   |  +--------------------+  |     |                          │
│      |   +--------------------------+     |                          │
│      +------------------------------------+                          │
│                                                                      │
│                                                                      │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
</pre>

The Pod for the product microservice processes the request and sends the response back through the same path to the user.

### Summary of Route 53's Role
Route 53's job is simple but critical:

 - It creates a stable name (api.flashapp.com) that never changes.

 - It links that name to the dynamic address of your Load Balancer, which can change.

 - This link is set up just once. All future changes to your Load Balancer's IP are handled automatically by tools like ExternalDNS, ensuring the api.flashapp.com name is always correct without any manual work from you.

# can you explain your previous answer in detail about Route 52 and EKS: Architecture? #

You're asking about a crucial part of a real-world microservices architecture on AWS. It's great that you want to understand this in detail! Let's simplify the previous explanation with a step-by-step breakdown and a new, detailed diagram that focuses on the role of each component.

1. ** The Goal: A User-Friendly Web Address for Your EKS App **
   The core problem is this: your EKS microservices are not static. Their network addresses (IP addresses) change frequently due to scaling, deployments, or failures. You can't tell a user to visit http://10.0.1.5 because that address might not work in an hour.

The goal is to give your application a stable, human-friendly web address like api.flashapp.com and have the entire system automatically ensure that this name always points to the right place. This is where Route 53 comes in.

2. **The Architecture: A Unified Entry Point**
   In a modern EKS setup, you don't expose each microservice directly to the public internet. Instead, you create a single, public entry point—an Application Load Balancer (ALB). The ALB then acts as a "doorman" for your entire EKS cluster, directing traffic to the correct microservice based on the URL path.

Route 53's job is to link your domain name to this stable ALB.

Here's the detailed, step-by-step flow with an ASCII diagram:
**Step 1: The Kubernetes Ingress Manifest ✍️**

You, the developer, start by creating a Kubernetes file (a "manifest") for an Ingress. This file tells your EKS cluster how to expose a microservice to the outside world. This is where you specify the domain name you want to use.


<pre>
```
# A part of your Kubernetes manifest file
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: flash-product-ingress
  annotations:
    kubernetes.io/ingress.class: alb
    alb.ingress.kubernetes.io/scheme: internet-facing
    alb.ingress.kubernetes.io/target-type: ip
spec:
  rules:
  - host: api.flashapp.com
    http:
      paths:
      - path: /products/*
        pathType: Prefix
        backend:
          service:
            name: flash-product-service
            port:
              number: 80
```
</pre>


***What this does: This YAML file tells Kubernetes: "When a request comes in for api.flashapp.com/products, send it to the flash-product-service running in my cluster."***

<hr>

### Step 2: The AWS Load Balancer Controller 🤖  ###

The AWS Load Balancer Controller is a special program (a "controller") that runs inside your EKS cluster. It's always watching for new Ingress manifests.

When you apply your Ingress manifest, the Load Balancer Controller sees it and says, "Aha, a new request for an internet-facing ALB!" It then goes to the AWS cloud and automatically provisions a brand-new Application Load Balancer for you. 
This ALB is given a public DNS name by AWS (e.g., k8s-default-flash-1234567890.us-east-1.elb.amazonaws.com) but doesn't have a simple, human-friendly name yet.

<pre>
┌───────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                                                                                               │
│                                                                                                               │
│                                                                                                               │
│                                                                                                               │
│                                                                                                               │
│                                                                                                               │
│                                                                                                               │
│       +------------------+     +-------------------------------+     +----------------------------------+     │
│       | EKS Cluster      | --> | AWS Load Balancer Controller  | --> | AWS ALB (Load Balancer)          |     │
│       |                  |     | (inside EKS)                  |     |                                  |     │
│       | (Ingress manifest)|     +-------------------------------+     |  - DNS Name: k8s-xxx.elb.com     |    │
│       +------------------+                                          |  - Static Public IP              |      │
│                                                                      +----------------------------------+     │
│                                                                                                               │
│                                                                                                               │
│                                                                                                               │
│                                                                                                               │
│                                                                                                               │
│                                                                                                               │
└───────────────────────────────────────────────────────────────────────────────────────────────────────────────┘
</pre>

<hr>

### Step 3: The ExternalDNS Controller 🗺️ ###

Here's where Route 53 gets involved, and this is the "magic" step.

The ExternalDNS controller is another program you install in your EKS cluster. Its job is to watch for services or ingresses that need a public DNS record.

ExternalDNS sees your Ingress manifest and its api.flashapp.com domain name.

It then connects to your Route 53 Hosted Zone (which is where your flashapp.com domain is managed).

It automatically creates a DNS record that points your domain name, api.flashapp.com, to the new ALB's DNS name. This is an Alias Record, which is a special type of Route 53 record designed to point to other AWS resources.

This automation is key: you write one manifest, and the controllers handle the rest.

<hr>

### Step 4: The Final Request Flow 🚀 ###

Now that all the pieces are in place, here's what happens when a user types your address:

<pre>
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│                                                                 │
│                                                                 │
│                                                                 │
│                                                                 │
│                                                                 │
│    +-----------+                                                │
│    | User      |                                                │
│    +-----------+                                                │
│          | (1) "Where is api.flashapp.com?"                     │
│          v                                                      │
│    +------------------------+                                   │
│    | Amazon Route 53        |                                   │
│    | (The DNS Phone Book)   |                                   │
│    |                        |                                   │
│    |  - Record: api.flashapp.com -> ALB DNS Name |              │
│    +------------------------+                                   │
│          | (2) "Go to k8s-xxx.elb.com"                          │
│          v                                                      │
│    +------------------------+                                   │
│    | Application Load       |                                   │
│    | Balancer (ALB)         |                                   │
│    | (The Doorman)          |                                   │
│    +------------------------+                                   │
│          | (3) "This is for /products.                          │
│          |      Send it to the Product Service pods."           │
│          v                                                      │
│    +------------------------------------+                       │
│    | EKS Cluster (The Building)         |                       │
│    |                                    |                       │
│    |   +--------------------------+     |                       │
│    |   |  Node 1 (EC2 Instance)   |     |                       │
│    |   |  +--------------------+  |     |                       │
│    |   |  | Pod (Product Service)| <---+ (Request arrives here) │
│    |   |  +--------------------+  |     |                       │
│    |   +--------------------------+     |                       │
│    |                                    |                       │
│    |   +--------------------------+     |                       │
│    |   |  Node 2 (EC2 Instance)   |     |                       │
│    |   |  +--------------------+  |     |                       │
│    |   |  | Pod (Order Service)  | |     |                      │
│    |   |  +--------------------+  |     |                       │
│    |   +--------------------------+     |                       │
│    +------------------------------------+                       │
│                                                                 │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
</pre>

**Summary**
The role of Route 53 is to provide a static, friendly name (api.flashapp.com) for your application. It uses an Alias Record to point this name to the dynamic DNS name of your Application Load Balancer.
The automation provided by the AWS Load Balancer Controller and ExternalDNS ensures this link is always correct, even if your underlying infrastructure changes.

## 📌 🤔 so according to Step 2: The AWS Load Balancer Controller 🤖 , for every single microservice , load balance controller assigns a seperate load balancer?  ##

That's an excellent follow-up question! The short answer is: no, not necessarily. The AWS Load Balancer Controller is designed to be smart and efficient. While it can create a separate ALB for every Ingress resource, the recommended and most cost-effective approach is to use a single ALB for multiple microservices.

Here’s a detailed breakdown of how it works:

One ALB for Multiple Microservices 🤝
The AWS Load Balancer Controller supports a feature called IngressGroup, which allows you to group multiple Kubernetes Ingress resources together. When you do this, the controller knows to merge the routing rules from all those Ingresses into a single, unified ALB.


This is the standard and most common practice for several reasons:

 - Cost Savings: You only pay for one ALB instead of many. ALBs can become expensive if you have dozens of microservices.

 - Simplified Management: You only have one public endpoint to manage and secure. This single ALB can handle all your api.flashapp.com traffic.

 - Centralized Routing: All your routing logic (e.g., /products, /users, /orders) is configured on one load balancer, making it easier to manage and monitor.

** How to Configure a Single ALB ⚙️ **
To make this happen, you simply add an annotation to your Ingress manifests, telling the controller that they belong to the same group.

Let's use your flash-product-service as an example, and imagine you have an flash-user-service as well.

 - flash-product-service Ingress:
<pre>
```
metadata:
  name: flash-product-ingress
  annotations:
    kubernetes.io/ingress.class: alb
    alb.ingress.kubernetes.io/group.name: flash-app-group # 👈 The key annotation
    alb.ingress.kubernetes.io/scheme: internet-facing
spec:
  rules:
  - host: api.flashapp.com
    http:
      paths:
      - path: /products/*
        pathType: Prefix
        backend:
          service:
            name: flash-product-service
            port:
              number: 80
```
</pre>

 - flash-user-service Ingress:
<pre>
```
metadata:
  name: flash-user-ingress
  annotations:
    kubernetes.io/ingress.class: alb
    alb.ingress.kubernetes.io/group.name: flash-app-group # 👈 Same group name!
    alb.ingress.kubernetes.io/scheme: internet-facing
spec:
  rules:
  - host: api.flashapp.com
    http:
      paths:
      - path: /users/*
        pathType: Prefix
        backend:
          service:
            name: flash-user-service
            port:
              number: 80
```
</pre>

By using the same alb.ingress.kubernetes.io/group.name annotation, you're instructing the AWS Load Balancer Controller to combine the routing rules for both /products and /users onto the same Application Load Balancer.

<hr>

A Visual Diagram of the Single ALB Approach
This diagram shows how one ALB becomes the central router for multiple microservices.

<pre>
+-------------------------------------------------------------------------------------------------------------------+
| User's Browser                                                                                                    |
|  - Request 1: api.flashapp.com/products                                                                           |
|  - Request 2: api.flashapp.com/users                                                                              |
+-------------------------------------------------------------------------------------------------------------------+
      |
      |  (Traffic goes to the single, stable ALB IP)
      v
+-------------------------------------------------------------------------------------------------------------------+
| Application Load Balancer (ALB)                                                                                   |
| (The Doorman with a Single Address)                                                                               |
|                                                                                                                   |
|   +-----------------------------------------------------------------+                                             |
|   | Routing Rules                                                   |                                             |
|   | --------------------------------------------------------------- |                                             |
|   | - IF Path is /products/* --> Send to 'product-service' target group |                                      |
|   | - IF Path is /users/* --> Send to 'user-service' target group    |                                      |
|   +-----------------------------------------------------------------+                                             |
+-------------------------------------------------------------------------------------------------------------------+
      |          |
      |          | (ALB routes traffic based on the URL path)
      v          v
+---------------------------------+  +---------------------------------+
| EKS Pods (Product Service)      |  | EKS Pods (User Service)         |
|                                 |  |                                 |
| - Handles requests for /products|  | - Handles requests for /users   |
+---------------------------------+  +---------------------------------+
</pre>

This architecture is the most common and efficient way to handle a microservices API gateway in EKS. You have the flexibility to create multiple Ingresses for each service while still benefiting from a single, centralized load balancer.