// This class controls launch window and rendering part

import java.util.ArrayList;
import java.util.LinkedList;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import javax.swing.JPanel;

import javax.swing.JFrame;

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
    //                           UP     DOWN   LEFT   RIGHT  SHOOT (space)
    private boolean[] control = {false, false, false, false, false};
    private int score = 0;

    // game objects
    GameObject.FPSController objFPSController;
    GameObject.Background objBackground;
    GameObject.MyShip objMyShip;
    LinkedList<GameObject.SpaceShip> objEnemies;
    LinkedList<GameObject.Bullet> objBullets;

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
        objFPSController = new GameObject.FPSController(fps);
        objBackground = new GameObject.Background(maxPosX, maxPosY);
        objMyShip = new GameObject.MyShip(level, maxPosX / 2, maxPosY - 1, maxPosX, maxPosY);
        objEnemies = new LinkedList<>();
        objBullets = new LinkedList<>();
        objEnemies.add(new GameObject.EnemyA(maxPosX/2-10, 0, maxPosX, maxPosY)); // for test
        objEnemies.add(new GameObject.EnemyB(maxPosX/2+10, 1, maxPosX, maxPosY)); // for test
        objEnemies.add(new GameObject.EnemyC(maxPosX/2, 1, maxPosX, maxPosY)); // for test
    }

    // main loop for the game
    public void loop()
    {
        boolean frame = false; // use this variable to slow down drawing
        while(!gameExit)
        {
            objFPSController.update();
            render(frame);
            frame = !frame;
            myPanel.repaint(); // refresh the frame to update content
        }
    }

    // collect render commands
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

    private void processLogic()
    {
        ArrayList<RenderCommand> commands = new ArrayList<>();
        if(control[0]) commands.addAll(objMyShip.update(GameObject.MoveDirection.DIR_UP));
        else if(control[1]) commands.addAll(objMyShip.update(GameObject.MoveDirection.DIR_DOWN));
        else if(control[2]) commands.addAll(objMyShip.update(GameObject.MoveDirection.DIR_LEFT));
        else if(control[3]) commands.addAll(objMyShip.update(GameObject.MoveDirection.DIR_RIGHT));
        else commands.addAll(objMyShip.update(GameObject.MoveDirection.DIR_NONE));

        myPanel.addCommand(commands);
    }

    // send close window event
    public int close()
    {
        myFrame.dispatchEvent(new WindowEvent(myFrame, WindowEvent.WINDOW_CLOSING));
        return score;
    }

    // detect for keyboard inputs
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
    // this function won't be used
    @Override
    public void keyTyped(KeyEvent e) {}

    // this object controls the actual drawing on JFrame
    public class MyPanel extends JPanel
    {
        static final long serialVersionUID = 1234L;
        static final int fontSize = 12;

        private ArrayList<RenderCommand> commands;
        private Font myFont = new Font(Font.MONOSPACED, Font.PLAIN, fontSize);
        public int chrHeight = 0;
        public int chrWidth = 0;

        public MyPanel()
        {
            commands = new ArrayList<>();
            // set font properties
            FontMetrics metrics = getFontMetrics(myFont);
            chrHeight = metrics.getMaxAscent();
            chrWidth = metrics.charWidth(' ');
        }
        
        @Override
        public void paint(Graphics g)
        {
            g.setFont(myFont);
            for(RenderCommand command : commands)
            {
                // fill background with black
                g.setColor(Color.BLACK);
                g.fillRect(command.getX()*chrWidth, command.getY()*chrHeight, command.getData().length()*chrWidth, chrHeight);
                // draw actual data
                g.setColor(Color.WHITE);
                g.drawString(command.getData(), command.getX()*chrWidth, (command.getY()+1)*chrHeight-5);
            }
            // clear drawing commands
            commands.clear();
        }

        // add new command for drawing
        public void addCommand(int posX, int posY, String data)
        {
            commands.add(new RenderCommand(posX, posY, data));
        }
        public void addCommand(RenderCommand cmd)
        {
            commands.add(cmd);
        }
        public void addCommand(ArrayList<RenderCommand> cmds)
        {
            commands.addAll(cmds);
        }
    }

    // this object stores a single render command
    // the command is sent to myPanel for rendering
    public static class RenderCommand
    {
        private int posX;
        private int posY;
        private String data;
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
        public int getX(){return posX;}
        public int getY(){return posY;}
        public String getData(){return data;}
        public void setX(int m){this.posX = m;}
        public void setY(int m){this.posY = m;}
        public void setData(String data){this.data = data;}
    }
}