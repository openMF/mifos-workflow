# Mifos Workflow Documentation

This directory contains comprehensive documentation for the Mifos Workflow Integration Service, built using AsciiDoc for professional, maintainable documentation.

## Documentation Structure

### Source Files (`asciidoc/`)

- **`index.adoc`** - Main documentation file with overview and navigation
- **`api-reference.adoc`** - Complete API documentation with examples
- **`workflow-processes.adoc`** - Detailed workflow process documentation
- **`deployment-guide.adoc`** - Deployment and operations guide
- **`configuration-reference.adoc`** - Complete configuration properties reference

### Generated Files (`generated/`)

- **`index.html`** - Main HTML documentation (generated from AsciiDoc)
- **`api-reference.html`** - API reference in HTML format
- **`workflow-processes.html`** - Workflow processes in HTML format
- **`deployment-guide.html`** - Deployment guide in HTML format
- **`configuration-reference.html`** - Configuration reference in HTML format

**⚠️ Important**: The `generated/` directory contains auto-generated files and should NOT be committed to version control. These files are recreated during the build process.

## Building Documentation

### Prerequisites

- Java 21
- Maven 3.6+
- AsciiDoctor (automatically managed by Maven plugin)

### Generate HTML Documentation

```bash
# Build the entire project including documentation
mvn clean install

# Generate only the documentation
mvn asciidoctor:process-asciidoc
```

### View Documentation

After generation, open `docs/generated/index.html` in your browser to view the complete documentation.

## Documentation Features

### Professional Formatting

- **Responsive Design**: Documentation works on desktop and mobile devices
- **Syntax Highlighting**: Code examples with proper syntax highlighting
- **Table of Contents**: Automatic navigation with clickable links
- **Search Functionality**: Built-in search capabilities
- **Print-Friendly**: Optimized for printing and PDF generation

### Content Organization

- **Modular Structure**: Documentation split into logical sections
- **Cross-References**: Links between related sections with working navigation
- **Code Examples**: Practical examples for all API endpoints
- **Configuration Guides**: Step-by-step setup instructions
- **Troubleshooting**: Common issues and solutions
- **Visual Aids**: Architecture diagrams and BPMN process flows

### API Documentation

- **Complete Endpoint Coverage**: All REST API endpoints documented
- **Request/Response Examples**: Real-world usage examples
- **Error Handling**: Comprehensive error code documentation
- **Authentication**: Security and authentication details
- **Rate Limiting**: API usage limits and guidelines

### Workflow Documentation

- **BPMN Process Definitions**: Complete workflow specifications
- **Process Variables**: Input/output variable documentation
- **Task Delegates**: Java delegate implementation details
- **Customization Guide**: How to extend and modify workflows
- **Best Practices**: Workflow design recommendations

### Deployment Guide

- **Environment Setup**: Development, staging, and production
- **Docker Deployment**: Container-based deployment options
- **System Administration**: Monitoring, logging, and maintenance
- **Security Configuration**: SSL/TLS and access control
- **Performance Tuning**: Optimization recommendations

## Writing Documentation

### AsciiDoc Syntax

The documentation uses AsciiDoc format with the following key features:

- **Headers**: Use `=`, `==`, `===` for different heading levels
- **Code Blocks**: Use `[source,language]` followed by `----` for syntax highlighting
- **Links**: Use `link:url[text]` for external links
- **Includes**: Use `include::filename.adoc[]` to include other files
- **Tables**: Use `|===` for creating data tables
- **Admonitions**: Use `[NOTE]`, `[WARNING]`, `[TIP]` for callouts

### Adding New Documentation

1. **Create New File**: Add a new `.adoc` file in the `asciidoc/` directory
2. **Update Index**: Add include directive to `index.adoc` if needed
3. **Build**: Run `mvn asciidoctor:process-asciidoc` to generate updated HTML
4. **Review**: Check the generated HTML for formatting and links

### Documentation Standards

- **Consistent Formatting**: Follow established patterns and styles
- **Clear Examples**: Provide practical, working code examples
- **Regular Updates**: Keep documentation current with code changes
- **User-Focused**: Write for the end user, not the developer
- **Comprehensive Coverage**: Document all features and configurations

## Configuration

The AsciiDoc Maven plugin is configured in `pom.xml` with the following settings:

- **Source Directory**: `docs/asciidoc/`
- **Output Directory**: `docs/generated/`
- **Backend**: HTML5
- **Doctype**: Book
- **Features**: 
  - Table of contents (left sidebar)
  - Section numbering
  - Syntax highlighting (highlightjs with GitHub theme)
  - Font icons
  - Section anchors for direct linking

## Continuous Integration

The documentation is automatically generated during the Maven build process in the `prepare-package` phase, ensuring that documentation is always up-to-date with the codebase.

## Contributing to Documentation

### Guidelines

1. **Keep it Current**: Update documentation when code changes
2. **Be Clear**: Use simple, clear language
3. **Provide Examples**: Include practical code examples
4. **Test Instructions**: Verify all setup and usage instructions
5. **Follow Style**: Maintain consistent formatting and structure

### Review Process

- All documentation changes should be reviewed
- Test all code examples and commands
- Verify links and cross-references
- Check generated HTML output
- Ensure mobile responsiveness

## Support

For documentation issues or improvements:

- **Repository**: https://github.com/openMF/mifos-workflow.git
- **GitHub Issues**: Create an issue with the `documentation` label
- **Pull Requests**: Submit improvements via pull requests
- **Community**: Discuss in the Mifos Community Forum

## License

This documentation is licensed under the same terms as the project (Mozilla Public License 2.0). 