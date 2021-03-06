package banano.bananominecraft.bananoeconomy.commands;

import banano.bananominecraft.bananoeconomy.DB;
import banano.bananominecraft.bananoeconomy.RPC;
import banano.bananominecraft.bananoeconomy.exceptions.TransactionError;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Tip implements CommandExecutor {
    private final JavaPlugin plugin;

    public Tip(final JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        final Player player = (Player) sender;

        // Did they call things correctly?
        if (args.length != 2) {
            player.sendMessage("You need to enter an amount to send and a player to send to");
            player.sendMessage("/tip [amount] [playername]");
            return true;
        }

        // Implementation Note:
        // Because this parses to `double` instead of say an infinite precision `BigDouble` we can
        //  round differently
        final String sAmount = args[0];
        final double amount;
        try {
            amount = Double.parseDouble(sAmount);
            if (amount <= 0) {
                player.sendMessage(String.format("Amount ('%s') has to be greater than 0", sAmount));
                return false;
            }
        } catch (final Exception e) {
            sender.sendMessage(String.format("Amount ('%s') is not a number greater than 0", sAmount));
            return false;
        }

        final String targetPlayerName = args[1];
        Player target = Bukkit.getPlayerExact(targetPlayerName);
        if (target == player) {
            player.sendMessage("You cannot tip yourself");
            return false;
        }
        target = null;
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (DB.isFrozen(player)) {
                player.sendMessage("Your account is frozen");
                return;
            }

            if (DB.isFrozen(targetPlayerName)) {
                player.sendMessage(targetPlayerName + "'s account is frozen");
                return;
            }

            player.sendMessage("Tipping " + targetPlayerName + " with " + amount + " BAN.");
            final String sWallet = DB.getWallet(player);
            final String tWallet = DB.getWallet(targetPlayerName);
            final String blockHash;
            try {
                blockHash = RPC.sendTransaction(sWallet, tWallet, amount);
            } catch (final TransactionError error) {
                player.sendMessage(String.format("Tip of %s to %s failed with: %s", sAmount, targetPlayerName, error.getUserError()));
                return;
            }

            final String blockURL = "https://creeper.banano.cc/explorer/block/" + blockHash;
            final TextComponent blocklink = new TextComponent("Click me to view the transaction in the block explorer");
            blocklink.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, blockURL));

            final String amountstr = Double.toString(amount);
            //player.spigot().sendMessage((new ComponentBuilder("You have sent ").color(ChatColor.YELLOW).append(amountstr).color(ChatColor.WHITE).bold(true).append(" to ").color(ChatColor.YELLOW)
                    //.append(target.getDisplayName()).color(ChatColor.WHITE).bold(true).append(" with block ID : ").append(blockHash).color(ChatColor.YELLOW).bold(true).create()));
            player.spigot().sendMessage(blocklink);

            /*target.spigot().sendMessage((new ComponentBuilder("You have received ").color(ChatColor.YELLOW).append(amountstr).color(ChatColor.WHITE).bold(true).append(" from ").color(ChatColor.YELLOW)
                    .append(player.getDisplayName()).color(ChatColor.WHITE).bold(true).append(" with block ID : ").append(blockHash).color(ChatColor.YELLOW).bold(true).create()));
            target.spigot().sendMessage(blocklink);*/
        });

        return false;
    }
}
