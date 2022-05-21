package me.realized.duels.party;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.Loadable;

public class PartyManagerImpl implements Loadable, Listener {
    
    private static final int INVITE_EXPIRATION = 30;

    private final Map<UUID, Map<UUID, PartyInvite>> invites = new HashMap<>();

    private final Map<UUID, Party> partyMap = new HashMap<>();
    private final List<Party> parties = new ArrayList<>();
    
    public PartyManagerImpl(final DuelsPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void handleLoad() {}

    @Override
    public void handleUnload() {
        invites.clear();
        parties.clear();
        partyMap.clear();
    }
    
    private Map<UUID, PartyInvite> getInvites(final Player player, final boolean create) {
        Map<UUID, PartyInvite> cached = invites.get(player.getUniqueId());

        if (cached == null && create) {
            invites.put(player.getUniqueId(), cached = new HashMap<>());
            return cached;
        }

        return cached;
    }

    public PartyInvite getInvite(final Player sender, final Player target) {
        final Map<UUID, PartyInvite> cached = getInvites(sender, false);

        if (cached == null) {
            return null;
        }

        final PartyInvite invite = cached.get(target.getUniqueId());

        if (invite == null) {
            return null;
        }

        if (System.currentTimeMillis() - invite.getCreation() >= INVITE_EXPIRATION * 1000L) {
            cached.remove(target.getUniqueId());
            return null;
        }

        return invite;
    }

    public boolean hasInvite(final Player sender, final Player target) {
        return getInvite(sender, target) != null;
    }

    public PartyInvite removeInvite(final Player sender, final Player target) {
        final Map<UUID, PartyInvite> cached = getInvites(sender, false);

        if (cached == null) {
            return null;
        }

        final PartyInvite invite = cached.remove(target.getUniqueId());

        if (invite == null) {
            return null;
        }

        if (System.currentTimeMillis() - invite.getCreation() >= INVITE_EXPIRATION * 1000L) {
            cached.remove(target.getUniqueId());
            return null;
        }

        return invite;
    }

    public void sendInvite(final Player sender, final Player target, final Party party) {
        final PartyInvite invite = new PartyInvite(sender, target, party);
        getInvites(sender, true).put(target.getUniqueId(), invite);
    }

    public Party get(final Player player) {
        return partyMap.get(player.getUniqueId());
    }

    public Party getOrCreate(final Player player) {
        Party party = get(player);

        if (party != null) {
            return party;
        }

        party = new Party(player);
        parties.add(party);
        partyMap.put(player.getUniqueId(), party);
        return party;
    }

    public boolean isInParty(final Player player) {
        return get(player) != null;
    }

    public void join(final Player player, final Party party) {
        party.add(player);
        partyMap.put(player.getUniqueId(), party);
    }

    public void remove(final Player player, final Party party) {
        party.remove(player);
        partyMap.remove(player.getUniqueId());
    }

    public void remove(final Party party) {
        party.setRemoved(true);
        party.getMembers().forEach(member -> partyMap.remove(member.getUuid()));
        parties.remove(party);
    }
    
    @EventHandler
    public void on(final PlayerQuitEvent event) {
        invites.remove(event.getPlayer().getUniqueId());
    }
}