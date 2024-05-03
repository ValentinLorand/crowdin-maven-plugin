# crowdin-maven

This project is forked from [glandais/crowdin-maven](https://github.com/glandais/crowdin-maven).

It has been rewritten to use the version 2 of the crowdin API. (The API V1 is deprecated).

This plugin allows Maven projects to be translated using crowdin.

# Quick start

## Configuration

Add a server in your `~/.m2/settings.xml` (keeping your API key private).

You can generate your personal API key in your crowdin settings.

```xml
<settings>
    <servers>
        <server>
            <id>crowdin-server</id>
            <username>TheIdOfTheCrowdinProject</username>
            <password>YourPersonalApiToken/password></password>
        </server>
    </servers>
</settings>
```

Configure your build for crowdin usage in your project's pom.xml :

```xml
<pluginManagement>
    <plugin>
        <groupId>com.googlecode.crowdin-maven</groupId>
        <artifactId>crowdin-plugin</artifactId>
        <version>${crowdin-plugin.version}</version>
        <configuration>
            <crowdinServerId>crowdin-server</crowdinServerId>
        </configuration>
    </plugin>
</pluginManagement>
```

## Pushing translations to crowdin

Put your messages files in properties format in `src/main/messages`.

> [WARNING] The plugin will update all the existing files on Crowdin, create new ones and delete the ones that are not in the folder.

| *Goal*             | *Description*                               |
|--------------------|---------------------------------------------|
| `mvn crowdin:push` | Push the messages files on Crowdin server . |

## Pull translations from crowdin

Retrieve the translations from crowdin and put them in `src/main/crowdin`.

> [WARNING] The files in the crowdin folder should not be modified manually.

| *Goal*                  | *Description*                          |
|-------------------------|----------------------------------------|
| `mvn crowdin:pull`      | Retrieve messages from crowdin server. |

## To be implemented

- Branches management