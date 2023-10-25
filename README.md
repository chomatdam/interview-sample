# Interview Sample

## Context

- Kafka-to-Kafka application
- Unit and Integration tests are prepared and ready to implement.
- Gradle as build tool
- Docker file available
- Basic CI/CD Pipeline

## Scenarios

### Scenario 1

#### Application

1. Develop a DLQ (Dead Letter Queue) with S3 acting as remote storage
2. Depending on time constraints, conduct Unit and/or only Integration testing for the solution

#### Infrastructure

1. Use Terraform to provision an S3 bucket. The code does not need to be perfect but should fulfill the
   primary requirements
2. Create a Helm chart where we can read environments variables from a configmap and/or credentials from a mounted
   secret

### Scenario 2

- Define a flexible detection configuration contract
- Load your configuration and consume events matching your detection patterns
- Enhance the code to handle multiple events and transmit a single event once:
  - merge identical record keys
  - the pattern has been recognized five times in the record value for the same user
  - Header-based filtering (pick one):
    - consumer skips event when state evolved in header `X-Event-State` from value `DETECTION_REQUIRED` to `IGNORED`
    - producer emits when consumed record header doesn't have `X-Company-Test: true`
