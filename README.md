# YinYIAng Warriors
Ashton Tablut player for UNIBO IA Challenge.
<br>
<img src="https://github.com/imRaazy/YinIAngWarriorsTablut/blob/master/logo.png" width=512>
## Installation
Make sure to run JDK 8 and ANT. If not install them into your OS.

Now, clone the project repository:

```
git clone https://github.com/imRaazy/YinIAngWarriorsTablut/
```

## Run the Server

The easiest way is to utilize the ANT configuration script from console.<br>
Go into the project folder (the folder with the `build.xml` file):
```
cd YinIAngWarriorsTablut/Tablut
```

Compile the project:

```
ant clean
ant compile
```

The compiled project is in  the `build` folder.
Run the server with:

```
ant server
```

Check the behaviour using the random players in two different console windows:

```
ant randomwhite

ant randomblack
```

At this point, a window with the game state should appear.

To be able to run other classes, change the `build.xml` file and re-compile everything.

## Run YinIAngWarriors Players

To run the players you can choose two ways:<br>
1) Run the YinIAngWarriors.jar from vmUtils folder with the following command:
```
java -jar YinIAngWarriors.jar -p $playerRole -h $ipAddress -t $timeout
```
2) Import the entire project (Tablut folder) in IntelliJIdea, create a run configuration and run TablutPlayerLauncher.kt

## Contributors
Milo Marchetti - @imRaazy
Nicolò Bartelucci - @nicobargit
Nicolò Saccone - @nicosac97
