# Natural Disasters Plugin

## Setup

First of all, **make sure that you're running Java 20+**, this will not work if you are using a newer Java version. I
haven't tested it with
older versions.

You'll need Spigot's BuildTools, you can download it
from [here](https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar).

With BuildTools inside some folder you'll need to run it as:

````
java -jar BuildTools.jar --rev 1.21.1 --remapped
````

This will take some time, BuildTools will download and patch a lot of things, as well as build stuff to your local
maven repository.

After this, compiling the project with `maven package` should work. The output file that is used is the
`NaturalDisaster-0.1-remapped.jar`, you can just place it inside your plugins folder.

## Compiling

Just run

```
mvn package
```

After that the target jar that you'll want is `NaturalDisaster-<VERSION>-remapped.jar` 
