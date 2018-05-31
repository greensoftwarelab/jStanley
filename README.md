# jStanley
jStanley is a static analyzer developed as an Eclipse plugin. The tool is capable of statically detecting the usage of energy-inefficient collections and suggest better alternatives. It can do the same, but considering the execution time or both energy and time at the same time.

## Instalation
- Download or clone this repository
- Move the file **greenlab_1.0.0.201805311446.jar** located inside the folder **pluginjar** to eclipse **dropins** folder
- Depending on the Operating System this folder may be located at:
	- **Linux**: usr/share/eclipse 
	- **OSX**: /Applications/Eclipse.app/Contents/Eclipse
- Restart Eclipse

## How to use
- Click ![jStanley](https://github.com/greensoftwarelab/jStanley/blob/master/icons/sample.png) icon located in your Eclipse toolbar menu
- jStanley evaluates all open projects in Eclipse
- To change the default analysis type and population size, click on the dropdown arrow o the right side of ![jStanley](https://github.com/greensoftwarelab/jStanley/blob/master/icons/sample.png)
	- Select one population size from 25K, 250K or 1M
	- Select the analysis type from Joules and/or Miliseconds


## Known bugs