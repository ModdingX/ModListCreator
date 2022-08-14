# Modlist
## How to use

1. Put the ModListCreator file into a folder
2. Open terminal/cmd
3. Add `modlist` after `java -jar ModListCreator-<version>-fatjar.jar`
4. Set arguments listed below
5. After all arguments, set the input files (`folder/*` for whole folder)
6. Run it and wait for output file(s)

## Arguments you could use

|   Argument   | Description                                                                                                                     |
|:------------:|---------------------------------------------------------------------------------------------------------------------------------|
|  no-header   | Generates the file without pack name and version                                                                                |
|   detailed   | Shows exact version of each mod                                                                                                 |
|    format    | The output format to use (`plain_text`, `html`, or `markdown` (default))                                                        |
|  **output**  | Defines the output path for generated files. If --pattern is set, describes a directory for output files, else a concrete file. |
|   pattern    | Defines the output file name pattern. %n is replaced with pack name, %v with pack version.                                      |

## Examples
### Detailed

To use this argument, use the following command:

`$ java -jar ModListCreator-<version>-fatjar.jar --detailed`

| Without argument                | With argument                            |
|---------------------------------|------------------------------------------|
| AIOT Botania (by MelanX)        | aiotbotania-1.16.2-1.3.2.jar (by MelanX) |
| Automatic Tool Swap (by MelanX) | ToolSwap-1.16.2-1.2.0.jar (by MelanX)    |
| Botania (by Vazkii)             | Botania-1.16.3-409.jar (by Vazkii)       |

### No Header

To use this argument, use the following command:

`$ java -jar ModListCreator-<version>-fatjar.jar --no-header`

| Without argument                            | With argument |
|---------------------------------------------|---------------|
| Garden of Glass (Questbook Edition) - 4.2.0 | _nothing_     |

### Pattern

To use this argument, use the following command:

`$ java -jar ModListCreator-<version>-fatjar.jar --pattern "This is %n in version %v`

> This is CaveStone in version 0.4.0

### Input

To use this argument, use the following command:

`$ java -jar ModListCreator-<version>-fatjar.jar --pattern "Name" --output output modpacks/*`

This will use the folder `modpacks` as input and tries to generate a modlist for each file in this folder.

### Output

To use this argument, use the following command:

`$ java -jar ModListCreator-<version>-fatjar.jar --output output.md`

This will generate a file called `output.md`. If you set `--pattern` argument, it will generate a folder
called `output.md`.

## Why use this instead of exported modlist?

- This tool sorts the project names alphabetically
- This tool links to the project and the author
- The official `modlist.html` from CurseForge exports contains broken links to the projects