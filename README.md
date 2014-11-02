# DnsMadeEasy

This is a simple tool that periodically updates your IP when it changes for [DnsMadeEasy](http://www.dnsmadeeasy.com/)'s dynamic DNS. A Windows EXE file is provided, though the tool is written in Java and can be run on any desktop OS.

## Download

Download the latest version here:

[Download DnsMadeEasy](https://github.com/EsotericSoftware/dnsmadeeasy/releases)

There is no installation, only an EXE and JAR file. Windows users only need the EXE file and run it in the usual ways. Other OS users only need the JAR file and run it using `java -jar DnsMadeEasy.jar`.

## Setup

When run the first time, the tool creates a `~/.dnsmadeeasy/config.txt` file containing:

```
User: 
Password: 
Record ID: 
Minutes: 30
Last IP: 
```

`User` and `Password` are your DnsMadeEasy credentials. You may optionally configure DnsMadeEasy to have a password per record, so you don't need to use your account password. `Record ID` identifies the record to update. `Minutes` is the number of minutes between IP checks. `Last IP` does not need to be set manually.

After saving the `config.txt` file, the DnsMadeEasy tool must be restarted. On Windows the tool runs in the background and only a single instance is allowed, so use Task Manager to end the task before starting it again.

Configure your OS to start the tool when the OS starts.

## Troubleshooting

The tool creates a `~/.dnsmadeeasy/dnsmadeeasy.log` file that contains timestamps for when the IP has changed and any error messages.

## License

The tool is released as OSS under the [New BSD license](https://github.com/EsotericSoftware/dnsmadeeasy/blob/master/LICENSE).
