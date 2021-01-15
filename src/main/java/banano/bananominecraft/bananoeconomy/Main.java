package banano.bananominecraft.bananoeconomy;

import banano.bananominecraft.bananoeconomy.commands.*;
import banano.bananominecraft.bananoeconomy.events.OnArenaPreparingStart;
import banano.bananominecraft.bananoeconomy.events.OnJoin;

import com.mongodb.client.model.Indexes;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

public final class Main extends JavaPlugin implements Listener {
    private static Main instance;
    public static Economy economy = null;
    public static Logger log = Bukkit.getLogger();
    private Plugin plugin;
    private HashMap<UUID, PermissionAttachment> playerPermissions;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        System.out.println("STARTED");
        getServer().getPluginManager().registerEvents(new OnJoin(), this);
        if (getServer().getPluginManager().getPlugin("MBedwars") != null) {
            getServer().getPluginManager().registerEvents(new OnArenaPreparingStart(), this);
        }

        getCommand("deposit").setExecutor(new deposit(this));
        getCommand("nodeinfo").setExecutor(new NodeInfo(this));
        getCommand("tip").setExecutor(new Tip(this));
        getCommand("withdraw").setExecutor(new Withdraw(this));
        getCommand("balance").setExecutor(new Balance(this));
        getCommand("freeze").setExecutor(new Freeze());
        getCommand("unfreeze").setExecutor(new UnFreeze());
        getCommand("donate").setExecutor(new Donate(this));
        getCommand("3ne9pcgmg3iuy").setExecutor(new RewardPlayer());

        System.out.println("registered commands");
        setupEconomy();
        System.out.println("economy setup");
        setupWallet();
        System.out.println("wallet setup");
        DB.usersCollection.createIndex(Indexes.hashed("name"));
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return super.onCommand(sender, command, label, args);
    }

    public void onDisable() {
        DB.getMongoClient().close();
    }

    private boolean setupEconomy()
    {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") != null) {
            Bukkit.getServer().getServicesManager().register(Economy.class, new VaultConnector(), this, ServicePriority.Highest);
        }
        System.out.println("return economy setup");
        return (economy != null);
    }

    private void setupWallet() {
        if(!RPC.wallet_exists(RPC.getWalletID())){
            System.out.println("MASTER WALLET DOES NOT EXIST -SETTING UP WALLET");
            RPC.walletCreate();
            String masterWallet = RPC.accountCreate(0);
            System.out.println("MASTER WALLET: " + masterWallet);
            Main.getPlugin(Main.class).getConfig().set("masterWallet", masterWallet);
            Main.getPlugin(Main.class).saveConfig();

        }else {
            System.out.println("Master wallet exists.");
        }
        String gmCfg = RPC.getGameWallet();
        if (gmCfg == null || gmCfg.equals("Account creation failed")) {
        	String game1Wallet = RPC.accountCreate(1223372035);
        	System.out.println("GAME 1 WALLET: " + game1Wallet);
        	Main.getPlugin(Main.class).getConfig().set("game1Wallet", game1Wallet);
        	Main.getPlugin(Main.class).saveConfig();
        }
    }
}
