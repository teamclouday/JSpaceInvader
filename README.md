# Space Invader  

A game written in Java, as a homework to CSE389  

------

Was intended to run in terminal, but Java does not support terminal related functions natively  
So turn to JFrame  

See `design.txt` for game object design  

------

### How to play  

In your terminal, run:  
```bash
javac *.java
java Game
```

Select a level and start playing  

### Easy Control  

1. `Esc` or close window to stop the game (`Esc` is recommended)  
2. `W` or `UP` to move up  
3. `A` or `LEFT` to move left  
4. `S` or `DOWN` to move down  
5. `D` or `RIGHT` to move right  
6. `Space` to shoot  

### Documentation  
Generate documentation of this project by:  
```bash
javadoc -d documentation *.java
```

------

### Screenshots  
1. Start screen  
   ![start screen](img/screenshot1.png)  
2. In-game play  
   ![ingame screen](img/screenshot2.png)  
3. Terminal  
   ![terminal screen](img/screenshot3.png)  

------

### Potential Improvements  
* Add inertia to enemies' movements, so that they move more fluently  
* Enemy may learn to avoid the bullets  
* Improve the shape and variety of enemies  