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
                                 



