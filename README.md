# ModListCreator
[![Total downloads](https://img.shields.io/github/downloads/MelanX/ModListCreator/total.svg)](https://www.github.com/MelanX/ModListCreator/releases/)

## How to use
1. Put the ModListCreator file into a folder
2. Extract the `manifest.json` file from an exported modpack file into the same folder
3. Run ModListCreator jar file
4. Wait for all the `output/modlist.*` files

## Arguments you could use
| Argument          | Output                                             |
|-------------------|----------------------------------------------------|
| `md` / `markdown` | Exports a **Markdown** files                       |
| `html`            | Exports an **HTML** files                          |
| `detailed`        | Shows exact version of each mod                    |
| `headless`        | Generates the file without pack name/version       |
| `nameFormat`      | Defines the name format                            |
| `manifest`        | Defines manifest file                              |
| `input`           | Defines the input directory for multiple manifests |
| `output`          | Defines the output directory for generated files   |
| `workingDir`      | Defines the path where input and output should be  |

### MD / Markdown / HTML
To use this argument, use the following command:

`$ java -jar ModListCreator-<version>.jar --md`

OR 


`$ java -jar ModListCreator-<version>.jar --markdown`

OR

`$ java -jar ModListCreator-<version>.jar --html`

This will generate only the given file type. Otherwise it will generate all types.

### Detailed
To use this argument, use the following command:

`$ java -jar ModListCreator-<version>.jar --detailed`

| Without argument                | With argument                            |
|---------------------------------|------------------------------------------|
| AIOT Botania (by MelanX)        | aiotbotania-1.16.2-1.3.2.jar (by MelanX) |
| Automatic Tool Swap (by MelanX) | ToolSwap-1.16.2-1.2.0.jar (by MelanX)    |
| Botania (by Vazkii)             | Botania-1.16.3-409.jar (by Vazkii)       |

### Headless
To use this argument, use the following command:

`$ java -jar ModListCreator-<version>.jar --headless`

| Without argument                            | With argument |
|---------------------------------------------|---------------|
| Garden of Glass (Questbook Edition) - 4.2.0 | *nothing*     |

### NameFormat
To use this argument, use the following command:

`$ java -jar ModListCreator-<version>.jar --nameFormat DEFAULT`

Allowed values:

| Format type  | Output                                         |
|--------------|------------------------------------------------|
| DEFAULT      | modlist.md                                     |
| VERSION      | 4.2.0.md                                       |
| NAME         | Garden of Glass (Questbook Edition).md         |
| NAME_VERSION | Garden of Glass (Questbook Edition) - 4.2.0.md |

If you generate .html files, the extension will be `.html`.

### Input
To use this argument, use the following command:

`$ java -jar ModListCreator-<version>.jar --input examplePath`

If you don't set the input, it will just use the [manifest.json](#manifest).

Otherwise, this will try to get each file in `examplePath` and creates an modlist in the [output](#output).

### Output
To use this argument, use the following command:

`$ java -jar ModListCreator-<version>.jar --output examplePath`

If you don't set the output, it will just use the `output` folder in the [base directory](#workingDir).

Otherwise, this will try to generate all the modlist files into `examplePath`. If the folder doesn't exist, it will be generated.

### Manifest
To use this argument, use the following command:

`$ java -jar ModListCreator-<version>.jar --manifest example.json`

If you don't set the manifest, it will just use the `manifest.json` in the [base directory](#workingDir).

### WorkingDir
To use this argument, use the following command:

`$ java -jar ModListCreator-<version>.jar --workingDir D:\path\to\directory`

If you don't set this, it will use the location of you Jar as base directory.

## Why use this instead of exported modlist?
- This tool sorts the project names alphabetically
- This tool links to the project and the author
- The official `modlist.html` contains broken links to the projects

## How to get
1. [Download here](https://github.com/MelanX/ModListCreator/releases)

OR

1. Clone this repository
2. Run `gradlew build`
3. Get file from path/build/libs/