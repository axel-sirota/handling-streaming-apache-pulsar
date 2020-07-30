# Handling Streaming Data with Apache Pulsar
Repository for course Handling Streaming Data with Apache Pulsar @ Pluralsight 

## Installing Apache Pulsar 2.6.0

### Setting Java version

Apache Pulsar runs in OpenJDK11 so we must install it and set `JAVA_HOME` to be it.

#### Windows

Download the installer from [here](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html) and execute it. This will unzip the Java compressed folder, set `JAVA_HOME` and add it to your `PATH`

#### Linux

Run 
```
sudo apt install openjdk-11-jdk
export JAVA_HOME=`/usr/libexec/java_home -v 11`
```

#### Mac

Run

```
brew tap AdoptOpenJDK/openjdk
brew cask install adoptopenjdk8
export JAVA_HOME=`/usr/libexec/java_home -v 11`
```

### Downloading the binary

To install Pulsar I recommend downloading the binary package [here](https://www.apache.org/dyn/mirrors/mirrors.cgi?action=download&filename=pulsar/pulsar-2.6.0/apache-pulsar-2.6.0-bin.tar.gz) and unzip it.

In Unix this is done simply by running:

```
tar xvfz Downloads/apache-pulsar-2.6.0-bin.tar.gz
```

Apache Pulsar has it's own CLI inside the binary package, so to start the standalone instance (the one we have seen in this course) we just have to run in the Terminal (or Powershell for Windows Users)

```
export PATH="/Users/axelsirota/apache-pulsar-2.6.0/bin:$PATH"
pulsar standalone
```

And done!