import java.awt.Color;
import java.awt.Graphics;

import com.gvaneyck.ggengine.gui.Screen

class TicTacToeView extends Screen {
    def gs
    
    public void doGraphics(Graphics g) {
        g.setColor(Color.WHITE)
        g.drawLine((int)(width / 3), 0, (int)(width / 3), height)
        g.drawLine((int)(2 * width / 3), 0, (int)(2 * width / 3), height)
        g.drawLine(0, (int)(height / 3), width, (int)(height / 3))
        g.drawLine(0, (int)(2 * height / 3), width, (int)(2 * height / 3))
        
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int x = width / 6 + i * width / 3
                int y = height / 6 + j * height / 3
                if (gs.board[i][j] == 1) {
                    int ex = height / 6 - 5
                    g.drawLine(x + ex, y + ex, x - ex, y - ex)
                    g.drawLine(x - ex, y + ex, x + ex, y - ex)
                }
                else if (gs.board[i][j] == 2) {
                    int w = width / 3 - 10
                    int h = height / 3 - 10
                    g.drawOval((int)(x - w/2), (int)(y - h/2), w, h)
                }
            }
        }
    }
}
