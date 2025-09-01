# AppliTracker: Autonomous Gmail sync-based Job Application Tracker 

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-green?logo=spring)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

AppliTracker is a powerful, event-driven, serverless application that completely automates the tracking of your job applications.  
It uses a suite of Google Cloud services to listen for new job-related emails in Gmail, intelligently parses them using the Gemini AI, and updates a Google Sheet in real-time—creating a single source of truth for your job search.

---

## ✨ Features

- **Real-Time Processing:** Uses Gmail’s `watch()` API and Pub/Sub push subscriptions for near-instant processing of new emails.  
- **Intelligent AI Parsing:** Leverages the Gemini API with a fine-tuned prompt to accurately extract Company Name, Job Title, Application Date, and Status (*APPLIED, SCREENING, INTERVIEW*, etc.).  
- **Stateful Tracking:** Intelligently updates existing application entries in your Google Sheet, only advancing the status based on a priority system.  
- **Robust & Stateless:** Built with a stateless architecture on Google Cloud Run, making it scalable and resilient. Handles retries and “poison pill” messages using a Dead-Letter Queue.  
- **Deduplication:** Prevents the same email from being processed multiple times by tracking unique Gmail Message IDs.  
- **Fully Automated:** Includes a scheduled function to automatically renew the Gmail `watch()` subscription every 6 days, making it a true “set and forget” utility.  

---

## 🏗 How It Works: Architecture

The system is built on an event-driven, serverless architecture that is both cost-effective and highly scalable.

1. A **Gmail Filter** applies a specific label to incoming job-related emails.  
2. The **Gmail `watch()` API** detects the label change and sends a notification to a **Google Pub/Sub** topic.  
3. A **Pub/Sub Push Subscription** makes an authenticated HTTP POST request to a secure **Google Cloud Run** service.  
4. The **Spring Boot Application** running on Cloud Run receives the request with a `@RestController`.  
5. The application logic fetches the full email, sends it to the **Gemini API** for parsing, and then writes the structured data to a **Google Sheet**, handling state updates and deduplication.  
6. A **Cloud Scheduler** job triggers a separate endpoint every 6 days to automatically renew the Gmail `watch()` subscription.  

🔗 [**View Mermaid (MMD) Diagram**](https://imgur.com/a/1sx78Gi)

---

## ⚙️ Tech Stack

- **Backend:** Java 17, Spring Boot 3, Apache Maven  
- **Cloud/DevOps:**  
  - Google Cloud Run (Compute)  
  - Google Cloud Pub/Sub (Messaging)  
  - Google Secret Manager (Secrets)  
  - Google Artifact Registry (Docker Registry)  
  - Google Cloud Build (Containerization)  
  - Google Cloud Scheduler (Cron Jobs)  
- **Database:** Google Sheets  
- **AI:** Google Gemini API  
- **CI/CD:** GitHub Actions  

---

## 🚀 CI/CD Docker Pipeline

This project is configured with a professional two-stage CI/CD pipeline using GitHub Actions:

1. **Continuous Integration (CI):** Triggered on every push or pull request to the `main` branch.  
   - Builds the Spring Boot application and runs all unit tests.  
   - Builds a production-ready Docker image.  
   - Pushes the versioned Docker image to **Google Artifact Registry**.  

2. **Continuous Deployment (CD):** Triggered *only after* the CI workflow succeeds on the `main` branch.  
   - Securely authenticates to Google Cloud using Workload Identity Federation.  
   - Pulls the exact Docker image built during the CI step from Artifact Registry.  
   - Deploys the image as a new revision to the Google Cloud Run service.  

---

## 🛠 Prerequisites & Getting Started

To replicate and deploy this project in your own Google Cloud environment, you will need:

- **Tools:** Java 17, Maven, Docker, `gcloud` CLI, and Postman installed locally.  
- **GCP Project:** A Google Cloud project with billing enabled.  
- **Enabled APIs:** You will need to enable all the APIs listed in the Tech Stack section.  
- **Configuration:** You must set up your own OAuth Consent Screen, Client ID, and the necessary Service Accounts and IAM permissions. The core secrets (Refresh Token, Sheet ID, Client Secrets JSON) are managed via **Google Secret Manager**.  

📌 A detailed, step-by-step tutorial for the GCP setup is outside the scope of this README but is a planned future addition.  

---

## 📌 Issues & Enhancements

All issues, feature enhancements, and other details are maintained in Notion.  

🔗 [**View Project Board on Notion**](https://www.notion.so/26165be3d8608069ba3bd18f66516baf?pvs=21)

---

## 🗺 Future Plans (Roadmap)

- [ ] **Infrastructure as Code (IaC):** Provide a complete **Terraform** configuration for replicating the entire GCP infrastructure with a single `terraform apply` command.  
- [ ] **Detailed Setup Guide:** Write a comprehensive `SETUP.md` to walk users through the manual configuration process.  
- [ ] **Web UI for Onboarding:** Develop a simple frontend to handle the OAuth flow, making the tool more accessible.  

---

## 📄 License

This project is licensed under the **MIT License**.  
You may use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the software.  

See the [LICENSE](LICENSE) file for details.  
