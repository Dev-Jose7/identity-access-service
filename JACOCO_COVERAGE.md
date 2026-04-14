# JaCoCo Coverage Reports

This document explains how to use JaCoCo for code coverage in the Identity Access Service.

## Overview

JaCoCo (Java Code Coverage) is integrated into the project to measure test coverage for the domain layer. The configuration focuses on covering business logic while excluding infrastructure and application layers.

## Configuration

### Files Added
- `build.gradle.kts` - Main JaCoCo plugin and basic configuration
- `jacoco.gradle` - Detailed JaCoCo configuration and custom tasks
- `scripts/test-with-coverage.sh` - Helper script for running tests with coverage
- `JACOCO_COVERAGE.md` - This documentation file

### Coverage Requirements
- **Minimum Instruction Coverage**: 80%
- **Minimum Branch Coverage**: 70%
- **Focus**: Domain layer only (`com.arka.identityaccess.domain.**`)

### Exclusions
- Application classes
- Configuration classes
- DTOs
- Controllers
- Repositories
- Service classes
- Test classes

## Usage

### Quick Start

```bash
# Run tests with coverage (recommended)
./scripts/test-with-coverage.sh

# Or run manually
./gradlew test jacocoTestReport jacocoTestCoverageVerification
```

### Individual Commands

```bash
# Run tests only
./gradlew test

# Generate coverage report only
./gradlew jacocoTestReport

# Verify coverage meets minimum requirements
./gradlew jacocoTestCoverageVerification

# Generate custom domain-focused report
./gradlew customJacocoReport

# Clean and run everything
./gradlew clean test jacocoTestReport jacocoTestCoverageVerification
```

### Specific Test Classes

```bash
# Run specific test classes
./gradlew test --tests UserAggregateTest
./gradlew test --tests SessionAggregateTest
./gradlew test --tests RoleAggregateTest

# Run tests for specific package
./gradlew test --tests "com.arka.identityaccess.domain.model.user.**"
```

## Report Locations

### Standard Reports
- **HTML Report**: `build/reports/jacoco/test/html/index.html`
- **XML Report**: `build/reports/jacoco/test/jacocoTestReport.xml`
- **Execution Data**: `build/jacoco/test.exec`

### Custom Reports
- **Domain-Focused HTML**: `build/reports/jacoco/custom/html/index.html`

## Understanding the Reports

### HTML Report
The HTML report provides:
- **Overall coverage summary**
- **Package-level coverage**
- **Class-level coverage**
- **Method-level coverage**
- **Source code highlighting** (green = covered, red = not covered)

### Key Metrics
- **Instruction Coverage**: Percentage of bytecode instructions executed
- **Branch Coverage**: Percentage of conditional branches taken
- **Line Coverage**: Percentage of source lines executed
- **Complexity Coverage**: Cyclomatic complexity coverage

### Coverage Colors
- 🟢 **Green**: Fully covered (100%)
- 🟡 **Yellow**: Partially covered (1-99%)
- 🔴 **Red**: Not covered (0%)

## CI/CD Integration

### GitHub Actions Example
```yaml
- name: Run Tests with Coverage
  run: |
    ./gradlew test jacocoTestReport jacocoTestCoverageVerification

- name: Upload Coverage to Codecov
  uses: codecov/codecov-action@v3
  with:
    file: ./build/reports/jacoco/test/jacocoTestReport.xml
```

### Jenkins Pipeline Example
```groovy
stage('Test with Coverage') {
    steps {
        sh './gradlew test jacocoTestReport jacocoTestCoverageVerification'
        publishHTML([
            allowMissing: false,
            alwaysLinkToLastBuild: true,
            keepAll: true,
            reportDir: 'build/reports/jacoco/test/html',
            reportFiles: 'index.html',
            reportName: 'JaCoCo Coverage Report'
        ])
    }
}
```

## Troubleshooting

### Common Issues

#### 1. Coverage Below Minimum
**Problem**: Coverage verification fails with "Coverage below minimum requirement"

**Solution**:
- Check the HTML report to identify uncovered code
- Add tests for uncovered business logic
- Consider if the uncovered code is dead code and can be removed

#### 2. No Coverage Data
**Problem**: Reports show 0% coverage

**Solution**:
- Ensure tests are actually running (`./gradlew test`)
- Check that test classes are in the correct package structure
- Verify JaCoCo agent is attached to JVM

#### 3. Reports Not Generated
**Problem**: HTML or XML reports not found

**Solution**:
- Run `./gradlew clean` and try again
- Check build logs for JaCoCo errors
- Ensure sufficient disk space

### Debug Mode

```bash
# Run with debug logging
./gradlew test jacocoTestReport --info --debug

# Check JaCoCo execution data
ls -la build/jacoco/
```

## Best Practices

### 1. Focus on Business Logic
- Prioritize covering domain entities, aggregates, and value objects
- Cover business rules and invariants
- Include domain events and exceptions

### 2. Meaningful Tests
- Write tests that verify behavior, not just coverage
- Use domain-driven test scenarios
- Test both happy paths and edge cases

### 3. Regular Monitoring
- Set up coverage gates in CI/CD
- Monitor coverage trends over time
- Review coverage reports in code reviews

### 4. Avoid Gaming the System
- Don't write tests just to increase coverage
- Focus on test quality over quantity
- Use coverage as a guide, not a goal

## Integration with IDE

### IntelliJ IDEA
1. Open `build/reports/jacoco/test/html/index.html`
2. Or use IntelliJ's built-in coverage runner
3. Right-click test class → "Run with Coverage"

### Eclipse
1. Install EclEmma JaCoCo plugin
2. Right-click test class → "Coverage As" → "JUnit Test"

### VS Code
1. Install Coverage Gutters extension
2. Open HTML report in browser
3. Use extension for inline coverage display

## Advanced Configuration

### Custom Rules
```gradle
tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            element = "PACKAGE"
            includes = ["com.arka.identityaccess.domain.model"]
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = 0.90.toBigDecimal() // 90% for domain model
            }
        }
    }
}
```

### Excluding Specific Classes
```gradle
tasks.jacocoTestReport {
    classDirectories.setFrom(
        files(classDirectories.files.map { file ->
            fileTree(file) {
                exclude(
                    "**/SomeClass.class",
                    "**/package/to/exclude/**"
                )
            }
        })
    )
}
```

## References

- [JaCoCo Official Documentation](https://www.jacoco.org/)
- [Gradle JaCoCo Plugin](https://docs.gradle.org/current/userguide/jacoco_plugin.html)
- [Test Coverage Best Practices](https://martinfowler.com/bliki/TestCoverage.html)

---

**Note**: This configuration focuses on domain layer coverage as per the project's domain-driven design approach. Infrastructure and application layers are intentionally excluded to maintain focus on business logic testing.
