package VoidPoro

import com.gvaneyck.ggengine.Action
import com.gvaneyck.ggengine.Game
import com.gvaneyck.ggengine.GameManager

class VoidPoroGame extends Game {
    static GameManager gm
    static gs

    def champions = [ 'Ahri', 'Master Yi', 'Caitlyn', 'Nautilus' ]

    public void init() {
        gs.player = [:]
        (1..gs.players).each { i ->
            gs.player[i] = [:]
            champions.each { champ ->
                gm.addAction(new Action(i, this, 'initPlayer', [ i, champ ]))
            }
        }

        gm.resolveActions()
    }

    public void initPlayer(int i, String champion) {

    }

    public void turn() {
    }

    public boolean isFinished() {
        return false
    }

    public Map end() {
        return [:]
    }
}
