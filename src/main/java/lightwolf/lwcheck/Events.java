package lightwolf.lwcheck;

import lightwolf.lwcheck.LwCheck;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Stream;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Events implements Listener {
   private List<String> allowedCommands = Arrays.asList("/list", "/help", "/msg");

   private Player getModeratorForPlayer(Player player) {
      Iterator var2 = LwCheck.checkRequests.entrySet().iterator();

      Entry entry;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         entry = (Entry)var2.next();
      } while(!((Player)entry.getKey()).equals(player));

      return (Player)entry.getValue();
   }

   @EventHandler
   public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
      Player player = event.getPlayer();
      if (LwCheck.checkRequests.containsKey(player)) {
         String command = event.getMessage().split(" ")[0];
         Stream var10000 = this.allowedCommands.stream();
         command.getClass();
         boolean isCommandAllowed = var10000.map(Object::toString).anyMatch(cmd -> ((String) cmd).startsWith(command));
         if (!isCommandAllowed) {
            player.sendMessage(ChatColor.RED + "Вы не можете использовать эту команду во время проверки.");
            event.setCancelled(true);
         }
      }

   }

   @EventHandler
   public void Block(BlockBreakEvent event) {
      Player player = event.getPlayer();
      if (LwCheck.checkRequests.containsKey(player)) {
         player.sendMessage(ChatColor.RED + "Вы не можете ломать блоки во время проверки.");
         event.setCancelled(true);
      }

   }

   @EventHandler
   public void move(PlayerMoveEvent event) {
      Player player = event.getPlayer();
      if (LwCheck.checkRequests.containsKey(player)) {
         event.setCancelled(true);
      }

   }

   @EventHandler
   public void place(BlockPlaceEvent event) {
      Player player = event.getPlayer();
      if (LwCheck.checkRequests.containsKey(player)) {
         player.sendMessage(ChatColor.RED + "Вы не можете ставить блоки во время проверки.");
         event.setCancelled(true);
      }

   }

   @EventHandler
   public void drop(PlayerDropItemEvent event) {
      Player player = event.getPlayer();
      if (LwCheck.checkRequests.containsKey(player)) {
         player.sendMessage(ChatColor.RED + "Вы не можете Выбрасывать предметы во время проверки.");
         event.setCancelled(true);
      }

   }

   @EventHandler
   public void quit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      if (LwCheck.checkRequests.containsKey(player)) {
         Player moderator = this.getModeratorForPlayer(player);
         if (moderator != null) {
            if (!LwCheck.Quit) {
               return;
            }

            player.sendTitle(ChatColor.RED + "", "", 0, 0, 0);
            String banCommand = LwCheck.bancommand + " " + player.getName() + " " + LwCheck.banforQuit + "d " + LwCheck.banQuit;
            moderator.performCommand(banCommand);
            notifyModeratorActions(moderator, player, "Игрок вышел с сервера и был забанен.");
            LwCheck.checkRequests.remove(player);
         }
      }

   }

   @EventHandler
   public void Damage(EntityDamageByEntityEvent event) {
      if (event.getDamager() instanceof Player) {
         Player damager = (Player)event.getDamager();
         if (LwCheck.checkRequests.containsKey(damager)) {
            event.setCancelled(true);
         }
      }

   }

   @EventHandler
   public void ModeratorChat(AsyncPlayerChatEvent event) {
      Player moderator = event.getPlayer();
      if (LwCheck.checkRequests.containsValue(moderator)) {
         Iterator var3 = LwCheck.checkRequests.entrySet().iterator();

         while(var3.hasNext()) {
            Entry<Player, Player> entry = (Entry)var3.next();
            if (((Player)entry.getValue()).equals(moderator)) {
               Player targetPlayer = (Player)entry.getKey();
               String message = ChatColor.GRAY + "[" + ChatColor.YELLOW + "Модератор" + ChatColor.GRAY + "] " + ChatColor.YELLOW + moderator.getName() + ": " + event.getMessage();
               targetPlayer.sendMessage(message);
               moderator.sendMessage(message);
            }
         }
      }

   }

   @EventHandler
   public void join(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      Iterator var3 = Bukkit.getOnlinePlayers().iterator();

      while(var3.hasNext()) {
         Player p = (Player)var3.next();
         if (p.hasPermission("LwCheck.notify")) {
            p.spigot().sendMessage((new ComponentBuilder(ChatColor.GOLD + "[LwCheck] " + ChatColor.GRAY + "Игрок " + ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " Зашел на сервер. Вы хотите его вызвать на проверку? *Нажмите на сообщение*")).event(new ClickEvent(Action.RUN_COMMAND, "/check " + player.getName())).create());
         }
      }

   }

   public static void notifyModeratorActions(Player moderator, Player targetPlayer, String message) {
      Iterator var3 = Bukkit.getOnlinePlayers().iterator();

      while(var3.hasNext()) {
         Player p = (Player)var3.next();
         if (p.hasPermission("LwCheck.notify")) {
            p.sendMessage(ChatColor.GRAY + "Модератор " + ChatColor.YELLOW + moderator.getName() + ChatColor.GRAY + " провел проверку игрока " + ChatColor.YELLOW + targetPlayer.getName() + ChatColor.GRAY + " и обнаружил читы. " + message);
         }
      }

   }

   @EventHandler
   public void PlayerChat(AsyncPlayerChatEvent event) {
      Player player = event.getPlayer();
      if (LwCheck.checkRequests.containsKey(player)) {
         Player moderator = (Player)LwCheck.checkRequests.get(player);
         event.setCancelled(true);
         String message = ChatColor.GRAY + "[" + ChatColor.YELLOW + "Подозреваемый" + ChatColor.GRAY + "] " + ChatColor.YELLOW + player.getName() + ": " + event.getMessage();
         moderator.sendMessage(message);
         player.sendMessage(message);
      }

   }
}
