package lightwolf.lwcheck;

import lightwolf.lwcheck.LwCheck;
import java.util.Iterator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Check implements CommandExecutor, Listener {
   private FileConfiguration config;

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(ChatColor.RED + "Команды доступны только игрокам!");
         return true;
      } else {
         Player player = (Player) sender;
         if (!player.hasPermission("LwCheck.check")) {
            player.sendMessage(ChatColor.RED + "У вас нет прав для использования этой команды.");
            return true;
         } else {
            if (command.getName().equalsIgnoreCase("check")) {
               if (args.length < 1) {
                  player.sendMessage(applyColor(config.getString("prefix") + ChatColor.GREEN + " Команды проверки на читы:"));
                  player.sendMessage(applyColor(config.getString("prefix") + ChatColor.YELLOW + " /check ник - вызвать игрока на проверку на читы."));
                  player.sendMessage(applyColor(config.getString("prefix") + ChatColor.YELLOW + " /check ник allow - разрешить игроку после успешной проверки."));
                  player.sendMessage(applyColor(config.getString("prefix") + ChatColor.YELLOW + " /check ник dis - забанить игрока за использование читов."));
                  player.sendMessage(applyColor(config.getString("prefix") + ChatColor.YELLOW + " /check ник ignore - забанить игрока за игнор проверки."));
                  return true;
               }

               Player targetPlayer = Bukkit.getServer().getPlayer(args[0]);
               if (targetPlayer == null) {
                  player.sendMessage(ChatColor.RED + "Игрок с ником " + args[0] + " не найден.");
                  return true;
               }

               if (LwCheck.checkRequests.containsKey(targetPlayer) && args.length == 1) {
                  player.sendMessage(ChatColor.RED + "Вы уже вызвали этого игрока на проверку.");
                  return true;
               }

               if (args.length == 1) {
                  playerCheck(player, targetPlayer);
               } else if (args.length == 2 && args[1].equalsIgnoreCase("allow")) {
                  allowCheck(player, targetPlayer);
               } else if (args.length == 2 && args[1].equalsIgnoreCase("dis")) {
                  disCheck(player, targetPlayer);
               } else if (args.length == 2 && args[1].equalsIgnoreCase("ignore")) {
                  ignoreCheck(player, targetPlayer);
               } else {
                  player.sendMessage(ChatColor.RED + "Использование: /check <ник> [allow|dis|ignore]");
               }
            }

            return false;
         }
      }
   }
   public Check(JavaPlugin plugin) {
      this.config = plugin.getConfig();
   }
   public String applyColor(String message) {
      String[] parts = message.split("&");
      StringBuilder result = new StringBuilder();
      for (int i = 0; i < parts.length; i++) {

         if (i != 0 && !parts[i].isEmpty()) {
            ChatColor color = ChatColor.getByChar(parts[i].charAt(0));

            if (color != null) {
               result.append(color);
            }

            if (parts[i].length() > 1) {
               result.append(parts[i].substring(1));
            }
         } else {
            result.append(parts[i]);
         }
      }

      return result.toString();
   }
   private void playerCheck(Player player, Player targetPlayer) {
      targetPlayer.setGameMode(GameMode.SURVIVAL);
      targetPlayer.setInvulnerable(true);
      Player moderator = player;
      LwCheck.checkRequests.put(targetPlayer, player);
      targetPlayer.sendTitle(ChatColor.RED + "Проверка на читы", "", 100, 200, 200);
      targetPlayer.sendRawMessage(ChatColor.RED + "Следуйте инструкции в чате");
      Location moderatorLocation = player.getLocation();
      targetPlayer.teleport(moderatorLocation);
      Iterator var9 = Bukkit.getOnlinePlayers().iterator();

      while (var9.hasNext()) {
         Player p = (Player) var9.next();
         if (p.hasPermission("LwCheck.notify")) {
            p.sendMessage(ChatColor.GRAY + "Модератор " + ChatColor.YELLOW + moderator.getName() + ChatColor.GRAY + " вызвал игрока " + ChatColor.YELLOW + targetPlayer.getName() + ChatColor.GRAY + " на проверку на читы.");
         }
      }

      targetPlayer.sendMessage(ChatColor.GREEN + " ");
      targetPlayer.sendMessage(ChatColor.GREEN + LwCheck.message1);
      targetPlayer.sendMessage(ChatColor.GREEN + LwCheck.message2);
      targetPlayer.sendMessage(ChatColor.GREEN + " ");
   }

   private void allowCheck(Player player, Player targetPlayer) {
      if (!LwCheck.checkRequests.containsKey(targetPlayer)) {
         player.sendMessage(ChatColor.RED + "Игрок не находится на проверке.");
         return;
      }

      Player moderator = (Player) LwCheck.checkRequests.get(targetPlayer);
      LwCheck.checkRequests.remove(targetPlayer);
      moderator.sendMessage(ChatColor.GREEN + "Игрок " + targetPlayer.getName() + " успешно прошел проверку на читы.");
      targetPlayer.sendMessage(ChatColor.GREEN + "Вы успешно прошли проверку на читы.");
      Iterator var11 = Bukkit.getOnlinePlayers().iterator();

      while (var11.hasNext()) {
         Player p = (Player) var11.next();
         targetPlayer.setGameMode(GameMode.SURVIVAL);
         targetPlayer.setInvulnerable(false);
         if (p.hasPermission("LwCheck.notify")) {
            p.sendMessage(ChatColor.GRAY + "Модератор " + ChatColor.YELLOW + moderator.getName() + ChatColor.GRAY + " закончил проверку игрока " + ChatColor.YELLOW + targetPlayer.getName() + ChatColor.GRAY + " и не обнаружил читов.");
         }
      }
   }

   private void disCheck(Player player, Player targetPlayer) {
      if (LwCheck.checkRequests.containsKey(targetPlayer)) {
         targetPlayer.sendTitle(ChatColor.RED + "", "", 0, 0, 0);
         Player moderator = (Player) LwCheck.checkRequests.get(targetPlayer);
         LwCheck.checkRequests.entrySet().removeIf((entry) -> {
            return ((Player) entry.getValue()).equals(player);
         });

         String banReason = LwCheck.banReason;
         String banCommand = "ban " + targetPlayer.getName() + " " + LwCheck.banDuration + "d " + banReason;
         moderator.performCommand(banCommand);


         Iterator var11 = Bukkit.getOnlinePlayers().iterator();

         while (var11.hasNext()) {
            Player p = (Player) var11.next();
            targetPlayer.setGameMode(GameMode.SURVIVAL);
            targetPlayer.setInvulnerable(false);
            if (p.hasPermission("LwCheck.notify")) {
               p.sendMessage(ChatColor.GRAY + "Модератор " + ChatColor.YELLOW + moderator.getName() + ChatColor.GRAY + " закончил проверку игрока " + ChatColor.YELLOW + targetPlayer.getName() + ChatColor.GRAY + " и обнаружил читы. Игрок забанен.");
            }
         }
      } else {
         player.sendMessage(ChatColor.RED + "Вы не можете использовать эту команду. Игрок не находится на проверке или вы не вызывали его.");
      }
   }
   private void ignoreCheck(Player player, Player targetPlayer) {
      if (LwCheck.checkRequests.containsKey(targetPlayer)) {
         targetPlayer.sendTitle(ChatColor.RED + "", "", 0, 0, 0);
         Player moderator = (Player) LwCheck.checkRequests.get(targetPlayer);
         LwCheck.checkRequests.entrySet().removeIf((entry) -> {
            return ((Player) entry.getValue()).equals(player);
         });

         String banIgnore = LwCheck.banIgnore;
         String banCommand = "ban " + targetPlayer.getName() + " " + LwCheck.banDuration + "d " + banIgnore;
         moderator.performCommand(banCommand);


         Iterator var11 = Bukkit.getOnlinePlayers().iterator();

         while (var11.hasNext()) {
            Player p = (Player) var11.next();
            targetPlayer.setGameMode(GameMode.SURVIVAL);
            targetPlayer.setInvulnerable(false);
            if (p.hasPermission("LwCheck.notify")) {
               p.sendMessage(ChatColor.GRAY + "Модератор " + ChatColor.YELLOW + moderator.getName() + ChatColor.GRAY + " закончил проверку игрока " + ChatColor.YELLOW + targetPlayer.getName() + ChatColor.GRAY + " Игрок отказался от проверки.");
            }
         }
      } else {
         player.sendMessage(ChatColor.RED + "Вы не можете использовать эту команду. Игрок не находится на проверке или вы не вызывали его.");
      }
   }


}
