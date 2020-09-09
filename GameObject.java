// This class contains all the definitions for the game objects

import java.util.Random;
import java.util.LinkedList;
import java.util.ArrayList;

/**
 * Stores all game objects as static classes
 */
public class GameObject
{
    /**
     * Define move direction types
     */
    public static enum MoveDirection
    {
        DIR_UP(0),
        DIR_DOWN(1),
        DIR_LEFT(2),
        DIR_RIGHT(3),
        DIR_NONE(4);
        public final int num;
        private MoveDirection(int num){this.num = num;}
    }

    /**
     * The bullet class
     */
    public static class Bullet
    {
        private final String design = "*";
        private int xPos, yPos, yDelta;
        private boolean isEnemy;

        public Bullet(int xPos, int yPos, MoveDirection dir, boolean isEnemy)
        {
            this.xPos = xPos;
            this.yPos = yPos;
            this.isEnemy = isEnemy;
            // dir can only be up or down
            switch(dir)
            {
                case DIR_UP:
                    this.yDelta = -1;
                    break;
                case DIR_DOWN:
                    this.yDelta = 1;
                    break;
                default:
                    this.yDelta = 0;
                    break;
            }
        }

        /**
         * Update the bullet object based on current frame
         * @param frame
         * @return array of render commands
         */
        public ArrayList<Renderer.RenderCommand> update(boolean frame)
        {
            ArrayList<Renderer.RenderCommand> commands = new ArrayList<>();
            if(frame)
            {
                commands.add(new Renderer.RenderCommand(xPos, yPos, " "));
                yPos += yDelta;
                commands.add(new Renderer.RenderCommand(xPos, yPos, design));
            }
            else
                commands.add(new Renderer.RenderCommand(xPos, yPos, design));
            return commands;
        }

        public boolean isEnemy(){return isEnemy;}
        public int getY(){return yPos;}
    }

    /**
     * The abstract Space Ship class for all space ships
     */
    public static abstract class SpaceShip
    {
        /**
         * Whether the ship is an enemy
         */
        public boolean isEnemy;
        /**
         * Update the ship position based on direction
         * @param dir
         * @return array of render commands
         */
        public abstract ArrayList<Renderer.RenderCommand> update(MoveDirection dir);
        /**
         * Explode the ship after it's dead
         * @return array of render commands
         */
        public abstract ArrayList<Renderer.RenderCommand> explode();
        /**
         * Shoot bullets
         */
        public abstract void shoot();
        /**
         * The ship gets hit by a bullet
         */
        public abstract void hit();
        /**
         * Is the ship still alive?
         * @return true or false
         */
        public abstract boolean isAlive();
    }
    
    /**
     * Defines my ship
     * @see GameObject.SpaceShip
     */
    public static class MyShip extends SpaceShip
    {
        private int xMax, yMax;
        public int xPos, yPos;
        private int d_HP;
        //  A
        // | |
        //<=-=>
        private final String[] design = {"A", "| |", "<=-=>"};
        private final String[] designClean = {" ", "   ", "     "};
        private final int[] possibleHPs = {12, 6, 3};
        // these 2 values define the size of my ship
        public final int offsetX = 1; // max offset from center X
        public final int offsetY = 2; // max offset from center Y

        public MyShip(int level, int xPos, int yPos, int xMax, int yMax)
        {
            this.isEnemy = false;
            this.xPos = xPos; this.yPos = yPos;
            this.xMax = xMax; this.yMax = yMax;
            // define HP by level
            level = (level > 0) ? level : 0;
            level = (level < 3) ? level : 2;
            d_HP = possibleHPs[level];
        }

        @Override
        public ArrayList<Renderer.RenderCommand> update(GameObject.MoveDirection dir) 
        {
            ArrayList<Renderer.RenderCommand> commands = new ArrayList<>();
            if(dir != MoveDirection.DIR_NONE)
            {
                // clear previous draw
                commands.add(new Renderer.RenderCommand(xPos, yPos-1, designClean[0]));
                commands.add(new Renderer.RenderCommand(xPos-1, yPos, designClean[1]));
                commands.add(new Renderer.RenderCommand(xPos-2, yPos+1, designClean[2]));
            }
            switch(dir)
            {
                case DIR_DOWN:
                    yPos += 1;
                    yPos = (yPos > (yMax - 1)) ? (yMax - 1) : yPos;
                    break;
                case DIR_UP:
                    yPos -= 1;
                    yPos = (yPos < yMax/3*2) ? yMax/3*2 : yPos;
                    break;
                case DIR_LEFT:
                    xPos -= 1;
                    xPos = (xPos < 2) ? 2 : xPos;
                    break;
                case DIR_RIGHT:
                    xPos += 1;
                    xPos = (xPos > xMax - 2) ? (xMax - 2) : xPos;
                    break;
                default:
                    break;
            }
            // draw new body
            commands.add(new Renderer.RenderCommand(xPos, yPos-1, design[0]));
            commands.add(new Renderer.RenderCommand(xPos-1, yPos, design[1]));
            commands.add(new Renderer.RenderCommand(xPos-2, yPos+1, design[2]));
            return commands;
        }

        @Override
        public ArrayList<Renderer.RenderCommand> explode() 
        {
            ArrayList<Renderer.RenderCommand> commands = new ArrayList<>();
            // clear previous draw
            commands.add(new Renderer.RenderCommand(xPos, yPos-1, designClean[0]));
            commands.add(new Renderer.RenderCommand(xPos-1, yPos, designClean[1]));
            commands.add(new Renderer.RenderCommand(xPos-2, yPos+1, designClean[2]));
            return commands;
        }

        @Override
        public void shoot() 
        {
            
        }

        @Override
        public void hit() {this.d_HP--;}
        @Override
        public boolean isAlive() {return this.d_HP > 0;}
    }

    /**
     * Defines enemy ship A
     * @see GameObject.SpaceShip
     */
    public static class EnemyA extends SpaceShip
    {
        private int xMax, yMax;
        public int xPos, yPos;
        private int d_HP;
        //<v>
        private final String[] design = {"<v>"};
        private final String[] designClean = {"   "};
        // these 2 values define the size of the ship
        public final int offsetX = 1; // max offset from center X
        public final int offsetY = 0; // max offset from center Y

        public EnemyA(int xPos, int yPos, int xMax, int yMax)
        {
            this.isEnemy = true;
            this.xPos = xPos; this.yPos = yPos;
            this.xMax = xMax; this.yMax = yMax;
            d_HP = 2;
        }

        @Override
        public ArrayList<Renderer.RenderCommand> update(GameObject.MoveDirection dir) 
        {
            ArrayList<Renderer.RenderCommand> commands = new ArrayList<>();
            if(dir != MoveDirection.DIR_NONE)
            {
                // clear previous draw
                commands.add(new Renderer.RenderCommand(xPos-1, yPos, designClean[0]));
            }
            switch(dir)
            {
                case DIR_DOWN:
                    yPos += 1;
                    yPos = (yPos > yMax/2) ? yMax/2 : yPos;
                    break;
                case DIR_UP:
                    yPos -= 1;
                    yPos = (yPos < 0) ? 0 : yPos;
                    break;
                case DIR_LEFT:
                    xPos -= 1;
                    xPos = (xPos < 1) ? 1 : xPos;
                    break;
                case DIR_RIGHT:
                    xPos += 1;
                    xPos = (xPos > xMax-1) ? xMax-1 : xPos;
                    break;
                default:
                    break;
            }
            // draw new body
            commands.add(new Renderer.RenderCommand(xPos-1, yPos, design[0]));
            return commands;
        }

        @Override
        public ArrayList<Renderer.RenderCommand> explode() 
        {
            ArrayList<Renderer.RenderCommand> commands = new ArrayList<>();
            // clear previous draw
            commands.add(new Renderer.RenderCommand(xPos-1, yPos, designClean[0]));
            return commands;
        }

        @Override
        public void shoot() 
        {
            
        }

        @Override
        public void hit() {this.d_HP--;}
        @Override
        public boolean isAlive() {return this.d_HP > 0;}
    }

    /**
     * Defines enemy ship B
     * @see GameObject.SpaceShip
     */
    public static class EnemyB extends SpaceShip
    {
        private int xMax, yMax;
        public int xPos, yPos;
        private int d_HP;
        //[===]
        // ( )
        //  v
        private final String[] design = {"[===]", "( )", "v"};
        private final String[] designClean = {"     ", "   ", " "};
        // these 2 values define the size of the ship
        public final int offsetX = 2; // max offset from center X
        public final int offsetY = 1; // max offset from center Y

        public EnemyB(int xPos, int yPos, int xMax, int yMax)
        {
            this.isEnemy = true;
            this.xPos = xPos; this.yPos = yPos;
            this.xMax = xMax; this.yMax = yMax;
            d_HP = 4;
        }

        @Override
        public ArrayList<Renderer.RenderCommand> update(GameObject.MoveDirection dir) 
        {
            ArrayList<Renderer.RenderCommand> commands = new ArrayList<>();
            if(dir != MoveDirection.DIR_NONE)
            {
                // clear previous draw
                commands.add(new Renderer.RenderCommand(xPos-2, yPos-1, designClean[0]));
                commands.add(new Renderer.RenderCommand(xPos-1, yPos, designClean[1]));
                commands.add(new Renderer.RenderCommand(xPos, yPos+1, designClean[2]));
            }
            switch(dir)
            {
                case DIR_DOWN:
                    yPos += 1;
                    yPos = (yPos > yMax/2) ? yMax/2 : yPos;
                    break;
                case DIR_UP:
                    yPos -= 1;
                    yPos = (yPos < 1) ? 1 : yPos;
                    break;
                case DIR_LEFT:
                    xPos -= 1;
                    xPos = (xPos < 2) ? 2 : xPos;
                    break;
                case DIR_RIGHT:
                    xPos += 1;
                    xPos = (xPos > xMax-2) ? xMax-2 : xPos;
                    break;
                default:
                    break;
            }
            // draw new body
            commands.add(new Renderer.RenderCommand(xPos-2, yPos-1, design[0]));
            commands.add(new Renderer.RenderCommand(xPos-1, yPos, design[1]));
            commands.add(new Renderer.RenderCommand(xPos, yPos+1, design[2]));
            return commands;
        }

        @Override
        public ArrayList<Renderer.RenderCommand> explode() 
        {
            ArrayList<Renderer.RenderCommand> commands = new ArrayList<>();
            // clear previous draw
            commands.add(new Renderer.RenderCommand(xPos-2, yPos-1, designClean[0]));
            commands.add(new Renderer.RenderCommand(xPos-1, yPos, designClean[1]));
            commands.add(new Renderer.RenderCommand(xPos, yPos+1, designClean[2]));
            return commands;
        }

        @Override
        public void shoot() 
        {
            
        }

        @Override
        public void hit() {this.d_HP--;}
        @Override
        public boolean isAlive() {return this.d_HP > 0;}
    }

    /**
     * Defines enemy ship C
     * @see GameObject.SpaceShip
     */
    public static class EnemyC extends SpaceShip
    {
        private int xMax, yMax;
        public int xPos, yPos;
        private int d_HP;
        //<[-----]>
        //  #####
        //   %%%
        //    V
        private final String[] design = {"<[-----]>", "#####", "%%%", "V"};
        private final String[] designClean = {"         ", "     ", "   ", " "};
        // these 2 values define the size of the ship
        public final int offsetX = 4; // max offset from center X
        public final int offsetY = 2; // max offset from center Y

        public EnemyC(int xPos, int yPos, int xMax, int yMax)
        {
            this.isEnemy = true;
            this.xPos = xPos; this.yPos = yPos;
            this.xMax = xMax; this.yMax = yMax;
            d_HP = 10;
        }

        @Override
        public ArrayList<Renderer.RenderCommand> update(GameObject.MoveDirection dir) 
        {
            ArrayList<Renderer.RenderCommand> commands = new ArrayList<>();
            if(dir != MoveDirection.DIR_NONE)
            {
                // clear previous draw
                commands.add(new Renderer.RenderCommand(xPos-4, yPos-1, designClean[0]));
                commands.add(new Renderer.RenderCommand(xPos-2, yPos, designClean[1]));
                commands.add(new Renderer.RenderCommand(xPos-1, yPos+1, designClean[2]));
                commands.add(new Renderer.RenderCommand(xPos, yPos+2, designClean[3]));
            }
            switch(dir)
            {
                case DIR_DOWN:
                    yPos += 1;
                    yPos = (yPos > yMax/2) ? yMax/2 : yPos;
                    break;
                case DIR_UP:
                    yPos -= 1;
                    yPos = (yPos < 1) ? 1 : yPos;
                    break;
                case DIR_LEFT:
                    xPos -= 1;
                    xPos = (xPos < 4) ? 4 : xPos;
                    break;
                case DIR_RIGHT:
                    xPos += 1;
                    xPos = (xPos > xMax-4) ? xMax-4 : xPos;
                    break;
                default:
                    break;
            }
            // draw new body
            commands.add(new Renderer.RenderCommand(xPos-4, yPos-1, design[0]));
            commands.add(new Renderer.RenderCommand(xPos-2, yPos, design[1]));
            commands.add(new Renderer.RenderCommand(xPos-1, yPos+1, design[2]));
            commands.add(new Renderer.RenderCommand(xPos, yPos+2, design[3]));
            return commands;
        }

        @Override
        public ArrayList<Renderer.RenderCommand> explode() 
        {
            ArrayList<Renderer.RenderCommand> commands = new ArrayList<>();
            // clear previous draw
            commands.add(new Renderer.RenderCommand(xPos-4, yPos-1, designClean[0]));
            commands.add(new Renderer.RenderCommand(xPos-2, yPos, designClean[1]));
            commands.add(new Renderer.RenderCommand(xPos-1, yPos+1, designClean[2]));
            commands.add(new Renderer.RenderCommand(xPos, yPos+2, designClean[3]));
            return commands;
        }

        @Override
        public void shoot() 
        {
            
        }

        @Override
        public void hit() {this.d_HP--;}
        @Override
        public boolean isAlive() {return this.d_HP > 0;}
    }

    /**
     * The background manager
     */
    public static class Background
    {
        private final String design = "|"; // defines the shape of each meteorite
        private final int sparsity = 10; // in range (0, 1000), only defines horizontal sparsity
        private LinkedList<ArrayList<Renderer.RenderCommand>> data;
        private int xMax, yMax;
        private Random myRand;

        // xMax and yMax should be in terms of font size
        public Background(int xMax, int yMax)
        {
            this.xMax = xMax;
            this.yMax = yMax;
            myRand = new Random();
            data = new LinkedList<>();
            for(int i = 0; i < this.yMax; i++)
            {
                data.add(randomizeNewLine(i));
            }
        }

        /**
         * Update the background
         * @return array of render commands
         */
        public ArrayList<Renderer.RenderCommand> update()
        {
            ArrayList<Renderer.RenderCommand> commands = new ArrayList<>();
            
            for(int i = 0; i < data.size() - 1; i++)
            {
                ArrayList<Renderer.RenderCommand> thisline = data.get(i);
                for(int j = 0; j < thisline.size(); j++)
                {
                    Renderer.RenderCommand cm = thisline.get(j);
                    commands.add(new Renderer.RenderCommand(cm.getX(), cm.getY(), " ")); // remove previous draw
                    cm.setY(cm.getY() + 1); // update data
                    commands.add(new Renderer.RenderCommand(cm.getX(), cm.getY(), cm.getData())); // draw new data
                }
            }
            // process the last line
            ArrayList<Renderer.RenderCommand> lastline = data.get(data.size()-1);
            for(int j = 0; j < lastline.size(); j++)
            {
                Renderer.RenderCommand cm = lastline.get(j);
                commands.add(new Renderer.RenderCommand(cm.getX(), cm.getY(), " "));
            }
            // randomize a new line and add to front
            ArrayList<Renderer.RenderCommand> newline = randomizeNewLine(0);
            data.removeLast();
            data.addFirst(newline);
            commands.addAll(newline);

            return commands;
        }

        /**
         * Randomize a new line of meteorites for background
         * @param y (which line)
         * @return array of render commands
         */
        private ArrayList<Renderer.RenderCommand> randomizeNewLine(int y)
        {
            ArrayList<Renderer.RenderCommand> newline = new ArrayList<>();
            for(int i = 0; i < xMax; i++)
            {
                if(myRand.nextInt(1000) < sparsity)
                {
                    newline.add(new Renderer.RenderCommand(i, y, design));
                }
            }
            return newline;
        }
    }

    /**
     * The fps controller object
     */
    public static class FPSController
    {
        private long tPrev = 0;
        private long tNow = 0;
        private long spf = 0;

        public FPSController(int fps)
        {
            this.spf = (long)(1000 / (float)fps);
            tPrev = System.currentTimeMillis();
            tNow = System.currentTimeMillis();
        }

        /**
         * Update time, and pause the thread to limit fps
         */
        public void update()
        {
            tNow = System.currentTimeMillis();
            long delta = tNow - tPrev;
            if(delta < spf)
            {
                try
                {
                    Thread.sleep(spf - delta);
                }catch(InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                }
            }
            tPrev = System.currentTimeMillis();
        }
    }
}
