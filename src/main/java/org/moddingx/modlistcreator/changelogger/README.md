# Changelog
## How to use

1. Put the ModListCreator file into a folder
2. Open terminal/cmd
3. Add `changelog` after `java -jar ModListCreator-<version>-fatjar.jar`
4. Set arguments listed below
5. Run it and wait for output file

## Arguments you could use

|  Argument  | Description                                                              |
|:----------:|--------------------------------------------------------------------------|
|    old     | Defines the old modpack zip or json file (defaults to `./old.json`)      |
|    new     | Defines the new modpack zip or json file (defaults to `./new.json`)      |
|   output   | Defines the output file name without extension (defaults to `changelog`) |
|   format   | The output format to use (`plain_text`, `html`, or `markdown` (default)  |
