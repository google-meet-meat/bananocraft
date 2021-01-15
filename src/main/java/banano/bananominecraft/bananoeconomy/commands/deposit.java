package banano.bananominecraft.bananoeconomy.commands;

import banano.bananominecraft.bananoeconomy.DB;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class deposit implements CommandExecutor {

	private final JavaPlugin plugin;

	public deposit(final JavaPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		new BukkitRunnable() {

			@Override
			public void run() {

				if (sender instanceof Player) {

					Player player = (Player) sender;
					if (DB.isFrozen(player)) {
						player.sendMessage("Your account has been frozen");
						return;
					}
					String playerWallet = DB.getWallet(player);

					String walletURL = "https://creeper.banano.cc/explorer/account/" + playerWallet;
					/*player.spigot()
							.sendMessage((new ComponentBuilder("Deposit BAN to this address: ")
									.color(net.md_5.bungee.api.ChatColor.YELLOW).append(playerWallet)
									.color(net.md_5.bungee.api.ChatColor.WHITE).bold(true).create()));*/
					TextComponent addrlink = new TextComponent("Click me to view your deposit address");
					addrlink.setColor(ChatColor.YELLOW);
					addrlink.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, walletURL));
					//addrlink.setUnderlined(false);
					player.spigot().sendMessage(addrlink);
				} else {
					System.out.println("You need to be a player");
				}
			}
		}.runTaskAsynchronously(plugin);

		return true;
	}
}
