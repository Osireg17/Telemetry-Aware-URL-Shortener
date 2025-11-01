# UrlShortener

How to start the UrlShortener application
---

1. Run `mvn clean install` to build your application
1. Start application with `java -jar target/url-shortener-api-1.0.0-SNAPSHOT.jar server config.yml`
1. To check that your application is running enter url `http://localhost:8080`

Health Check
---

To see your applications health enter url `http://localhost:8081/healthcheck`

Requirements
---

- Java: This project is configured to compile and run against Java 21 (LTS). Please install a Java 21 JDK and make it available as your default `java` and `javac`, or set `JAVA_HOME` accordingly.

	Examples:

	- SDKMAN: `sdk install java 21-open` then `sdk use java 21-open`
	- Adoptium (macOS, Homebrew): `brew tap --cask homebrew/cask-versions && brew install --cask temurin21`

	After installing, verify with:

	```bash
	mvn -v
	```

	The `mvn -v` output should show Java 21 as the runtime (or a later JDK compatible with --release 21).

Notes / Next steps
---

- Update CI pipelines or Docker base images to use a JDK 21 runtime image (for example `eclipse-temurin:21-jre` or appropriate distro image).
- Consider pinning `maven-compiler-plugin` to a specific version if strict reproducibility is required (current project uses pluginManagement which provides a recent version).
