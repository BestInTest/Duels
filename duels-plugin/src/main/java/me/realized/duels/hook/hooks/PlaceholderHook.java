package me.realized.duels.hook.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.queue.DQueue;
import me.realized.duels.api.queue.DQueueManager;
import me.realized.duels.data.UserData;
import me.realized.duels.data.UserManagerImpl;
import me.realized.duels.queue.Queue;
import me.realized.duels.queue.QueueManager;
import me.realized.duels.queue.sign.QueueSignImpl;
import me.realized.duels.queue.sign.QueueSignManagerImpl;
import me.realized.duels.util.hook.PluginHook;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.stream.Collectors;

public class PlaceholderHook extends PluginHook<DuelsPlugin> {

    public static final String NAME = "PlaceholderAPI";

    private final UserManagerImpl userDataManager;

    public PlaceholderHook(final DuelsPlugin plugin) {
        super(plugin, NAME);
        this.userDataManager = plugin.getUserManager();
        new Placeholders().register();
    }

    public class Placeholders extends PlaceholderExpansion {

        @Override
        public String getIdentifier() {
            return "duels";
        }

        @Override
        public String getAuthor() {
            return "Realized";
        }

        @Override
        public String getVersion() {
            return "1.0";
        }

        @Override
        public String onPlaceholderRequest(final Player player, final String identifier) {
            if (player == null) {
                return "Player is required";
            }

            final UserData user = userDataManager.get(player);

            if (user == null) {
                return null;
            }

            /*
            switch (identifier) {
                case "wins":
                    return String.valueOf(user.getWins());
                case "losses":
                    return String.valueOf(user.getLosses());
                case "can_request":
                    return String.valueOf(user.canRequest());
            }
            */
            if (identifier.equalsIgnoreCase("wins")) {
                return String.valueOf(user.getWins());
            } else if (identifier.equalsIgnoreCase("losses")) {
                return String.valueOf(user.getLosses());
            } else if (identifier.equalsIgnoreCase("can_request")) {
                return String.valueOf(user.canRequest());
            } else if (identifier.contains("arena_")) { //arena_world_X_Y_Z
                String[] loc = identifier.split("_");
                //loc[0] = arena_
                World world = Bukkit.getWorld(loc[1]);
                double x = Double.parseDouble(loc[2]);
                double y = Double.parseDouble(loc[3]);
                double z = Double.parseDouble(loc[4]);
                Location signLocation = new Location(world, x, y, z);
                QueueSignManagerImpl qsm = (QueueSignManagerImpl) plugin.find("QueueSignManagerImpl");
                Queue queue = qsm.get(signLocation).getQueue();
                return queue.getPlayersInMatch() + " / " + queue.getQueuedPlayers().size();
            }

            return null;
        }
    }
}
