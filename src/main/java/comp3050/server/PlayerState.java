package comp3050.server;

import java.util.ArrayList;
import java.util.List;

// Per-player state held by SessionManager.
// One PlayerState exists per active session.
public class PlayerState {
    public int y;
    public int x;
    public char avatar;
    public List<Character> inventory;

    public PlayerState(int y, int x, char avatar) {
        this.y = y;
        this.x = x;
        this.avatar = avatar;
        this.inventory = new ArrayList<>();
    }
}
