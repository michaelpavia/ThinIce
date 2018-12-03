import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ThinIce extends JFrame {

    JPanel board;
    KeyController c;
    ButtonClicked b;
    ComClicked comc;
    JPanel [] tiles;
    JPanel controlPanel;
    JPanel commandList;
    JPanel commands;
    JButton up;
    JButton down;
    JButton left;
    JButton right;
    JButton run;
    JButton [] commandButtons;
    int [] commandCache;
    int comCount;
    int pos;
    int prevPos;
    int curLevel;
    boolean levelOver;

    public ThinIce() {

        board = new JPanel(new GridLayout(5, 5));
        board.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        c = new KeyController();
        b = new ButtonClicked();
        comc = new ComClicked();
        tiles = new JPanel[25];
        controlPanel = new JPanel(new BorderLayout());
        commandList = new JPanel(new GridLayout(1, 100));
        commands = new JPanel(new GridLayout(1, 5));
        up = new JButton("Up");
        down = new JButton("Down");
        left = new JButton("Left");
        right = new JButton("Right");
        run = new JButton("Run");
        commandButtons = new JButton[100];
        commandCache = new int[100];
        comCount = 0;
        pos = 1;
        prevPos = 1;
        curLevel = 1;
        levelOver = false;

        loadLevel("src//1.txt");

        commandList.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        /*for(int i = 0; i < commandButtons.length; i++) {
            commandButtons[i] = new JButton("");
            commandButtons[i].addActionListener(comc);
            commandList.add(commandButtons[i]);
        }*/

        up.addActionListener(b);
        down.addActionListener(b);
        left.addActionListener(b);
        right.addActionListener(b);
        run.addActionListener(b);
        commands.add(up);
        commands.add(down);
        commands.add(left);
        commands.add(right);
        commands.add(run);
        controlPanel.add(commands, BorderLayout.SOUTH);
        controlPanel.add(commandList, BorderLayout.NORTH);

        tiles[0].setBackground(Color.GREEN);
        setLayout(new BorderLayout());
        add(board, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
        addKeyListener(c);
        setFocusable(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700, 700);
        setVisible(true);
    }

    private class KeyController implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
            switch(e.getKeyCode()) {
                case KeyEvent.VK_UP:move(0); break;
                case KeyEvent.VK_DOWN:move(1); break;
                case KeyEvent.VK_LEFT:move(2); break;
                case KeyEvent.VK_RIGHT:move(3); break;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {

        }
    }

    private class ButtonClicked implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if(comCount < 100) {
                switch (((JButton) e.getSource()).getText()) {
                    case "Up":
                        addCommand("Up", 0);
                        break;
                    case "Down":
                        addCommand("Down", 1);
                        break;
                    case "Left":
                        addCommand("Left", 2);
                        break;
                    case "Right":
                        addCommand("Right", 3);
                        break;
                    case "Run":
                        runCommands();
                        break;
                }
            }
        }
    }

    private class ComClicked implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JButton b = (JButton)e.getSource();
            if(!b.getText().equals("")) {
                b.setText("");
                int i;
                for (i = 1; i < comCount; i++) {
                    if (commandButtons[i - 1].getText().equals("")) {
                        commandButtons[i - 1].setText(commandButtons[i].getText());
                        commandButtons[i].setText("");
                    }
                }
                commandButtons[i] = null;
                commandList.remove(i - 1);
                comCount--;
            }
        }
    }

    public void runCommands() {
        Thread t = new Thread() {
            public void run() {
                for (int i = 0; i < comCount && !levelOver; i++) {
                    move(commandCache[i]);
                    try {
                        TimeUnit.MILLISECONDS.sleep(200);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ThinIce.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
        t.start();
    }

    public void addCommand(String comText, int com) {
        //commandButtons[comCount].setText(comText);
        commandButtons[comCount] = new JButton(comText);
        commandButtons[comCount].addActionListener(comc);
        commandList.add(commandButtons[comCount]);
        commandCache[comCount] = com;
        comCount++;
        board.revalidate();
        board.repaint();
    }

    public void move(int dir) {

        prevPos = pos;
        if(dir == 0 && pos >= (int)Math.sqrt(tiles.length)) { //Up
            pos -= (int)Math.sqrt(tiles.length);
            moveCheck();
        } else if (dir == 1 && pos <= (tiles.length - (int)Math.sqrt(tiles.length))) { //Down
            pos += (int)Math.sqrt(tiles.length);
            moveCheck();
        } else if (dir == 2 && pos != 1 && pos % (int)Math.sqrt(tiles.length) != 0) { //Left
            pos--;
            moveCheck();
        } else if (dir == 3 && pos % (int)Math.sqrt(tiles.length) != 0) { //Right
            pos++;
            moveCheck();
        }
    }

    public void moveCheck() {
        System.out.println(pos + " " + prevPos);
        System.out.println(pos + " " + (tiles.length - (int)Math.sqrt(tiles.length)));
        if(!tiles[pos - 1].getBackground().equals(Color.decode("#0099ff"))) {
            tiles[prevPos - 1].setBackground(Color.decode("#0099ff"));
            System.out.println(tiles[pos - 1].getBackground());
            if(tiles[pos - 1].getBackground().equals(Color.RED)) {
                tiles[pos - 1].setBackground(Color.decode("#0099ff"));
                boolean complete = true;
                for(int i = 0; i < tiles.length && complete; i++) {
                    if(!tiles[i].getBackground().equals(Color.decode("#0099ff"))) {
                        complete = false;
                    }
                }
                if(complete) {
                    nextLevel();
                }

            } else {
                tiles[pos - 1].setBackground(Color.GREEN);
            }
        }
        else {
            pos = prevPos;
            if(!(checkMove(pos - 1) && checkMove(pos + 1) && checkMove(pos - (int)Math.sqrt(tiles.length)) && checkMove(pos + (int)Math.sqrt(tiles.length)))) {
                loadLevel("src//" + curLevel + ".txt");
            }
        }

    }

    public void nextLevel() {
        curLevel++;
        loadLevel("src//" + curLevel + ".txt");
    }

    public boolean checkMove(int p) {
        if(p < 1 || p > 25) {
            return false;
        } else if(!tiles[p - 1].getBackground().equals(Color.decode("#0099ff"))) {
            return false;
        }
        return true;
    }

    public void parseLevel(int size, int [] start, int [] end) {
        levelOver = true;
        board.removeAll();
        board.setLayout(new GridLayout((int)Math.sqrt(size), (int)Math.sqrt(size)));
        tiles = new JPanel[size];
        for (int i = 0; i < size; i++){
            JPanel panel = new JPanel();
            panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            tiles[i] = panel;
            tiles[i].setBackground(new Color(139, 205, 249));
            board.add(panel);
        }
        tiles[(start[1] * (int)Math.sqrt(size)) + start[0]].setBackground(Color.GREEN);
        pos = (start[1] * (int)Math.sqrt(size)) + start[0] + 1;
        prevPos = (start[1] * (int)Math.sqrt(size)) + start[0] + 1;
        tiles[(end[1] * (int)Math.sqrt(size)) + end[0]].setBackground(Color.RED);
        resetValues();
        board.revalidate();
        board.repaint();
    }

    public void loadLevel(String path) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(path)));
            String line = br.readLine();
            String [] t = line.split("\\s+");
            int size = Integer.parseInt(t[0]);
            int [] start = new int [] {Integer.parseInt(t[1]), Integer.parseInt(t[2])};
            int [] end = new int [] {Integer.parseInt(t[3]), Integer.parseInt(t[4])};
            parseLevel(size, start, end);
            line = br.readLine();
            while(!line.equals("END")) {
                t = line.split("\\s+");
                int x = Integer.parseInt(t[0]);
                int y = Integer.parseInt(t[1]);
                Color col = new Color(Integer.parseInt(t[2]), Integer.parseInt(t[3]), Integer.parseInt(t[4]));
                line = br.readLine();
                tiles[(x * (int)Math.sqrt(size)) + y].setBackground(col);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resetValues() {
        commandCache = new int[100];
        comCount = 0;
        for(int i = 0; i < commandButtons.length; i++) {
            //commandButtons[i].setText("");
            commandButtons[i] = null;
        }
        commandList.removeAll();
        levelOver = false;
    }

    public static void main(String[]args) {
        new ThinIce();
    }
}
