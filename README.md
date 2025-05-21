# Bajaj Finserv Health Webhook Project

This Spring Boot application handles a webhook flow for Bajaj Finserv Health's hiring challenge.

## Application Flow

1. On startup, the application automatically:
   - Sends a POST request to generate a webhook
   - Solves an SQL problem based on the registration number
   - Submits the SQL solution to the webhook URL

## Technical Stack

- Java 11
- Spring Boot 2.7.0
- Spring Data JPA
- RestTemplate for HTTP requests
- JWT for authentication

## Project Structure

- `HealthQualifierApplication`: Main Spring Boot application class
- `WebhookService`: Service that handles the webhook flow
- `ApplicationConfig`: Configuration class with RestTemplate and CommandLineRunner
- Models:
  - `WebhookRequest`: Request model for generating webhooks
  - `WebhookResponse`: Response model with webhook URL and access token
  - `SolutionRequest`: Request model for submitting solutions

## SQL Solution

The application contains two SQL solutions:

1. For registration numbers ending with odd digits:
   ```sql
   SELECT e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME,
   COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT
   FROM EMPLOYEE e1
   JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID
   LEFT JOIN EMPLOYEE e2 ON e1.DEPARTMENT = e2.DEPARTMENT AND e2.DOB > e1.DOB
   GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME
   ORDER BY e1.EMP_ID DESC
   ```

2. For registration numbers ending with even digits:
   ```sql
   SELECT c.CUSTOMER_NAME, c.CITY,
   COUNT(o.ORDER_ID) AS TOTAL_ORDERS,
   SUM(o.TOTAL_AMOUNT) AS TOTAL_AMOUNT,
   MAX(o.ORDER_DATE) AS LAST_ORDER_DATE
   FROM CUSTOMER c
   JOIN ORDERS o ON c.CUSTOMER_ID = o.CUSTOMER_ID
   GROUP BY c.CUSTOMER_NAME, c.CITY
   HAVING COUNT(o.ORDER_ID) > 5
   ORDER BY SUM(o.TOTAL_AMOUNT) DESC
   ```

## Building and Running

To build and run the application:

```bash
mvn clean install
java -jar target/health-qualifier-1.0.0.jar
```

## Configuration

The application configuration can be found in `application.properties` file:

- Webhook URLs
- JWT Token
- Server configuration
- Logging levels
- HTTP Client configuration 