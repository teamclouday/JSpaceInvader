// This class contains all the definitions for the game objects

import java.util.Random;
import java.util.LinkedList;
import java.util.ArrayList;
import java.awt.Color;

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
        public int xPos, yPos;
        private int yDelta;
        private boolean isEnemy;
        private Color color;

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
            if(isEnemy)
                color = Color.RED;
            else
                color = Color.CYAN;
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
                commands.add(new Renderer.RenderCommand(xPos, yPos, design, color));
            }
            else
                commands.add(new Renderer.RenderCommand(xPos, yPos, design, color));
            return commands;
        }

        /**
         * Undraw the bullet after it explode (or hit)
         * @return a render command
         */
        public Renderer.RenderCommand explode()
        {
            return new Renderer.RenderCommand(xPos, yPos, " ");
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
         * Set the offset from center position of the ship
         */
        public final int offsetX;
        public final int offsetY;
        /**
         * Set the center position of the ship
         */
        public int xPos, yPos;
        /**
         * Whether the ship gets hit just now
         */
        protected boolean getHitJustNow;
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
         * @return array of new bullets
         */
        public abstract ArrayList<Bullet> shoot();
        /**
         * The ship gets hit by a bullet
         * @return array of special render command
         */
        public abstract ArrayList<Renderer.RenderCommand> hit();
        /**
         * Is the ship still alive?
         * @return true or false
         */
        public abstract boolean isAlive();
        /**
         * Constructor of the abstract class
         * @param offsetX
         * @param offsetY
         */
        public SpaceShip(int offsetX, int offsetY)
        {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }
    }
    
    /**
     * Defines my ship
     * @see GameObject.SpaceShip
     */
    public static class MyShip extends SpaceShip
    {
        private int xMax, yMax;
        public int d_HP;
        //  A
        // | |
        //<=-=>
        private final String[] design = {"A", "| |", "<=-=>"};
        private final String[] designClean = {" ", "   ", "     "};
        private final int[] possibleHPs = {20, 10, 5, 1};
        // these 2 values define the shoot timeout
        private final int shootTimeout = 200;
        private long shootTimer = 0;

        public MyShip(int level, int xPos, int yPos, int xMax, int yMax)
        {
            super(1, 2);
            this.isEnemy = false;
            this.xPos = xPos; this.yPos = yPos;
            this.xMax = xMax; this.yMax = yMax;
            // define HP by level
            level = (level > 0) ? level : 0;
            level = (level < 4) ? level : 3;
            d_HP = possibleHPs[level];
            shootTimer = System.currentTimeMillis();
            getHitJustNow = false;
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
            Color color = Color.WHITE;
            if(dir == MoveDirection.DIR_NONE && getHitJustNow)
            {
                getHitJustNow = false;
                color = Color.RED;
            }
            commands.add(new Renderer.RenderCommand(xPos, yPos-1, design[0], color));
            commands.add(new Renderer.RenderCommand(xPos-1, yPos, design[1], color));
            commands.add(new Renderer.RenderCommand(xPos-2, yPos+1, design[2], color));
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
        public ArrayList<Bullet> shoot() 
        {
            ArrayList<Bullet> newBullets = new ArrayList<>();
            if(System.currentTimeMillis() - shootTimer > shootTimeout)
            {
                // my ship can only shoot one bullet a time
                newBullets.add(new Bullet(xPos, yPos - 2, MoveDirection.DIR_UP, false));
                shootTimer = System.currentTimeMillis();
            }
            return newBullets;
        }

        @Override
        public ArrayList<Renderer.RenderCommand> hit() 
        {
            this.d_HP--;
            getHitJustNow = true;
            ArrayList<Renderer.RenderCommand> commands = new ArrayList<>();
            commands.add(new Renderer.RenderCommand(xPos, yPos-1, design[0], Color.RED));
            commands.add(new Renderer.RenderCommand(xPos-1, yPos, design[1], Color.RED));
            commands.add(new Renderer.RenderCommand(xPos-2, yPos+1, design[2], Color.RED));
            return commands;
        }
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
        private int d_HP;
        //<v>
        private final String[] design = {"<v>"};
        private final String[] designClean = {"   "};
        // these 2 values define the shoot timeout
        private final int shootTimeout = 1200;
        private long shootTimer = 0;

        public EnemyA(int xPos, int yPos, int xMax, int yMax)
        {
            super(1, 0);
            this.isEnemy = true;
            this.xPos = xPos; this.yPos = yPos;
            this.xMax = xMax; this.yMax = yMax;
            d_HP = 2;
            shootTimer = System.currentTimeMillis();
            getHitJustNow = false;
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
            Color color = Color.WHITE;
            if(dir == MoveDirection.DIR_NONE && getHitJustNow)
            {
                getHitJustNow = false;
                color = Color.RED;
            }
            commands.add(new Renderer.RenderCommand(xPos-1, yPos, design[0], color));
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
        public ArrayList<Bullet> shoot() 
        {
            ArrayList<Bullet> newBullets = new ArrayList<>();
            if(System.currentTimeMillis() - shootTimer > shootTimeout)
            {
                // enemy A can only shoot one bullet a time
                newBullets.add(new Bullet(xPos, yPos + 1, MoveDirection.DIR_DOWN, true));
                shootTimer = System.currentTimeMillis();
            }
            return newBullets;
        }

        @Override
        public ArrayList<Renderer.RenderCommand> hit() 
        {
            this.d_HP--;
            getHitJustNow = true;
            ArrayList<Renderer.RenderCommand> commands = new ArrayList<>();
            commands.add(new Renderer.RenderCommand(xPos-1, yPos, design[0], Color.RED));
            return commands;
        }
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
        private int d_HP;
        //[===]
        // ( )
        //  v
        private final String[] design = {"[===]", "( )", "v"};
        private final String[] designClean = {"     ", "   ", " "};
        // these 2 values define the shoot timeout
        private final int shootTimeout = 900;
        private long shootTimer = 0;

        public EnemyB(int xPos, int yPos, int xMax, int yMax)
        {
            super(2, 1);
            this.isEnemy = true;
            this.xPos = xPos; this.yPos = yPos;
            this.xMax = xMax; this.yMax = yMax;
            d_HP = 4;
            shootTimer = System.currentTimeMillis();
            getHitJustNow = false;
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
            Color color = Color.WHITE;
            if(dir == MoveDirection.DIR_NONE && getHitJustNow)
            {
                getHitJustNow = false;
                color = Color.RED;
            }
            commands.add(new Renderer.RenderCommand(xPos-2, yPos-1, design[0], color));
            commands.add(new Renderer.RenderCommand(xPos-1, yPos, design[1], color));
            commands.add(new Renderer.RenderCommand(xPos, yPos+1, design[2], color));
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
        public ArrayList<Bullet> shoot() 
        {
            ArrayList<Bullet> newBullets = new ArrayList<>();
            if(System.currentTimeMillis() - shootTimer > shootTimeout)
            {
                // enemy B can only shoot one bullet a time
                newBullets.add(new Bullet(xPos, yPos + 2, MoveDirection.DIR_DOWN, true));
                shootTimer = System.currentTimeMillis();
            }
            return newBullets;
        }

        @Override
        public ArrayList<Renderer.RenderCommand> hit() 
        {
            this.d_HP--;
            getHitJustNow = true;
            ArrayList<Renderer.RenderCommand> commands = new ArrayList<>();
            commands.add(new Renderer.RenderCommand(xPos-2, yPos-1, design[0], Color.RED));
            commands.add(new Renderer.RenderCommand(xPos-1, yPos, design[1], Color.RED));
            commands.add(new Renderer.RenderCommand(xPos, yPos+1, design[2], Color.RED));
            return commands;
        }
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
        private int d_HP;
        //<[-----]>
        //  #####
        //   %%%
        //    V
        private final String[] design = {"<[-----]>", "#####", "%%%", "V"};
        private final String[] designClean = {"         ", "     ", "   ", " "};
        // these 2 values define the shoot timeout
        private final int shootTimeout = 600;
        private long shootTimer = 0;

        public EnemyC(int xPos, int yPos, int xMax, int yMax)
        {
            super(4, 2);
            this.isEnemy = true;
            this.xPos = xPos; this.yPos = yPos;
            this.xMax = xMax; this.yMax = yMax;
            d_HP = 10;
            shootTimer = System.currentTimeMillis();
            getHitJustNow = false;
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
            Color color = Color.WHITE;
            if(dir == MoveDirection.DIR_NONE && getHitJustNow)
            {
                getHitJustNow = false;
                color = Color.RED;
            }
            commands.add(new Renderer.RenderCommand(xPos-4, yPos-1, design[0], color));
            commands.add(new Renderer.RenderCommand(xPos-2, yPos, design[1], color));
            commands.add(new Renderer.RenderCommand(xPos-1, yPos+1, design[2], color));
            commands.add(new Renderer.RenderCommand(xPos, yPos+2, design[3], color));
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
        public ArrayList<Bullet> shoot() 
        {
            ArrayList<Bullet> newBullets = new ArrayList<>();
            if(System.currentTimeMillis() - shootTimer > shootTimeout)
            {
                // enemy C can shoot two bullets a time
                newBullets.add(new Bullet(xPos-2, yPos + 1, MoveDirection.DIR_DOWN, true));
                newBullets.add(new Bullet(xPos+2, yPos + 1, MoveDirection.DIR_DOWN, true));
                shootTimer = System.currentTimeMillis();
            }
            return newBullets;
        }

        @Override
        public ArrayList<Renderer.RenderCommand> hit() 
        {
            this.d_HP--;
            getHitJustNow = true;
            ArrayList<Renderer.RenderCommand> commands = new ArrayList<>();
            commands.add(new Renderer.RenderCommand(xPos-4, yPos-1, design[0], Color.RED));
            commands.add(new Renderer.RenderCommand(xPos-2, yPos, design[1], Color.RED));
            commands.add(new Renderer.RenderCommand(xPos-1, yPos+1, design[2], Color.RED));
            commands.add(new Renderer.RenderCommand(xPos, yPos+2, design[3], Color.RED));
            return commands;
        }
        @Override
        public boolean isAlive() {return this.d_HP > 0;}
    }

    /**
     * The background manager
     */
    public static class Background
    {
        private final String design = "'"; // defines the shape of each meteorite
        private final int sparsity = 5; // in range (0, 1000), only defines horizontal sparsity
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
            for(int i = 0; i <= this.yMax; i++)
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
            for(int i = 0; i <= xMax; i++)
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
        private final long finalPauseTimeout = 1000;
        private final long normalPauseTimeout = 10;

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
                    System.out.println("Thread sleep interrupted");
                    Thread.currentThread().interrupt();
                }
            }
            tPrev = System.currentTimeMillis();
        }

        /**
         * Normal pause
         */
        public void pause()
        {
            try
            {
                Thread.sleep(normalPauseTimeout);
            }catch(InterruptedException e)
            {
                System.out.println("Thread sleep interrupted");
                Thread.currentThread().interrupt();
            }
        }

        /**
         * Final pause after game over
         */
        public void finalPause()
        {
            try
            {
                Thread.sleep(finalPauseTimeout);
            }catch(InterruptedException e)
            {
                System.out.println("Thread sleep interrupted");
                Thread.currentThread().interrupt();
            }
        }
    }
}