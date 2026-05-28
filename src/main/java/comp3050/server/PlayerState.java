package comp3050.server;

import java.util.ArrayList;
import java.util.List;

public class PlayerState {
    public String username;
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