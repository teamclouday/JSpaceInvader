import java.util.Random;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import javax.swing.JPanel;
import javax.swing.JFrame;

/**
 * This is the class to manage rendering and game objects.
 * Also handle keyboard events
 * @see java.awt.event.KeyListener
 * @see javax.swing.JFrame
 */
public class Renderer implements KeyListener
{
    // variables related to the window
    private JFrame myFrame = null;
    private MyPanel myPanel = null;
    private final int frameWidth = 600;
    private final int frameHeight = 600;
    
    // game status and properties
    private boolean gameExit = false;
    private int maxPosX, maxPosY;
    private final int fps = 60;
    //                           UP     DOWN   LEFT   RIGHT  SHOOT
    private boolean[] control = {false, false, false, false, false};
    private int score = 0; // how many enemies defeated
    private int scoreRound = -1; // how many rounds survived (starting from -1)
    
    // game objects
    GameObject.FPSController objFPSController;
    GameObject.Background objBackground;
    GameObject.MyShip objMyShip;
    LinkedList<GameObject.SpaceShip> objEnemies;
    LinkedList<GameObject.Bullet> objBullets;
    private final int maxEnemiesSpawned = 5; // how many enemies can be spawned at most
    private final int minEnemiesSpawned = 1; // how many enemies can be spawned at least
    private Random enemyRand;

    public Renderer(int level)
    {
        // initalize JPanel
        myPanel = new MyPanel();
        myPanel.setPreferredSize(new Dimension(frameWidth, frameHeight));
        maxPosX = frameWidth / myPanel.chrWidth - 1;
        maxPosY = frameHeight / myPanel.chrHeight - 1;
        // initialize new JFrame
        myFrame = new JFrame("Space Invader");
        myFrame.add(myPanel);
        myFrame.setResizable(false);
        myFrame.addKeyListener(this);
        myFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        myFrame.pack();
        myFrame.setLocationRelativeTo(null);
        myFrame.setVisible(true);
        myFrame.setBackground(Color.BLACK);
        myFrame.setAlwaysOnTop(true);
        // initialize game objects
        enemyRand = new Random(System.currentTimeMillis());
        objFPSController = new GameObject.FPSController(fps);
        objBackground = new GameObject.Background(maxPosX, maxPosY);
        objMyShip = new GameObject.MyShip(level, maxPosX / 2, maxPosY - 1, maxPosX, maxPosY);
        objEnemies = new LinkedList<>();
        objBullets = new LinkedList<>();
    }

    /**
     * The main game loop
     */
    public void loop()
    {
        boolean frame = false; // use this variable to slow down drawing
        while(!gameExit)
        {
            if(objMyShip == null)
            {
                // this means game over
                objFPSController.finalPause();
                break;
            }
            objFPSController.update();
            render(frame);
            renderUI();
            frame = !frame;
            myPanel.repaint(); // refresh the frame to update content
        }
    }

    /**
     * Create render commands to render UI
     */
    private void renderUI()
    {
        String myHP    = "HP    = " + objMyShip.d_HP;
        String myScore = "Score = " + score;
        String myRound = "Round = " + scoreRound;
        myPanel.addCommand(new RenderCommand(1, 0, myHP));
        myPanel.addCommand(new RenderCommand(1, 1, myScore));
        myPanel.addCommand(new RenderCommand(1, 2, myRound));
    }

    /**
     * Collect render command and send to JPanel
     * @param frame
     */
    private void render(boolean frame)
    {
        ArrayList<RenderCommand> commands = new ArrayList<>();
        commands.addAll(objBackground.update());
        if(frame)
        {
            processLogic();
            for(GameObject.Bullet bullet : objBullets)
                commands.addAll(bullet.update(frame));
        }
        else
        {
            commands.addAll(objMyShip.update(GameObject.MoveDirection.DIR_NONE));
            for(GameObject.SpaceShip ship : objEnemies)
                commands.addAll(ship.update(GameObject.MoveDirection.DIR_NONE));
            for(GameObject.Bullet bullet : objBullets)
                commands.addAll(bullet.update(frame));
        }
        myPanel.addCommand(commands);
    }

    /**
     * Process internal game logic (body movements, bullet hit, random spawned enemies)
     */
    private void processLogic()
    {
        ArrayList<RenderCommand> commands = new ArrayList<>();
        // update my ship direction based on control input
        if(control[0]) commands.addAll(objMyShip.update(GameObject.MoveDirection.DIR_UP));
        else if(control[1]) commands.addAll(objMyShip.update(GameObject.MoveDirection.DIR_DOWN));
        else if(control[2]) commands.addAll(objMyShip.update(GameObject.MoveDirection.DIR_LEFT));
        else if(control[3]) commands.addAll(objMyShip.update(GameObject.MoveDirection.DIR_RIGHT));
        else commands.addAll(objMyShip.update(GameObject.MoveDirection.DIR_NONE));
        if(control[4]) objBullets.addAll(objMyShip.shoot());
        // process enemies
        if(objEnemies.size() <= 0)
        {
            // randomly spawn enemies if num enemy is 0
            scoreRound++;
            int num = enemyRand.nextInt((maxEnemiesSpawned - minEnemiesSpawned) + 1) + minEnemiesSpawned;
            for(int i = 0; i < num; i++)
            {
                int enemyType = enemyRand.nextInt(1000); // [0, 50) - Enemy C, [50, 400) - Enemy B, [400, 1000) - Enemy A
                if(enemyType < 50)
                {
                    // set offsets of Ship C
                    int offsetX = 4; int offsetY = 2;
                    int maxTry = 10;
                    while(maxTry > 0)
                    {
                        int posX = enemyRand.nextInt((maxPosX-8)+1) + 4; // [4, maxPosX - 4]
                        int posY = enemyRand.nextInt((maxPosY/2-1)+1) + 1; // [1, maxPosY / 2]
                        boolean goodPos = true;
                        for(GameObject.SpaceShip ship : objEnemies)
                        {
                            if(Math.abs(ship.xPos - posX) < (ship.offsetX + offsetX + 1) && Math.abs(ship.yPos - posY) < (ship.offsetY + offsetY + 1))
                            {
                                // overlap detected
                                goodPos = false;
                                break;
                            }
                        }
                        if(goodPos)
                        {
                            objEnemies.add(new GameObject.EnemyC(posX, posY, maxPosX, maxPosY));
                            break;
                        }
                        maxTry--;
                    }
                }
                else if(enemyType < 400)
                {
                    // set offsets of Ship B
                    int offsetX = 2; int offsetY = 1;
                    int maxTry = 10;
                    while(maxTry > 0)
                    {
                        int posX = enemyRand.nextInt((maxPosX-8)+1) + 4; // [4, maxPosX - 4]
                        int posY = enemyRand.nextInt((maxPosY/2-1)+1) + 1; // [1, maxPosY / 2]
                        boolean goodPos = true;
                        for(GameObject.SpaceShip ship : objEnemies)
                        {
                            if(Math.abs(ship.xPos - posX) < (ship.offsetX + offsetX + 1) && Math.abs(ship.yPos - posY) < (ship.offsetY + offsetY + 1))
                            {
                                // overlap detected
                                goodPos = false;
                                break;
                            }
                        }
                        if(goodPos)
                        {
                            objEnemies.add(new GameObject.EnemyB(posX, posY, maxPosX, maxPosY));
                            break;
                        }
                        maxTry--;
                    }
                }
                else
                {
                    // set offsets of Ship A
                    int offsetX = 1; int offsetY = 0;
                    int maxTry = 10;
                    while(maxTry > 0)
                    {
                        int posX = enemyRand.nextInt((maxPosX-8)+1) + 4; // [4, maxPosX - 4]
                        int posY = enemyRand.nextInt((maxPosY/2-1)+1) + 1; // [1, maxPosY / 2]
                        boolean goodPos = true;
                        for(GameObject.SpaceShip ship : objEnemies)
                        {
                            if(Math.abs(ship.xPos - posX) < (ship.offsetX + offsetX + 1) && Math.abs(ship.yPos - posY) < (ship.offsetY + offsetY + 1))
                            {
                                // overlap detected
                                goodPos = false;
                                break;
                            }
                        }
                        if(goodPos)
                        {
                            objEnemies.add(new GameObject.EnemyA(posX, posY, maxPosX, maxPosY));
                            break;
                        }
                        maxTry--;
                    }
                }
            }
        }
        ListIterator<GameObject.SpaceShip> enemyIter = objEnemies.listIterator();
        while(enemyIter.hasNext())
        {
            // randomly move on Y axis
            // move following my ship on X axis
            GameObject.SpaceShip ship = enemyIter.next();
            // check if alive
            if(!ship.isAlive())
            {
                score++;
                commands.addAll(ship.explode());
                enemyIter.remove(); // remove dead ship
                continue;
            }
            int horiOrVert = enemyRand.nextInt(2);
            GameObject.MoveDirection finalChoice = GameObject.MoveDirection.DIR_NONE;
            if(horiOrVert == 0)
            {
                // move horizontally
                int desiredDir = (objMyShip.xPos >= ship.xPos) ? 1 : -1; // move towards my ship
                // check validity
                boolean validDir = true;
                boolean validOppositeDir = true;
                for(GameObject.SpaceShip otherShip : objEnemies)
                {
                    if(otherShip != ship)
                    {
                        if(Math.abs(ship.xPos + desiredDir - otherShip.xPos) < (otherShip.offsetX + ship.offsetX + 1))
                        {
                            // overlap detected
                            validDir = false;
                            break;
                        }
                        if(Math.abs(ship.xPos - desiredDir - otherShip.xPos) < (otherShip.offsetX + ship.offsetX + 1))
                            validOppositeDir = false; // not valid to move opposite way
                    }
                }
                if(validDir)
                    finalChoice = (desiredDir < 0) ? GameObject.MoveDirection.DIR_LEFT : GameObject.MoveDirection.DIR_RIGHT;
                else if(validOppositeDir)
                {
                    // only 1/5 possibility to stay still
                    if(enemyRand.nextInt(5) > 0)
                        finalChoice = (desiredDir > 0) ? GameObject.MoveDirection.DIR_LEFT : GameObject.MoveDirection.DIR_RIGHT;
                }
            }
            else
            {
                // move vertically
                int desiredDir = (enemyRand.nextInt(2) > 0) ? 1 : -1; // 50% possibility
                // check validity
                boolean validDir = true;
                boolean validOppositeDir = true;
                for(GameObject.SpaceShip otherShip : objEnemies)
                {
                    if(otherShip != ship)
                    {
                        if(Math.abs(ship.yPos + desiredDir - otherShip.yPos) < (otherShip.offsetY + ship.offsetY + 1))
                        {
                            // overlap detected
                            validDir = false;
                            break;
                        }
                        if(Math.abs(ship.yPos - desiredDir - otherShip.yPos) < (otherShip.offsetY + ship.offsetY + 1))
                            validOppositeDir = false; // not valid to move opposite way
                    }
                }
                if(validDir)
                    finalChoice = (desiredDir < 0) ? GameObject.MoveDirection.DIR_UP : GameObject.MoveDirection.DIR_DOWN;
                else if(validOppositeDir)
                {
                    // only 1/5 possibility to stay unmoved
                    if(enemyRand.nextInt(5) > 1)
                        finalChoice = (desiredDir > 0) ? GameObject.MoveDirection.DIR_UP : GameObject.MoveDirection.DIR_DOWN;
                }
            }
            commands.addAll(ship.update(finalChoice));
            // randomly trigger shoot
            if(enemyRand.nextInt(10) > 2)
                objBullets.addAll(ship.shoot());
        }
        // process bullets
        ListIterator<GameObject.Bullet> bulletIter = objBullets.listIterator();
        while(bulletIter.hasNext())
        {
            GameObject.Bullet bullet = bulletIter.next();
            // check if bullet is outside of screen
            if(bullet.yPos < 0 || bullet.yPos > maxPosY)
            {
                commands.add(bullet.explode());
                bulletIter.remove();
                continue;
            }
            // process by type
            boolean hit = false;
            if(bullet.isEnemy())
            {
                if(Math.abs(bullet.xPos - objMyShip.xPos) <= (objMyShip.offsetX) && Math.abs(bullet.yPos - objMyShip.yPos) <= (objMyShip.offsetY))
                {
                    commands.addAll(objMyShip.hit());
                    hit = true;
                }
            }
            else
            {
                for(GameObject.SpaceShip ship : objEnemies)
                {
                    if(Math.abs(bullet.xPos - ship.xPos) <= (ship.offsetX) && Math.abs(bullet.yPos - ship.yPos) <= (ship.offsetY))
                    {
                        commands.addAll(ship.hit());
                        hit = true;
                        break;
                    }
                }
            }
            if(hit)
            {
                commands.add(bullet.explode());
                bulletIter.remove();
            }
        }
        // if my ship is not alive, set it to null
        if(!objMyShip.isAlive())
        {
            commands.addAll(objMyShip.explode());
            objMyShip = null;
        }
        myPanel.addCommand(commands);
    }

    /**
     * Send close window event
     * @return String, final summary
     */
    public String close()
    {
        myFrame.dispatchEvent(new WindowEvent(myFrame, WindowEvent.WINDOW_CLOSING));
        String summary = "Your final score = " + score;
        summary += "\nYou have survived " + scoreRound + " rounds";
        return summary;
    }

    @Override
    public void keyPressed(KeyEvent e) 
    {
        switch(e.getKeyCode())
        {
            case KeyEvent.VK_ESCAPE:
                gameExit = true;
                break;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                control[0] = true;
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                control[1] = true;
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                control[2] = true;
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                control[3] = true;
                break;
            case KeyEvent.VK_SPACE:
                control[4] = true;
                break;
            default: break;
        }
    }
    @Override
    public void keyReleased(KeyEvent e)
    {
        switch(e.getKeyCode())
        {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                control[0] = false;
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                control[1] = false;
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                control[2] = false;
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                control[3] = false;
                break;
            case KeyEvent.VK_SPACE:
                control[4] = false;
                break;
            default: break;
        }
    }
    @Override
    public void keyTyped(KeyEvent e) {}

    /**
     * This class handles the real text rendering, by the render commands received.
     * @see javax.swing.JPanel
     */
    public class MyPanel extends JPanel
    {
        static final long serialVersionUID = 1234L;
        static final int fontSize = 12;

        private ArrayList<RenderCommand> commands;
        private Font myFont = new Font(Font.MONOSPACED, Font.PLAIN, fontSize);
        public int chrHeight = 0;
        public int chrWidth = 0;
        private int chrDescent = 0;

        public MyPanel()
        {
            commands = new ArrayList<>();
            // set font properties
            FontMetrics metrics = getFontMetrics(myFont);
            chrHeight = metrics.getHeight();
            chrWidth = metrics.charWidth(' ');
            chrDescent = metrics.getMaxDescent();
        }
        
        @Override
        public void paint(Graphics g)
        {
            g.setFont(myFont);
            for(RenderCommand command : commands)
            {
                // fill background with black
                g.setColor(Color.BLACK);
                g.fillRect(command.getX()*chrWidth, command.getY()*chrHeight + chrDescent, command.getData().length()*chrWidth, chrHeight);
                // draw actual data
                g.setColor(command.getColor());
                g.drawString(command.getData(), command.getX()*chrWidth, (command.getY()+1)*chrHeight);
            }
            // clear drawing commands
            commands.clear();
        }

        /**
         * Add a single command
         * @param posX
         * @param posY
         * @param data
         */
        public void addCommand(int posX, int posY, String data)
        {
            commands.add(new RenderCommand(posX, posY, data));
        }
        /**
         * Add a single command
         * @param cmd
         */
        public void addCommand(RenderCommand cmd)
        {
            commands.add(cmd);
        }
        /**
         * Add a list of commands
         * @param cmds
         */
        public void addCommand(ArrayList<RenderCommand> cmds)
        {
            commands.addAll(cmds);
        }
    }

    /**
     * This class stores a single render command
     */
    public static class RenderCommand
    {
        private int posX;
        private int posY;
        private String data;
        private Color color = Color.WHITE;
        RenderCommand()
        {
            this.posX = 0;
            this.posY = 0;
            this.data = "";
        }
        RenderCommand(int posX, int posY, String data)
        {
            this.posX = posX;
            this.posY = posY;
            this.data = data;
        }
        RenderCommand(int posX, int posY, String data, Color color)
        {
            this.posX = posX;
            this.posY = posY;
            this.data = data;
            this.color = color;
        }
        public int getX(){return posX;}
        public int getY(){return posY;}
        public String getData(){return data;}
        public Color getColor(){return color;}
        public void setX(int m){this.posX = m;}
        public void setY(int m){this.posY = m;}
        public void setData(String data){this.data = data;}
    }
}