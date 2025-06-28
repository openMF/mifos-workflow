# Mifos-Workflow Documentation Setup

This directory contains the AsciiDoc documentation setup for the project.

## Structure

- `asciidoc/` - Source AsciiDoc files
  - `index.adoc` - Main documentation template
- `generated/` - Generated HTML documentation (gitignored)

## Building Documentation

### Prerequisites

- Java 21
- Maven 3.6+

### Generate HTML Documentation

```bash
mvn asciidoctor:process-asciidoc
```

This will generate HTML files in the `docs/generated/` directory.

### View Documentation

After generation, open `docs/generated/index.html` in your browser.

## Writing Documentation

### AsciiDoc Syntax

The documentation uses AsciiDoc format. Key features:

- **Headers**: Use `=`, `==`, `===` for different heading levels
- **Code blocks**: Use `[source,language]` followed by `----` for code blocks
- **Links**: Use `link:url[text]` for external links
- **Includes**: Use `include::filename.adoc[]` to include other files

### Adding New Documentation

1. Create a new `.adoc` file in the `asciidoc/` directory
2. Add the include directive to `index.adoc` if you want it in the main documentation
3. Run `mvn asciidoctor:process-asciidoc` to generate updated HTML

## Configuration

The AsciiDoc Maven plugin is configured in `pom.xml` with the following settings:

- **Source directory**: `docs/asciidoc/`
- **Output directory**: `docs/generated/`
- **Backend**: HTML5
- **Doctype**: Book
- **Features**: Table of contents, syntax highlighting, section numbering

## Continuous Integration

The documentation is automatically generated during the Maven build process in the `prepare-package` phase. 