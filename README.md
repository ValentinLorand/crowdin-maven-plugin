# crowdin-maven

This project is forked from [glandais/crowdin-maven](https://github.com/glandais/crowdin-maven).

It has been rewritten to use the version 2 of the crowdin API. (The API V1 is deprecated).

This plugin allows Maven projects to be translated using crowdin.

# Quick start

## Configuration

Add a server to your ~/.m2/settings.xml (keeping your API key private)

```xml
<settings>
<!-- ... -->
 <servers>
  <!-- ... -->
  <server>
   <id>crowdin-server</id>
   <username>TheIdOfTheCrowdinProject<username>
   <password>YourPersonalApiToken/password>
  <server>
 </servers>
<!-- ... -->
 <pluginGroups>
  <!-- ... -->
  <pluginGroup>com.googlecode.crowdin-maven</pluginGroup>
 </pluginGroups>
</settings>
```

Configure your build for crowdin usage in your project's pom.xml :

```xml
<project>
<!-- ... -->
 <build>
 <!-- ... -->
  <plugins>
   <!-- ... -->
   <plugin>
    <groupId>com.googlecode.crowdin-maven</groupId>
    <artifactId>crowdin-plugin</artifactId>
    <version>LATEST</version>   
     <executions>
      <execution>
       <goals>
        <goal>aggregate</goal>
       </goals>
      </execution>
     </executions>
     <configuration>
      <crowdinServerId>crowdin-server</crowdinServerId>
     </configuration>
   </plugin>
   <!-- ... -->
  </plugin>
  <!-- ... -->
 </build>
 <!-- ... -->
</project>
```

## Pushing translations to crowdin

Put your messages files in properties format in src/main/messages.

| *Goal*             | *Description*                                                                                                      |
|--------------------|--------------------------------------------------------------------------------------------------------------------|
| `mvn crowdin:push` | Push the messages files on crowdin.<br> It is a Maven first, files or keys not in Maven will be erased on crowdin. |

## Getting translations from crowdin

Retrieve the translations from crowdin and put them in src/main/crowdin.

| *Goal*                  | *Description*                                                                                                                                                                                                                                                                                                                                          |
|-------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `mvn crowdin:pull`      | Retrieve messages from crowdin in `src/main/crowdin`.<br>`src/main/crowdin` must be considered as a derived resource. Do not edit those files.                                                                                                                                                                                                         |
