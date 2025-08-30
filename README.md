# Mifos Workflow Integration Service

A Spring Boot application that provides workflow management for Mifos X operations using the Flowable BPMN 2.0 workflow engine. This service integrates with Fineract (Mifos X Backend) to manage client lifecycle and loan operations through standardized business processes.

## Features

### Core Workflows
- **Client Lifecycle Management**
  - Client Onboarding Process
  - Client Offboarding Process  
  - Client Transfer Process
- **Loan Operations**
  - Loan Origination Process
  - Loan Disbursement Process
  - Loan Cancellation Process

### Technical Capabilities
- BPMN 2.0 workflow engine powered by Flowable
- RESTful API for workflow management
- Integration with Mifos Fineract API
- Basic authentication with Fineract credentials
- Process monitoring and status tracking
- Comprehensive error handling and validation

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- MySQL 8.4+ database
- Mifos Fineract instance (for integration)
- Docker and Docker Compose (optional, for containerized deployment)

## Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/mifos/mifos-workflow.git
cd mifos-workflow
```

### 2. Configure Environment Variables

Set the following environment variables:

```bash
export DB_PASSWORD=your_secure_db_password
export FINERACT_PASSWORD=your_secure_fineract_password
export WORKFLOW_FINERACT_BASEURL=https://localhost:8443/fineract-provider/api/v1/
export WORKFLOW_FINERACT_TENANTID=default
export WORKFLOW_FINERACT_VALIDATESSL=false
```

### 3. Build and Run

```bash
# Build the application
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8081`

### 4. Docker Deployment (Alternative)

For containerized deployment:

```bash
# Start the application with Docker Compose
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop the application
docker-compose down
```

### 5. Verify Installation

```bash
# Check application health
curl http://localhost:8081/actuator/health
```

## Configuration

### Application Properties

The main configuration is in `src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=8081

# Database Configuration
spring.datasource.url=jdbc:mysql://127.0.0.1:3307/mifos_flowable?characterEncoding=UTF-8
spring.datasource.username=mifos
spring.datasource.password=${DB_PASSWORD:password}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Flowable Configuration
flowable.database-schema-update=true
flowable.async-executor-activate=true
workflow.engine.flowable.async-executor-enabled=true
workflow.engine.flowable.database-schema-update=true
workflow.engine.flowable.history-enabled=true

# Fineract Integration
workflow.fineract.baseUrl=${WORKFLOW_FINERACT_BASEURL:https://localhost:8443/fineract-provider/api/v1/}
workflow.fineract.username=mifos
workflow.fineract.password=${FINERACT_PASSWORD:password}
workflow.fineract.tenantId=${WORKFLOW_FINERACT_TENANTID:default}
workflow.fineract.test-enabled=true
workflow.fineract.connection-timeout=30000
workflow.fineract.read-timeout=30000

# Authentication Settings
workflow.authentication.enabled=true
workflow.authentication.auth-key-header=Authorization
workflow.authentication.auth-key-prefix=Basic 
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_PASSWORD` | Database password | `password` |
| `FINERACT_PASSWORD` | Fineract API password | `password` |
| `WORKFLOW_FINERACT_BASEURL` | Fineract API base URL | `https://localhost:8443/fineract-provider/api/v1/` |
| `WORKFLOW_FINERACT_TENANTID` | Fineract tenant ID | `default` |
| `WORKFLOW_FINERACT_VALIDATESSL` | Validate SSL certificates | `false` |

## API Documentation

### Base URL
```
http://localhost:8081/api/v1
```

### Authentication
All endpoints require Basic Authentication using Fineract credentials:

```bash
Authorization: Basic <base64-encoded-credentials>
```

### API Endpoints

#### Authentication

**Authenticate with Fineract**
```bash
POST /auth/authenticate
```

**Get Authentication Status**
```bash
GET /auth/status
```

**Logout**
```bash
DELETE /auth/logout
```

#### Client Workflows

**Start Client Onboarding**
```bash
POST /workflow/client-onboarding/start
```

**Get Client Onboarding Tasks**
```bash
GET /workflow/client-onboarding/tasks
```

**Complete Client Onboarding Task**
```bash
POST /workflow/client-onboarding/tasks/{taskId}/complete
```

**Start Client Offboarding**
```bash
POST /workflow/client-offboarding/start
```

**Start Client Transfer**
```bash
POST /workflow/client-transfer/start
```

#### Loan Workflows

**Start Loan Origination**
```bash
POST /workflow/loan-origination/start
```

**Approve Loan**
```bash
POST /workflow/loan-origination/approve
```

**Reject Loan**
```bash
POST /workflow/loan-origination/reject
```

**Start Loan Disbursement**
```bash
POST /workflow/loan-disbursement/start
```

**Start Loan Cancellation**
```bash
POST /workflows/loan-cancellation/start
```

### Postman Collection

For a complete API reference with all endpoints, request examples, and testing capabilities, use our Postman collection:

**[📋 Mifos Workflow API Collection](https://documenter.getpostman.com/view/30045330/2sB3HhrMfe)**

The collection includes:
- All available endpoints organized by workflow type
- Pre-configured request examples with sample data
- Environment variables for easy testing
- Authentication setup
- Response examples and documentation

### Example API Usage

#### Authenticate with Fineract
```bash
curl -X POST "http://localhost:8081/api/v1/auth/authenticate" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "mifos",
    "password": "password",
    "tenantId": "default"
  }'
```

#### Start Client Onboarding
```bash
curl -X POST "http://localhost:8081/api/v1/workflow/client-onboarding/start" \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic bWlmb3M6cGFzc3dvcmQ=" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "officeId": 1,
    "dateFormat": "dd MMMM yyyy",
    "locale": "en",
    "active": true,
    "legalFormId": 1,
    "externalId": "EXT001",
    "mobileNo": "+1234567890",
    "dateOfBirth": "1990-01-01"
  }'
```

#### Start Loan Origination
```bash
curl -X POST "http://localhost:8081/api/v1/workflow/loan-origination/start" \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic bWlmb3M6cGFzc3dvcmQ=" \
  -d '{
    "clientId": 123,
    "productId": 1,
    "principal": 10000.00,
    "loanTermFrequency": 12,
    "loanTermFrequencyType": 2,
    "loanType": "individual",
    "loanPurposeId": 1,
    "interestRatePerPeriod": 15.0,
    "interestRateFrequencyType": 2,
    "amortizationType": 1,
    "interestType": 1,
    "interestCalculationPeriodType": 1,
    "transactionProcessingStrategyId": "mifos-standard-strategy",
    "numberOfRepayments": 12,
    "repaymentEvery": 1,
    "repaymentFrequencyType": 2,
    "expectedDisbursementDate": "2024-01-15",
    "submittedOnDate": "2024-01-01",
    "dateFormat": "yyyy-MM-dd",
    "locale": "en"
  }'
```

#### Start Loan Cancellation
```bash
curl -X POST "http://localhost:8081/api/v1/workflows/loan-cancellation/start" \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic bWlmb3M6cGFzc3dvcmQ=" \
  -d '{
    "loanId": 123,
    "cancellationReasonId": 1,
    "cancelledOnDate": "2024-01-15",
    "note": "Client requested cancellation",
    "dateFormat": "yyyy-MM-dd",
    "locale": "en"
  }'
```

## Architecture

### System Components

- **REST Controllers**: Handle HTTP requests for workflow operations
- **Service Layer**: Business logic and workflow orchestration
- **Flowable Engine**: BPMN 2.0 workflow execution engine
- **Fineract Client**: Integration with Mifos Fineract API
- **Database**: MySQL for process data and audit trail

### Workflow Process Structure

Each workflow follows a standardized BPMN 2.0 structure:

1. **Start Event**: Process initiation
2. **User Tasks**: Manual intervention points
3. **Service Tasks**: Automated business logic execution
4. **Gateways**: Decision points and flow control
5. **End Events**: Process completion

## Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=LoanOriginationControllerTest

# Run with test profile
mvn test -Dspring.profiles.active=test
```

### Test Structure

- **Unit Tests**: `src/test/java/org/mifos/workflow/`
- **Test Configuration**: `src/test/resources/application-test.properties`

## Deployment

### Local Development

```bash
# Build the application
mvn clean install

# Run the application
mvn spring-boot:run
```

### Production Deployment

#### Option 1: Traditional Deployment

1. **Build the application**
   ```bash
   mvn clean package -DskipTests
   ```

2. **Set production environment variables**
   ```bash
   export DB_PASSWORD=secure_production_password
   export FINERACT_PASSWORD=secure_fineract_password
   export WORKFLOW_FINERACT_BASEURL=https://your-fineract-server/fineract-provider/api/v1/
   export WORKFLOW_FINERACT_TENANTID=your_tenant_id
   export WORKFLOW_FINERACT_VALIDATESSL=true
   ```

3. **Run the application**
   ```bash
   java -jar target/mifos-workflow-0.0.1-SNAPSHOT.jar
   ```

#### Option 2: Docker Deployment

1. **Update docker-compose.yml** with production values
2. **Set environment variables**
   ```bash
   export DB_PASSWORD=secure_production_password
   export FINERACT_PASSWORD=secure_fineract_password
   export WORKFLOW_FINERACT_BASEURL=https://your-fineract-server/fineract-provider/api/v1/
   export WORKFLOW_FINERACT_TENANTID=your_tenant_id
   export WORKFLOW_FINERACT_VALIDATESSL=true
   ```

3. **Deploy with Docker Compose**
   ```bash
   docker-compose up -d
   ```

## Monitoring

### Health Checks

```bash
# Application health
curl http://localhost:8081/actuator/health

# Workflow engine health
curl http://localhost:8081/actuator/health/workflow
```

### Process Monitoring

```bash
# Get active processes
curl -H "Authorization: Basic bWlmb3M6cGFzc3dvcmQ=" \
  http://localhost:8081/api/v1/workflow/client-onboarding/processes

# Get process status
curl -H "Authorization: Basic bWlmb3M6cGFzc3dvcmQ=" \
  http://localhost:8081/api/v1/workflow/client-onboarding/processes/{processInstanceId}/status

# Get process variables
curl -H "Authorization: Basic bWlmb3M6cGFzc3dvcmQ=" \
  http://localhost:8081/api/v1/workflow/client-onboarding/processes/{processInstanceId}/variables

# Get process history
curl -H "Authorization: Basic bWlmb3M6cGFzc3dvcmQ=" \
  http://localhost:8081/api/v1/workflow/client-onboarding/history

# Get deployment information
curl -H "Authorization: Basic bWlmb3M6cGFzc3dvcmQ=" \
  http://localhost:8081/api/v1/workflow/client-onboarding/deployments
```

## Error Handling

### Common Error Codes

| HTTP Status | Error Code | Description |
|-------------|------------|-------------|
| 400 | Bad Request | Invalid request parameters |
| 401 | Unauthorized | Authentication required |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Business rule violation |
| 422 | Unprocessable Entity | Validation error |
| 500 | Internal Server Error | Server error |

### Error Response Format

```json
{
  "timestamp": "2024-01-01T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Required field 'clientId' is missing",
  "path": "/api/v1/workflow/loan-origination/start"
}
```

## Documentation

### Generated Documentation

```bash
# Generate HTML documentation
mvn asciidoctor:process-asciidoc

# View documentation
open docs/generated/index.html
```

### AsciiDoc Documentation Structure

- [API Reference](docs/asciidoc/api-reference.adoc) - Complete API documentation
- [Workflow Processes](docs/asciidoc/workflow-processes.adoc) - Process documentation
- [Deployment Guide](docs/asciidoc/deployment-guide.adoc) - Deployment instructions
- [Configuration Reference](docs/asciidoc/configuration-reference.adoc) - Configuration options


## Troubleshooting

### Common Issues

1. **Database Connection Issues**
   - Verify MySQL is running on port 3307 (not 3306)
   - Check database name is `mifos_flowable` (not `mifos_workflow`)
   - Verify database credentials in environment variables
   - Ensure database schema is created

2. **Fineract Integration Issues**
   - Verify Fineract instance is running
   - Check Fineract credentials and tenant configuration
   - Ensure Fineract API endpoints are accessible
   - Verify SSL certificate if using HTTPS

3. **Workflow Engine Issues**
   - Check Flowable database tables are created
   - Verify BPMN process definitions are deployed
   - Review application logs for workflow errors

4. **Docker Issues**
   - Ensure Docker and Docker Compose are installed
   - Check if ports 8081 and 3307 are available
   - Verify Docker has access to host network for Fineract integration

### Debug Mode

```bash
# Enable debug logging
export LOGGING_LEVEL_ORG_MIFOS_WORKFLOW=DEBUG
export LOGGING_LEVEL_ORG_MIFOS=DEBUG

# Start with debug logging
mvn spring-boot:run

# Or with Docker
LOG_LEVEL=DEBUG docker-compose up
```

## Contributing

### Development Setup

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Run the test suite
6. Submit a pull request

### Code Style

- Follow Java coding conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public methods
- Ensure test coverage for new functionality

## License

This project is licensed under the Mozilla Public License 2.0 - see the [LICENSE](LICENSE) file for details.

## Support

### Getting Help

- **Documentation**: Check the documentation first
- **Issues**: Report bugs via [GitHub Issues](https://github.com/openMF/mifos-workflow/issues)
- **Community**: Connect with the Mifos community

### Reporting Issues

When reporting issues, please include:

- Environment details (OS, Java version, Maven version)
- Steps to reproduce the issue
- Expected vs actual behavior
- Relevant application logs and stack traces