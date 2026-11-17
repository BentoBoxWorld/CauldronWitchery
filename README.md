# CauldronWitchery
[![Discord](https://img.shields.io/discord/272499714048524288.svg?logo=discord)](https://discord.bentobox.world)
[![Build Status](https://ci.codemc.org/buildStatus/icon?job=BentoBoxWorld/CauldronWitchery)](https://ci.codemc.org/job/BentoBoxWorld/job/CauldronWitchery/)

Add-on for BentoBox to provide ability to summon mobs with a magic for any BentoBox GameMode. 

## What is this addon

This addon allows to be a witch and summon things using magic stick and magic recipes to summon mobs or blocks.

## Translations

As most of BentoBox projects, CauldronWitchery Addon is translatable in any language. Everyone can contribute, and translate some parts of the addon in their language via [GitLocalize](https://gitlocalize.com/repo/2976).
If your language is not in the list, please contact to developers via Discord and it will be added there.

## Where to find

Currently CauldronWitchery Addon is in **Beta stage**, so it may or may not contain bugs... a lot of bugs. Also it means, that some features are not working or implemented. 
You can download it from [Release tab](https://github.com/BentoBoxWorld/CauldronWitchery/releases)

Or you can try **nightly builds** where you can check and test new features that will be implemented in next release from [Jenkins Server](https://ci.codemc.org/job/BentoBoxWorld/job/CauldronWitchery/lastStableBuild/).

If you like this addon but something is missing or is not working as you want, you can always submit an [Issue request](https://github.com/BentoBoxWorld/CauldronWitchery/issues) or get a support in Discord [BentoBox ![icon](https://avatars2.githubusercontent.com/u/41555324?s=15&v=4)](https://discord.bentobox.world)

## How to use

1. Place the addon jar in the addons folder of the BentoBox plugin
2. Restart the server
3. The addon will create a data folder and inside the folder will be a config.yml, template.yml and books folder.
4. When server is started you can use `/[admin_cmd] witchery` command to access admin menu.
   * With admin menu you can change addon settings
   * With admin menu you can create your magic sticks and recipes.

You can also create magic sticks and recipes using template file. To import template file you need to use admin panel.


## Placeholders

CauldronWitchery registers [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) placeholders for every GameMode it hooks into. Replace `[gamemode]` with the lower-case GameMode name, e.g. `bskyblock`.

| Placeholder | Description |
|---|---|
| `%[gamemode]_cauldronwitchery_sticks%` | Number of magic sticks the player has purchased |
| `%[gamemode]_cauldronwitchery_recipes_used%` | Number of recipes the player has successfully used |
| `%[gamemode]_cauldronwitchery_recipes_used_<recipe_id>%` | Number of times the player has successfully used a specific recipe |
| `%[gamemode]_cauldronwitchery_summons%` | Number of successful mob summons |

Recipe IDs are unique per GameMode. They can be set with the `id` key in `template.yml`; otherwise they are generated from the recipe outcome (e.g. `sheep`, `diamond`, or the book name), with `_2`, `_3`, ... added if the same outcome is used by more than one recipe. Use `/bbox dump-placeholders` to list all registered placeholders.

## Compatibility

- [x] BentoBox - 3.23.7 or later
- [x] Paper - 26.3
- [x] Java - 25


## Information

More information can be found in [Wiki Pages](https://docs.bentobox.world/en/latest/addons/CauldronWitchery/).
