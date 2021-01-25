# ModListCreator
[![Total downloads](https://img.shields.io/github/downloads/MelanX/ModListCreator/total.svg)](https://www.github.com/MelanX/ModListCreator/releases/)

## How to use
1. Put the ModListCreator file into a folder
2. Extract the `manifest.json` file from an exported modpack file into the same folder
3. Run ModListCreator jar file
4. Wait for all the `modlist.*` files.

## Arguments you could use
| Argument     | Output                                       |
|--------------|----------------------------------------------|
| `--html`     | Exports only an **HTML** file                |
| `--md`       | Exports only a **Markdown** file             |
| `--detailed` | Shows exact version of each mod              |
| `--headless` | Generates the file without pack name/version |

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